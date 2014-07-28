package com.sunsharing.sfs.nameserver.handle.api;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.api.ReleaseLock;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-25.
 */
@HandleAno(action = Constant.RELEASE_LOCK)
public class ReleaseLockHandle implements Handle {
    @Override
    public void handler(Channel channel, BaseProtocol pro) {
        ReleaseLock releaseLock = (ReleaseLock)pro;
        int blockId = releaseLock.getBlockId();
        Block block = BlockCache.getBlockById(blockId);
        if(block!=null)
        {
            block.releaseLock();
        }
        channel.close();
    }
}
