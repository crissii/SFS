package com.sunsharing.sfs.common.pro.dataserver;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-24.
 */
@Protocol(action = Constant.FILE_PACKAGE_UPDATE_ROLLBACK)
public class FilePakageUpdateIndexRollBack extends JsonBodyProtocol {

    private long blockIndx;

    private long infoIndx;

    private int blockId;

    public long getBlockIndx() {
        return blockIndx;
    }

    public void setBlockIndx(long blockIndx) {
        this.blockIndx = blockIndx;
    }

    public long getInfoIndx() {
        return infoIndx;
    }

    public void setInfoIndx(long infoIndx) {
        this.infoIndx = infoIndx;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
