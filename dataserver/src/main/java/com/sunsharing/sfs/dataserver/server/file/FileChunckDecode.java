package com.sunsharing.sfs.dataserver.server.file;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.ProFactory;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.common.utils.FileMd5;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import java.io.RandomAccessFile;

/**
 * 文件传输
 * User: criss
 * Date: 13-7-22
 * Time: 下午2:54
 * To change this template use File | Settings | File Templates.
 */
public class FileChunckDecode  extends ReplayingDecoder<FileChunckDecode.State> {
    Logger logger = Logger.getLogger(FileChunckDecode.class);

    public FileChunckDecode()
    {
         super(State.Read_header,true);
    }

    BaseProtocol pro = null;
    RandomAccessFile raf = null;
    long currentWriteLen = 0L;

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, State state) {
        switch (state)
        {
            case Read_header:
               pro = ProFactory.createPro(buffer);
               if(pro instanceof FilePakageSave)
               {
                   logger.info("收到："+((FilePakageSave) pro).getFileName()+",包："+((FilePakageSave) pro).getCurrentPakage());
                   FilePakageSave fs = (FilePakageSave)pro;
                   try
                   {
                        raf = BlockWrite.getInstance().openBlock(fs.getBlockId());
                        //raf.seek(64*1024*1024L);
                        //读取前八位
                        if(fs.getOldBlockIndex()==0)
                        {
                            //新增
                            currentWriteLen = raf.readLong();
                        }else
                        {
                            //更新文件
                            currentWriteLen = fs.getOldBlockIndex();
                        }
                        if(currentWriteLen+fs.getTotalSize()+fs.getExtendFileSize()>=64*1024*1024L)
                        {
                            ((FilePakageSave) pro).setFileDoSuccess(false);
                            ((FilePakageSave) pro).setErrorMsg("oldBlockIndex:"+fs.getOldBlockIndex()+":"+fs.getFileName()+"无法写Block文件:"+fs.getBlockId()+"超过64M文件长度,currentWriteLen:"+currentWriteLen+":totalSize:"+fs.getTotalSize()+":"+fs.getExtendFileSize());
                            BlockWrite.getInstance().closeBlock(raf);
                            //return reset(pro);
                        }else
                        {
                            raf.seek(currentWriteLen+fs.getFromIndex());
                        }
                   }catch (Exception e)
                   {
                       logger.error("无法打开Block文件:"+fs.getBlockId(),e);
                       ((FilePakageSave) pro).setFileDoSuccess(false);
                       ((FilePakageSave) pro).setErrorMsg("无法打开Block文件:"+fs.getBlockId());
                       BlockWrite.getInstance().closeBlock(raf);
                       //return reset(pro);
                   }
                   checkpoint(State.Read_Content);
               }else
               {
                   return reset(pro);
               }
            case Read_Content:
                FilePakageSave fs = (FilePakageSave)pro;
                if(!buffer.readable())
                {
                    checkpoint(State.Read_Content);
                } else
                {
                    //处理文件
                    long currentChunkSize = ((FilePakageSave) pro).getToIndex()-((FilePakageSave) pro).getFromIndex();
                    int maxCanRead = buffer.readableBytes();
                    currentChunkSize = Math.min(maxCanRead, currentChunkSize);

                    if(!StringUtils.isBlank(((FilePakageSave) pro).getErrorMsg()))
                    {
                        buffer.readBytes((int)currentChunkSize).array();
                        //buffer.
                        fs.increate(currentChunkSize);
                    }else
                    {
                        try{
                            byte[] bu = buffer.readBytes((int)currentChunkSize).array();
                            raf.write(bu);
                            fs.increate(currentChunkSize);
                        }catch(Exception e){
                            logger.error("接收错误,blockId:"+fs.getBlockId(),e);
                            //throw new RuntimeException("接收文件包出错");
                            ((FilePakageSave) pro).setFileDoSuccess(false);
                            ((FilePakageSave) pro).setErrorMsg("接收错误,blockId:"+fs.getBlockId());
                            BlockWrite.getInstance().closeBlock(raf);
                            //return reset(pro);
                            checkpoint(State.Read_Content);
                        }
                    }
                    logger.info("成功写入block,blockId:"+fs.getBlockId()+":大小:"+currentChunkSize);
                    if(fs.isLast()){
                        //结束这个包的读取
                        //raf.close();
                        BlockWrite.getInstance().closeBlock(raf);
                        if(!StringUtils.isBlank(((FilePakageSave) pro).getErrorMsg()))
                        {
                            return reset(pro);
                        }
                        try
                        {

                            String md5 = FileMd5.getFileMD5String(BlockWrite.getInstance().getBlockFile(fs.getBlockId()),
                                    currentWriteLen+((FilePakageSave) pro).getFromIndex(), currentChunkSize);
                            logger.info("包序号:"+((FilePakageSave) pro).getCurrentPakage()+"," +
                                    "源Md5:"+(((FilePakageSave) pro).getMd5())+",目的Md5:"+md5+":currentWriteLen:"+currentWriteLen+"" +
                                    ":currentChunkSize"+currentChunkSize+":fromeIndex:"+((FilePakageSave) pro).getFromIndex());
                            if(!md5.equals(((FilePakageSave) pro).getMd5()))
                            {
                                ((FilePakageSave) pro).setFileDoSuccess(false);
                                BlockWrite.getInstance().closeBlock(raf);
                                logger.error("校验MD5出错,文件名:" + ((FilePakageSave) pro).getFileName() + ",包序号:" + ((FilePakageSave) pro).getCurrentPakage());
                                ((FilePakageSave) pro).setErrorMsg("校验MD5出错,文件名:" + ((FilePakageSave) pro).getFileName() + ",包序号:" + ((FilePakageSave) pro).getCurrentPakage());
                                return reset(pro);
                            }
                            ((FilePakageSave) pro).setFileDoSuccess(true);
                            return reset(pro);//读取完一个完整的帧
                        }catch (Exception e)
                        {
                            logger.error("校验MD5出错,文件名:"+((FilePakageSave) pro).getFileName()+",包序号:"+((FilePakageSave) pro).getCurrentPakage(),e);
                            //throw new RuntimeException(e.getMessage());
                            ((FilePakageSave) pro).setFileDoSuccess(false);
                            ((FilePakageSave) pro).setErrorMsg("校验MD5出错,文件名:" + ((FilePakageSave) pro).getFileName() + ",包序号:" + ((FilePakageSave) pro).getCurrentPakage());
                            BlockWrite.getInstance().closeBlock(raf);
                            return reset(pro);
                        }finally {

                        }
                    }else
                    {
                        checkpoint(State.Read_Content);
                    }
                }
                break;
        }
        return reset(pro);
    }

    protected enum State {
        Read_header,
        Read_Content
    }
    private Object reset(BaseProtocol pro) {
        checkpoint(State.Read_header);
        return pro;
    }



}
