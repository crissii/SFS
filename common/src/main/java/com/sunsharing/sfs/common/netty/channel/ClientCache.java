package com.sunsharing.sfs.common.netty.channel;

import com.sunsharing.sfs.common.netty.NettyClient;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by criss on 14-2-19.
 */
public class ClientCache {

    static Logger logger = Logger.getLogger(ClientCache.class);

    public static List<LongChannel> channels = new ArrayList<LongChannel>();

    public static synchronized LongChannel getChannel(NettyClient nettyClient,String ip,String port)
    {
        logger.debug("当前连接数:"+channels.size());
        for(LongChannel ch:channels)
        {
            if(ch.getIp().equals(ip) && ch.getPort().equals(port))
            {
                return ch;
            }
        }
        logger.info("找不到Channel,重新连接");
        LongChannel channel = new LongChannel(nettyClient,ip,port);
        channel.connect();
        channel.start();
        channels.add(channel);
        logger.info("添加后连接数:"+channels.size());
        return channel;
    }

    public static synchronized void remove(LongChannel longChannel)
    {
        channels.remove(longChannel);
        logger.info("删除后连接数:"+channels.size());
    }

    public static LongChannel getChannel(Channel channel)
    {
        for(LongChannel ch:channels)
        {
            if(ch.getChannel()==channel)
            {
                return ch;
            }
        }
        return null;
    }

}
