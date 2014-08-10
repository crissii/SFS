package com.sunsharing.sfs.common.pro.dataserver;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-24.
 */
@Protocol(action = Constant.File_SUCCESS_UPLOAD_INDEX)
public class FileSuccessUploadIndex extends JsonBodyProtocol {

    int blockId;

    String filename;

    long blockIndex;

    long fileSize;

    long extendFileSize;

    boolean updateFile = false;

    long oldFileSize;

    long oldExt;

    public long getOldFileSize() {
        return oldFileSize;
    }

    public void setOldFileSize(long oldFileSize) {
        this.oldFileSize = oldFileSize;
    }

    public long getOldExt() {
        return oldExt;
    }

    public void setOldExt(long oldExt) {
        this.oldExt = oldExt;
    }

    public boolean isUpdateFile() {
        return updateFile;
    }

    public void setUpdateFile(boolean updateFile) {
        this.updateFile = updateFile;
    }

    public long getExtendFileSize() {
        return extendFileSize;
    }

    public void setExtendFileSize(long extendFileSize) {
        this.extendFileSize = extendFileSize;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(long blockIndex) {
        this.blockIndex = blockIndex;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
