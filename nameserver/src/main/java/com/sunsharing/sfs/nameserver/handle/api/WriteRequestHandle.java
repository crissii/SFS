package com.sunsharing.sfs.nameserver.handle.api;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.api.ReadFile;
import com.sunsharing.sfs.common.pro.api.WriteRequest;
import com.sunsharing.sfs.common.pro.api.WriteRequestResult;
import com.sunsharing.sfs.common.utils.GetFileName;
import com.sunsharing.sfs.nameserver.Config;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.CreateBlock;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 写请求
 * Created by criss on 14-7-3.
 */
@HandleAno(action = Constant.WRITE_REQUEST)
public class WriteRequestHandle implements Handle {
    Logger logger = Logger.getLogger(WriteRequestHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {
        WriteRequest writeRequest = (WriteRequest)baseProtocol;
        ChannelFuture future = null;

        String filename = writeRequest.getFilename();
        //处理更新文件
        if(!StringUtils.isBlank(filename))
        {
            try
            {
                GetFileName getFileName = new GetFileName();
                byte[] arr = getFileName.getBlockIdBlokLength(filename);
                byte[] blockId = new byte[4];
                byte[] filenameByte = new byte[8];
                System.arraycopy(arr,0,blockId,0,4);
                System.arraycopy(arr,4,filenameByte,0,8);
                int blockIdInt = (ByteUtils.getInt(blockId));
                long filenameLong = (ByteUtils.getLong(filenameByte,0));
                Block block = BlockCache.getBlockById(blockIdInt);
                if(block==null)
                {
                    throw new RuntimeException("blockId:"+blockIdInt+"找不到缓存");
                }
                if(!block.isAllOnline())
                {
                    throw new RuntimeException("无法写入BlockId:"+blockIdInt+"不是所有的DataServer都在线");
                }
                Map<String,Long> fileInfo = block.getFileInfoMap(filenameLong);
                if(writeRequest.getFilesize()>fileInfo.get("fileSize")+fileInfo.get("extendFileSize"))
                {
                    throw new RuntimeException(filename+"超过了预留的更新空间无法更新，请使用上传接口");
                }
                //锁住block
                block.tryLock(5);
                String [] dataServerPort = block.getDataServerIdIpPort();
                List<Integer> dss = block.getOnlineServer();
                int index = new Random().nextInt(dss.size());
                DataServerStat dataServer = DataServerStat.getDataServerStat(dss.get(index));
                WriteRequestResult result = new WriteRequestResult();
                result.setMessageId(writeRequest.getMessageId());
                result.setStatus(true);
                result.setBlockId(blockIdInt);
                result.setLeaderDataserverIp(dataServer.getStatReport().getIp());
                result.setLenderFileServerPort(dataServer.getStatReport().getFileServerport());
                result.setLeaderDataServer(dataServer.getServerId());
                result.setFilesize(writeRequest.getFilesize());
                result.setExtendFilesize(writeRequest.getExtendfilesize());
                result.setDataServer(dataServerPort);
                result.setOldblockIndex(fileInfo.get("blockIndex"));
                result.setOldFilesize(fileInfo.get("fileSize"));
                result.setOldExtendFileSize(fileInfo.get("extendFileSize"));
                future = channel.write(result);
                return;
            }catch (Exception e)
            {
                logger.error("...",e);
                WriteRequestResult result = new WriteRequestResult();
                result.setMessageId(writeRequest.getMessageId());
                result.setStatus(false);
                result.setErrorMsg(e.getMessage());
                future = channel.write(result);
                return;
            }finally {
                if(future!=null)
                {
                    try
                    {
                        future.awaitUninterruptibly();
                        channel.close();
                    }catch (Exception e)
                    {

                    }
                }
            }
        }

        boolean successLock = false;
        //1.获取最空闲的DataServer
        List<DataServerStat> dataServers = DataServerStat.getFreeDataServer(1,0);



        if(dataServers.size()==0)
        {
            WriteRequestResult result = new WriteRequestResult();
            result.setMessageId(writeRequest.getMessageId());
            result.setStatus(false);
            result.setErrorMsg("取不到在线的DataServer");
            future = channel.write(result);
            return;
        }
        DataServerStat dataServer = dataServers.get(0);
        //2.获取dataServer写的锁
        dataServer.tryLock();
        int writeBlockId = 0;
        try
        {
            List<Block> currentBlocks = dataServer.getCurrentblock();
            boolean createBlock = true;
            try
            {
                for(int i=0;i<currentBlocks.size();i++)
                {
                    if(currentBlocks.get(i).canwrite(writeRequest.getFilesize()+
                    writeRequest.getExtendfilesize()))
                    {
                        createBlock = false;
                        writeBlockId = currentBlocks.get(i).getBlockId();
                    }
                }
            }catch (Exception e)
            {
                logger.error("获取block出错",e);
                throw new RuntimeException("获取block出错");
            }

            //3.需要新建Block
            if(createBlock)
            {
                List<DataServerStat> dss =
                        DataServerStat.getFreeDataServer
                                (new Integer(Config.lowestBackNum)-1,dataServer.getServerId());
                if(dss.size()<new Integer(Config.lowestBackNum)-1)
                {
                    throw new RuntimeException("在线服务器服务达到:"+Config.lowestBackNum+"台");
                }
                for(DataServerStat ds:dss)
                {
                    ds.tryLock();
                }
                try
                {
                    CreateBlock cb = new CreateBlock();
                    int blockId = BlockCache.genblockId();
                    int [] dsIds = new int[new Integer(Config.lowestBackNum)];
                    int index = 0;
                    for(Iterator iter = dss.iterator();iter.hasNext();)
                    {
                        DataServerStat tmp = (DataServerStat)iter.next();
                        dsIds[index++] = tmp.getServerId();
                    }
                    dsIds[index] = dataServer.getServerId();
                    String result = cb.createBlock(dsIds,blockId);
                    if(!"success".equals(result))
                    {
                        throw new RuntimeException("无法创建block:"+blockId+"::"+result);
                    }
                    //添加block
                    Block block = BlockCache.addBlock(blockId,dsIds);
                    block.tryLock(5);
                    writeBlockId = block.getBlockId();
                    for(DataServerStat ds:dss)
                    {
                        ds.addCurrentBlock(block);
                    }
                    dataServer.addCurrentBlock(block);
                }catch (Exception e)
                {
                    throw e;
                }finally {
                    for(DataServerStat ds:dss)
                    {
                        ds.releaseLock();
                    }
                }

            }else
            {
                BlockCache.getBlockById(writeBlockId).tryLock(5);
            }
            //4.创建完block,锁住block,通知开始传输
            Block block = BlockCache.getBlockById(writeBlockId);
            boolean isAllOnline = block.isAllOnline();
            if(!isAllOnline)
            {
                WriteRequestResult writeRequestResult = new WriteRequestResult();
                writeRequestResult.setMessageId(writeRequest.getMessageId());
                writeRequestResult.setStatus(false);
                writeRequestResult.setErrorMsg("blockId:" + writeBlockId + ",不是所有的DataServer都在线");
                future = channel.write(writeRequestResult);
                return;
            }

            //block.tryLock(5);
            //成功处理锁定
            String [] dataServerPort = block.getDataServerIdIpPort();
            WriteRequestResult result = new WriteRequestResult();
            result.setMessageId(writeRequest.getMessageId());
            result.setStatus(true);
            result.setBlockId(writeBlockId);
            result.setLeaderDataserverIp(dataServer.getStatReport().getIp());
            result.setLenderFileServerPort(dataServer.getStatReport().getFileServerport());
            result.setLeaderDataServer(dataServer.getServerId());
            result.setFilesize(writeRequest.getFilesize());
            result.setExtendFilesize(writeRequest.getExtendfilesize());
            result.setDataServer(dataServerPort);
            future = channel.write(result);
            successLock = true;
        }catch (Exception e)
        {
            logger.error("...",e);
            WriteRequestResult result = new WriteRequestResult();
            result.setMessageId(writeRequest.getMessageId());
            result.setStatus(false);
            result.setErrorMsg(e.getMessage());
            future = channel.write(result);
            return;
        }finally
        {
            if(future!=null)
            {
                try
                {
                    future.awaitUninterruptibly();
                    channel.close();
                }catch (Exception e)
                {

                }
            }

            dataServer.releaseLock();
            //当block是异常的时候解锁
            if(writeBlockId!=0 && !successLock)
            {
                Block block = BlockCache.getBlockById(writeBlockId);
                if(block!=null)
                {
                    block.releaseLock();
                }
            }

        }




    }
}
