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

    private String filename;

    private boolean updateFile =false;

    private long oldFileSize = 0;

    private long oldExtendFile = 0;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isUpdateFile() {
        return updateFile;
    }

    public void setUpdateFile(boolean isUpdateFile) {
        this.updateFile = isUpdateFile;
    }

    public long getOldFileSize() {
        return oldFileSize;
    }

    public void setOldFileSize(long oldFileSize) {
        this.oldFileSize = oldFileSize;
    }

    public long getOldExtendFile() {
        return oldExtendFile;
    }

    public void setOldExtendFile(long oldExtendFile) {
        this.oldExtendFile = oldExtendFile;
    }

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
