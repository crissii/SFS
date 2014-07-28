package com.sunsharing.sfs.common.pro.nameserver.createblock;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * dataServer向NameServer发起创建Block的请求，返回BlockId;
 * Created by criss on 14-6-30.
 */
@Protocol(action = Constant.BLOCK_CREATE_REQUEST)
public class BlockCreateRequest extends JsonBodyProtocol {

    int blockId;

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
