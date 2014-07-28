package com.sunsharing.sfs.common.pro.dataserver;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-24.
 */
@Protocol(action = Constant.FILE_PACKAGE_UPDATE_INDEX_RESULT)
public class FilePakageUpdateIndexResult extends JsonBodyProtocol {

    boolean status;

    String msg;

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
