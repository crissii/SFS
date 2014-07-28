package com.sunsharing.sfs.nameserver.handle.api;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.api.ReadFile;
import com.sunsharing.sfs.common.pro.api.ReadFileResult;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 * Created by criss on 14-7-27.
 */
@HandleAno(action = Constant.READ_FILE)
public class ReadFileHandle implements Handle {
    Logger logger = Logger.getLogger(ReadFileHandle.class);
    @Override
    public void handler(Channel channel, BaseProtocol pro) {
        ReadFile readFile = (ReadFile)pro;
        ChannelFuture future = null;
        try
        {
            int blockId = readFile.getBlockId();
            Block block = BlockCache.getBlockById(blockId);
            if(block==null)
            {
                throw new RuntimeException("找不到blockId:"+blockId+"的缓存");
            }
            byte[] fileInfo = block.getFileInfo(readFile.getFilename());
            byte[] blockIndex = new byte[8];
            byte[] fileSize = new byte[8];
            System.arraycopy(fileInfo,0,blockIndex,0,8);
            System.arraycopy(fileInfo,8,fileSize,0,8);
            String[] dataServers = block.getOnlineDataServerIpPort();
            ReadFileResult result = new ReadFileResult();
            result.setStatus(true);
            result.setDataServers(dataServers);
            result.setFileSize(ByteUtils.bytesTolong(fileSize));
            result.setBlockIndex(ByteUtils.bytesTolong(blockIndex));
            result.setMessageId(readFile.getMessageId());
            future = channel.write(result);
        }catch (Exception e)
        {
            logger.error("读取block:"+readFile.getBlockId()+",错误信息:"+e.getMessage(),e);
            ReadFileResult result = new ReadFileResult();
            result.setStatus(false);
            result.setMsg("读取block:"+readFile.getBlockId()+",错误信息:"+e.getMessage());
            result.setMessageId(readFile.getMessageId());
            future = channel.write(result);
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
}
