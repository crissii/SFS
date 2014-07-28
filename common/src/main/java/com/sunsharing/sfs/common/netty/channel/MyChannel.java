package com.sunsharing.sfs.common.netty.channel;

import org.jboss.netty.channel.ChannelFuture;

/**
 * Created by criss on 14-2-19.
 */
public interface MyChannel {

    ChannelFuture write(Object obj);

    void close();

}
