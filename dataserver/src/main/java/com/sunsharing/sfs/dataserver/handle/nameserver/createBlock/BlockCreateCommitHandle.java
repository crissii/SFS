package com.sunsharing.sfs.dataserver.handle.nameserver.createBlock;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.dataserver.block.BlockCache;
import com.sunsharing.sfs.common.pro.nameserver.createblock.*;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-2.
 */
@HandleAno(action = Constant.BLOCK_CREATE_COMMIT)
public class BlockCreateCommitHandle implements Handle {

    Logger logger = Logger.getLogger(BlockCreateCommitHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {

        BlockCreateCommit commit = (BlockCreateCommit)baseProtocol;

        BlockCache.addBlock(commit.getBlockId());

    }

}
