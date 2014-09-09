package com.sunsharing.sfs.common.pro.dirmain;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-9-5.
 */
@Protocol(action = Constant.DIR_MAIN)
public class DirMain extends JsonBodyProtocol {
    String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
