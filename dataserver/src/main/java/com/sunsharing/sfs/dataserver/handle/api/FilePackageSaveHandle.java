package com.sunsharing.sfs.dataserver.handle.api;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.distribute.DistributeCall;
import com.sunsharing.sfs.common.distribute.DistributeTransaction;
import com.sunsharing.sfs.common.netty.LongNettyClient;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.api.FilePackageResult;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndex;
import com.sunsharing.sfs.common.pro.dataserver.FileSuccessUploadIndex;
import com.sunsharing.sfs.common.pro.dataserver.FileSuccessUploadIndexResult;
import com.sunsharing.sfs.dataserver.Config;
import com.sunsharing.sfs.dataserver.block.Block;
import com.sunsharing.sfs.dataserver.block.BlockCache;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import com.sunsharing.sfs.dataserver.server.file.FilePackageUpdateIndexThread;
import com.sunsharing.sfs.dataserver.server.file.FilePakageThread;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by criss on 14-7-22.
 */
@HandleAno(action = Constant.FILE_PACKAGE_SAVE)
public class FilePackageSaveHandle implements Handle {
    static  ExecutorService exec = Executors.newFixedThreadPool(5);
    Logger logger = Logger.getLogger(FilePackageSaveHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {
        FilePakageSave filePakageSave = (FilePakageSave)baseProtocol;
        ChannelFuture future = null;
        Block block = BlockCache.getBlock(filePakageSave.getBlockId());

        try
        {
            if(block==null)
            {
                FilePackageResult result2 = new FilePackageResult();
                result2.setStatus(true);
                result2.setMessageId(filePakageSave.getMessageId());
                future = channel.write(result2);
                return;
            }
            synchronized (block)
            {
                if(!filePakageSave.isFileDoSuccess())
                {

                    FilePackageResult result = createError(filePakageSave,"DataServer:"+Config.dataserverId+":"+filePakageSave.getFileName()+":pk:"+
                            filePakageSave.getCurrentPakage()+"：错误信息:"+filePakageSave.getErrorMsg());
                    logger.error(result.getMsg());
                    future = channel.write(result);
                    return;
                }

                if(filePakageSave.isLeader(new Integer(Config.dataserverId)))
                {
                    //1.领导者，将该发送至其他服务器
                    String [] dataServers = filePakageSave.getDataservers();
                    List<DistributeCall> calls = new ArrayList<DistributeCall>();
                    DistributeTransaction tras = new DistributeTransaction();
                    for(int i=0;i<dataServers.length;i++)
                    {
                        String[] arr = dataServers[i].split(":");
                        String dsId = arr[0];
                        String ip = arr[1];
                        String fileport = arr[2];
                        if(!dsId.equals(Config.dataserverId))
                        {
                            //不是自己服务器发送
                            FilePakageThread thread = new FilePakageThread(filePakageSave.clone(),ip,fileport);
                            calls.add(thread);
                        }
                    }
                    String result = tras.excute(calls,exec);
                    if(!"success".equals(result))
                    {
                        logger.error("其他Dataserver无法返回正确的信息");
                        FilePackageResult result1 = createError(filePakageSave,"DataServer:"+Config.dataserverId+":"+filePakageSave.getFileName()+":pk:"+
                                filePakageSave.getCurrentPakage()+"：错误信息:"+result);
                        future = channel.write(result1);
                        return;
                    }
                    //2.成功存储，更新包的位置成功信息
                    BlockWrite.getInstance().writePackage(filePakageSave.getBlockId(),filePakageSave.getCurrentPakage());
                    //3.判断文件是否传送成功 ,这个判断有问题，当同一个文件多个包同时到达时，这时候所有包的处理，都是成功了。这个bug比较隐蔽
                    //增加同步语句
                    boolean isSuccess = BlockWrite.getInstance().fileAllPackageSuccess(filePakageSave.getBlockId(),
                        filePakageSave.getTotalPakage());
                    if(isSuccess)
                    {
                        //4.发现所有都成功，向所有服务器发布更新索引的信息
                        //更新package的状态
                        BlockWrite.getInstance().updatePackage0(filePakageSave.getBlockId(),
                                filePakageSave.getTotalPakage());
                        List<DistributeCall> indexCalls = new ArrayList<DistributeCall>();
                        long[] beforeIndex = BlockWrite.getInstance().getBeforeIndex(filePakageSave.getBlockId());
                        for(int i=0;i<dataServers.length;i++)
                        {
                            String[] arr = dataServers[i].split(":");
                            String ip = arr[1];
                            String msgPort = arr[2];
                            FilePakageUpdateIndex index = new FilePakageUpdateIndex();
                            index.setBlockId(filePakageSave.getBlockId());
                            index.setTotalSize(filePakageSave.getTotalSize());
                            index.setFileName(filePakageSave.getFileName());
                            index.setMessageId(StringUtils.generateUUID());
                            index.setExtendSize(filePakageSave.getExtendFileSize());
                            if(filePakageSave.getOldBlockIndex()!=0)
                            {
                                index.setUpdateFile(true);
                                index.setOldFileSize(filePakageSave.getOldFilesize());
                                index.setOldExtendFile(filePakageSave.getOldExtendFileSize());
                            }

                            FilePackageUpdateIndexThread t = new FilePackageUpdateIndexThread(
                                   index ,beforeIndex,ip,msgPort,filePakageSave
                            );
                            indexCalls.add(t);
                        }
                        String updateIndexResult = tras.excute(indexCalls);
                        if("success".equals(updateIndexResult))
                        {
                            //5.向nameServer汇报成功上传了
                            try
                            {
                                LongNettyClient nettyClient = new LongNettyClient();
                                FileSuccessUploadIndex uploadIndex = new FileSuccessUploadIndex();
                                uploadIndex.setBlockId(filePakageSave.getBlockId());
                                uploadIndex.setFileSize(filePakageSave.getTotalSize());
                                uploadIndex.setBlockIndex(beforeIndex[0]);
                                uploadIndex.setFilename(filePakageSave.getFileName());
                                uploadIndex.setExtendFileSize(filePakageSave.getExtendFileSize());
                                if(filePakageSave.getOldBlockIndex()!=0)
                                {
                                    uploadIndex.setUpdateFile(true);
                                    uploadIndex.setOldFileSize(filePakageSave.getOldFilesize());
                                    uploadIndex.setOldExt(filePakageSave.getOldExtendFileSize());
                                }


                                FileSuccessUploadIndexResult uploadResult =
                                        (FileSuccessUploadIndexResult)nettyClient.request(uploadIndex,Config.nameServerIp,new Integer(Config.nameServerPort),5000);
                                if(!uploadResult.isStatus())
                                {
                                    throw new RuntimeException(uploadResult.getMsg());
                                }
                                FilePackageResult result2 = new FilePackageResult();
                                result2.setStatus(true);
                                result2.setMessageId(filePakageSave.getMessageId());
                                future = channel.write(result2);
                            }catch (Exception e)
                            {
                                logger.error("上传状态报错",e);
                                //回滚状态
                                DistributeTransaction t = new DistributeTransaction();
                                t.rollback(indexCalls);
                                FilePackageResult result2 = createError(filePakageSave,e.getMessage());
                                future = channel.write(result2);
                                return;
                            }


                        }else
                        {
                            FilePackageResult result1 = createError(filePakageSave,"DataServer:"+
                                    Config.dataserverId+":"+filePakageSave.getFileName()+":pk:"+
                                    filePakageSave.getCurrentPakage()+"：错误信息:"+updateIndexResult);
                            future = channel.write(result1);
                            return;
                        }
                    }else
                    {
                        //本次包已经成功
                        FilePackageResult result2 = new FilePackageResult();
                        result2.setStatus(true);
                        result2.setMessageId(filePakageSave.getMessageId());
                        future = channel.write(result2);
                    }
                }else
                {
                    //非领导者,返回一个处理成功的标志
                    FilePackageResult result = new FilePackageResult();
                    result.setStatus(true);
                    result.setMessageId(filePakageSave.getMessageId());
                    future = channel.write(result);
                    return;
                }
            }

        }catch (Exception e)
        {
            logger.error("处理报文错误",e);
            FilePackageResult result = createError(filePakageSave,"DataServer:"+Config.dataserverId+":"+filePakageSave.getFileName()+":pk:"+
                    filePakageSave.getCurrentPakage()+"：错误信息:"+e.getMessage());
            future = channel.write(result);
            return;
        }finally {
            if(future!=null)
            {
                future.awaitUninterruptibly();
                channel.disconnect();
            }else
            {
                channel.disconnect();
            }
            //channel.getCloseFuture()
            //channel.disconnect();
        }
    }

    private FilePackageResult createError(FilePakageSave filePakageSave,String message)
    {
        FilePackageResult result = new FilePackageResult();
        result.setStatus(false);
        result.setMsg(message);
        result.setFilename(filePakageSave.getFileName());
        result.setCurrentPakage(filePakageSave.getCurrentPakage());
        result.setFromIndex(filePakageSave.getFromIndex());
        result.setToIndex(filePakageSave.getToIndex());
        result.setMessageId(filePakageSave.getMessageId());
        return result;
    }
}
