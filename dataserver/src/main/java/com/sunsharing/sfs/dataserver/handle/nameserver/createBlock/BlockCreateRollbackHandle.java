package com.sunsharing.sfs.dataserver.handle.nameserver.createBlock;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.nameserver.createblock.BlockCreateCommit;
import com.sunsharing.sfs.common.pro.nameserver.createblock.BlockCreateRollback;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-2.
 */
@HandleAno(action = Constant.BLOCK_CREATE_ROLLBACK)
public class BlockCreateRollbackHandle implements Handle {

    Logger logger = Logger.getLogger(BlockCreateRollbackHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {
        BlockCreateRollback commit = (BlockCreateRollback)baseProtocol;
        int blockId = commit.getBlockId();
        BlockWrite.getInstance().rollbackBlock(blockId);
    }
}
