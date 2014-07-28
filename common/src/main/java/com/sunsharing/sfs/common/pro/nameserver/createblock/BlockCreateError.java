package com.sunsharing.sfs.common.pro.nameserver.createblock;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * nameServer返回错误
 * Created by criss on 14-6-30.
 */
@Protocol(action = Constant.BLOCK_CREATE_RESULT_ERROR)
public class BlockCreateError extends JsonBodyProtocol {

    String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
