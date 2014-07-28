package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-27.
 */
@Protocol(action = Constant.READ_FILE_RESULT)
public class ReadFileResult extends JsonBodyProtocol {

    boolean status;

    String msg;

    String[] dataServers;

    long blockIndex;

    long fileSize;

    public String[] getDataServers() {
        return dataServers;
    }

    public void setDataServers(String[] dataServers) {
        this.dataServers = dataServers;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
