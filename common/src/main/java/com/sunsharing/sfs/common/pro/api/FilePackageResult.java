package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-22.
 */
@Protocol(action = Constant.FILE_PACKAGE_SAVE_RESULT)
public class FilePackageResult extends JsonBodyProtocol {

    boolean status;

    String filename;

    int currentPakage;

    String msg;

    /**开始索引*/
    long fromIndex;
    /**结束索引，传输时不包括这个字符*/
    long toIndex;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getCurrentPakage() {
        return currentPakage;
    }

    public void setCurrentPakage(int currentPakage) {
        this.currentPakage = currentPakage;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(long fromIndex) {
        this.fromIndex = fromIndex;
    }

    public long getToIndex() {
        return toIndex;
    }

    public void setToIndex(long toIndex) {
        this.toIndex = toIndex;
    }
}
