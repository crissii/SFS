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

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

}
