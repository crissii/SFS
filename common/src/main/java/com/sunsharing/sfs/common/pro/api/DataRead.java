package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-27.
 */
@Protocol(action = Constant.DATA_READ)
public class DataRead extends JsonBodyProtocol {

    int blockId;

    long blockIndex;

    long length;

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public long getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(long blockIndex) {
        this.blockIndex = blockIndex;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
