package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-27.
 */
@Protocol(action = Constant.READ_FILE)
public class ReadFile extends JsonBodyProtocol {

    private int blockId;
    private long filename;

    public long getFilename() {
        return filename;
    }

    public void setFilename(long filename) {
        this.filename = filename;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
