package com.sunsharing.sfs.common.netty;

import com.sunsharing.sfs.common.netty.channel.ClientCache;
import com.sunsharing.sfs.common.netty.channel.LongChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;

/**
 * Created by criss on 14-2-19.
 */
public class LongNettyClient extends NettyClient implements Client {
    public BaseProtocol request(BaseProtocol pro, String ip, int port, int timeout) throws Exception {
        LongChannel longChannel = ClientCache.getChannel(this, ip, port + "");
        return getResult(pro,longChannel,timeout);
    }

    @Override
    public void requestNoRes(BaseProtocol pro, String ip, int port) throws Exception {
        LongChannel longChannel = ClientCache.getChannel(this, ip, port + "");
        getNoResult(pro,longChannel);
    }

    public void requestConnectedNoRes(BaseProtocol pro, String ip, int port)throws Exception{
        LongChannel longChannel = ClientCache.getChannel(this, ip, port + "");
        if(longChannel.isConnected())
        {
            getNoResult(pro,longChannel);
        }
    }
}
