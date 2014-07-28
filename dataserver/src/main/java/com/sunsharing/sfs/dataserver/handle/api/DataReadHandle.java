package com.sunsharing.sfs.dataserver.handle.api;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.api.DataRead;
import com.sunsharing.sfs.common.pro.api.DataReadResult;
import com.sunsharing.sfs.dataserver.Config;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by criss on 14-7-27.
 */
@HandleAno(action = Constant.DATA_READ)
public class DataReadHandle implements Handle {

    Logger logger = Logger.getLogger(DataReadHandle.class);

    @Override
    public void handler(Channel channel, BaseProtocol pro) {
        DataRead read = (DataRead)pro;
        int blockId = read.getBlockId();
        long blockIndex = read.getBlockIndex();
        long filelen = read.getLength();

        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        RandomAccessFile raf1 = null;
        List<ChannelFuture> futures = new ArrayList<ChannelFuture>();
        try
        {
            raf1 = new RandomAccessFile(blockDir,"rw");
            final FileRegion region =
                    new DefaultFileRegion(raf1.getChannel(), blockIndex, filelen,true);
            DataReadResult result = new DataReadResult();
            result.setStatus(true);
            result.setMessageId(read.getMessageId());
            result.setTotallen(read.getLength());
            futures.add(channel.write(result));
            futures.add(channel.write(region));
        }catch (Exception e)
        {
            DataReadResult result = new DataReadResult();
            result.setStatus(false);
            result.setMessageId(read.getMessageId());
            result.setMsg("读取blockId出错,错误信息:" + e.getMessage());
            futures.add(channel.write(result));
            logger.error("读block出错", e);

        }finally {
            for(ChannelFuture future:futures)
            {
                future.awaitUninterruptibly();
            }
            try
            {
                channel.close();
            }catch (Exception e)
            {

            }
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }

    }
}
