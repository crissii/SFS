package com.sunsharing.sfs.common.pro.nameserver.createblock;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 *
 * Created by criss on 14-7-2.
 */
@Protocol(action = Constant.BLOCK_CREATE_COMMIT)
public class BlockCreateCommit extends JsonBodyProtocol {

    int blockId;

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

}
