package com.sunsharing.sfs.dataserver.handle.nameserver.createBlock;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.nameserver.createblock.BlockCreateError;
import com.sunsharing.sfs.common.pro.nameserver.createblock.BlockCreateRequest;
import com.sunsharing.sfs.common.pro.nameserver.createblock.BlockCreateResult;
import com.sunsharing.sfs.common.utils.ExceptionUtils;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * 创建Block处理
 * Created by criss on 14-7-2.
 */
@HandleAno(action = Constant.BLOCK_CREATE_REQUEST)
public class BlockRequestHandle implements Handle {
    Logger logger = Logger.getLogger(BlockRequestHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {
        BlockCreateRequest request = (BlockCreateRequest)baseProtocol;
        int blockId = request.getBlockId();
        try
        {
            BlockWrite.getInstance().writeBlock(blockId);
            BlockCreateResult result = new BlockCreateResult();
            result.setMessageId(request.getMessageId());
            channel.write(result);
        }catch (Exception e)
        {
            logger.error("创建block"+blockId+"出错",e);
            String localAddress = channel.getLocalAddress().toString();
            BlockCreateError blockCreateError = new BlockCreateError();
            blockCreateError.setMessageId(request.getMessageId());
            blockCreateError.setErrorMsg(localAddress+":"+"创建block:"+blockId+":出错\n"+
                    ExceptionUtils.exception2String(e));
            channel.write(blockCreateError);
        }
    }
}
