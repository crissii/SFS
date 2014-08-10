package com.sunsharing.sfs.common.pro.dataserver;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-23.
 */
@Protocol(action = Constant.FILE_PACKAGE_UPDATE_INDEX)
public class FilePakageUpdateIndex extends JsonBodyProtocol {

    int blockId;


    long totalSize;

    long extendSize;

    String fileName;

    boolean updateFile = false;

    long oldFileSize=0;

    long oldExtendFile = 0;

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

    public boolean isUpdateFile() {
        return updateFile;
    }

    public void setUpdateFile(boolean updateFile) {
        this.updateFile = updateFile;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getExtendSize() {
        return extendSize;
    }

    public void setExtendSize(long extendSize) {
        this.extendSize = extendSize;
    }
}
