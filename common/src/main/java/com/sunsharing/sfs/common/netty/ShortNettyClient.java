package com.sunsharing.sfs.common.netty;

import com.sunsharing.sfs.common.netty.channel.ShortChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 * Created by criss on 14-2-19.
 */
public class ShortNettyClient extends NettyClient implements Client {

    public BaseProtocol request(BaseProtocol pro, String ip, int port, int timeout) throws Exception {

        Channel channel = connect(ip,port);
        logger.debug("client is connected to netty server " + ip + ":" + port);
        ShortChannel shortChannel = new ShortChannel();
        shortChannel.setChannel(channel);
        try
        {
            return getResult(pro,shortChannel,timeout);
        }catch (Exception e)
        {
            logger.error("请求出错！", e);
            throw e;
        }finally {
            if (channel != null) {
                channel.close();
            }
        }
    }

    @Override
    public void requestNoRes(BaseProtocol pro, String ip, int port) throws Exception {
        Channel channel = connect(ip,port);
        logger.debug("client is connected to netty server " + ip + ":" + port);
        ShortChannel shortChannel = new ShortChannel();
        shortChannel.setChannel(channel);
        ChannelFuture future = null;
        try
        {
            future = getNoResult(pro,shortChannel);
        }catch (Exception e)
        {
            logger.error("请求出错！", e);
            throw e;
        }finally {
            if (channel != null) {
                try
                {
                    future.awaitUninterruptibly();
                    channel.close();
                    //channel.disconnect();
                    //channel.close();
                }catch (Exception e)
                {

                }
            }
        }
    }

}
