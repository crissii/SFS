package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-27.
 */
@Protocol(action = Constant.DATA_READ_RESULT)
public class DataReadResult  extends JsonBodyProtocol {

    boolean status;

    String msg;

    long totallen;

    long currentRead = 0L;

    boolean doSuccess = true;

    String doError = "";

    public String getDoError() {
        return doError;
    }

    public void setDoError(String doError) {
        this.doError = doError;
    }

    public boolean isDoSuccess() {
        return doSuccess;
    }

    public void setDoSuccess(boolean doSuccess) {
        this.doSuccess = doSuccess;
    }

    public long getTotallen() {
        return totallen;
    }

    public void setTotallen(long totallen) {
        this.totallen = totallen;
    }

    public long getCurrentRead() {
        return currentRead;
    }

    public void setCurrentRead(long currentRead) {
        this.currentRead = currentRead;
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

    public void addOffSet(long currentRead)
    {
        this.currentRead+=currentRead;
    }

    public boolean islast()
    {
        if(currentRead==getTotallen())
        {
            return true;
        }else
        {
            return false;
        }
    }

}
