package com.sunsharing.sfs.common.netty;

import com.sunsharing.sfs.common.netty.channel.ClientCache;
import com.sunsharing.sfs.common.netty.channel.LongChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;

/**
 * Created by criss on 14-6-26.
 */
public interface Client {

    public BaseProtocol request(BaseProtocol pro, String ip, int port, int timeout) throws Exception;

    public void requestNoRes(BaseProtocol pro, String ip, int port) throws Exception;

    public void cleanResource();

}
