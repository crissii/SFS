package com.sunsharing.sfs.common.pro.nameserver.createblock;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * nameServer返回blockId
 * Created by criss on 14-6-30.
 */
@Protocol(action = Constant.BLOCK_CREATE_RESULT)
public class BlockCreateResult extends JsonBodyProtocol {

}
