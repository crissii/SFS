package com.sunsharing.sfs.common.pro;

import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-6-27.
 */
public interface Handle  {

    public void handler(Channel channel,BaseProtocol pro);

}
