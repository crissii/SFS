package com.sunsharing.sfs.requestapi;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.ProFactory;
import com.sunsharing.sfs.common.pro.api.DataReadResult;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.common.utils.FileMd5;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * 文件传输
 * User: criss
 * Date: 13-7-22
 * Time: 下午2:54
 * To change this template use File | Settings | File Templates.
 */
public class FileReadChunckDecode extends ReplayingDecoder<FileReadChunckDecode.State> {
    Logger logger = Logger.getLogger(FileReadChunckDecode.class);

    public FileReadChunckDecode()
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

               if(pro instanceof DataReadResult)
               {
                   logger.info("收到："+((DataReadResult) pro));
                   DataReadResult result = (DataReadResult)pro;
                   if(!result.isStatus())
                   {
                       result.setDoSuccess(false);
                       result.setDoError("接收错误:"+result.getMsg());
                       return reset(pro);
                   }
                   checkpoint(State.Read_Content);
               }else
               {
                   return reset(pro);
               }
            case Read_Content:
                DataReadResult fs = (DataReadResult)pro;
                if(!buffer.readable())
                {
                    checkpoint(State.Read_Content);
                } else
                {
                    //处理文件
                    long currentChunkSize = fs.getTotallen();
                    int maxCanRead = buffer.readableBytes();
                    currentChunkSize = Math.min(maxCanRead, currentChunkSize);

                    try
                    {
                        OutputStream out = (OutputStream)FileReadClient.readCache.get(fs.getMessageId());
                        if(out!=null)
                        {
                            byte[] hehe = buffer.readBytes((int)currentChunkSize).array();
                            System.out.println("0000000:"+hehe.length);
                            out.write(hehe);
                            fs.addOffSet(currentChunkSize);
                        }
                    }catch (Exception e)
                    {
                        logger.error("接收错误:",e);
                        fs.setDoSuccess(false);
                        fs.setDoError("接收错误:"+e.getMessage());
                        return reset(pro);
                    }

                    if(fs.islast()){
                        //结束这个包的读取
                        //raf.close();
                        return reset(pro);
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
