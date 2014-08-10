package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * 文件发送请求
 * Created by criss on 14-7-3.
 */
@Protocol(action = Constant.WRITE_REQUEST)
public class WriteRequest extends JsonBodyProtocol {
    long filesize = 0;
    /**
     * 扩展大小,用于更新
     */
    long extendfilesize = 0;

    String filename="";

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public long getExtendfilesize() {
        return extendfilesize;
    }

    public void setExtendfilesize(long extendfilesize) {
        this.extendfilesize = extendfilesize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
