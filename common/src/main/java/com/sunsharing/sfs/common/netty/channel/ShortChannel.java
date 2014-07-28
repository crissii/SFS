package com.sunsharing.sfs.common.netty.channel;


import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 * Created by criss on 14-2-19.
 */
public class ShortChannel implements MyChannel {

    Channel channel;

    public ChannelFuture write(Object obj)
    {
        return channel.write(obj);
    }

    @Override
    public void close() {
        channel.close();
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
