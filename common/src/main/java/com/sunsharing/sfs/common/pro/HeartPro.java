package com.sunsharing.sfs.common.pro;

import com.sunsharing.sfs.common.netty.channel.ServerCache;
import com.sunsharing.sfs.common.netty.channel.ServerChannel;
import com.sunsharing.sfs.common.pro.ano.Protocol;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-2-19.
 */
@Protocol(action = Constant.HEART_BEAT)
public class HeartPro extends BaseProtocol {
    Logger logger = Logger.getLogger(HeartPro.class);
    @Override
    protected int getRealBodyLength() {
        return 0;
    }

    @Override
    public ChannelBuffer generate() {
        setAction(Constant.HEART_BEAT);
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        byte[] header = new byte[1];
        header[0] = Constant.HEART_BEAT;
        buffer.writeBytes(header);
        return buffer;
    }

    @Override
    public BaseProtocol createFromChannel(ChannelBuffer buffer,BaseProtocol pro) {
        if (buffer.readableBytes() < 1) {
            return null;
        }
        pro.action = buffer.readByte();
        return pro;
    }

    @Override
    public void handler(Channel channel,BaseProtocol pro) {
        logger.debug("收到心跳请求...");
        ServerChannel sc = ServerCache.getChannel(channel);
        if(sc!=null)
        {
            sc.refreshHeartBeat();
        }else
        {
            logger.warn("无法从缓存中找到Channel:"+channel.getRemoteAddress());
        }
        HeartPro heart = new HeartPro();
        channel.write(heart);
    }
}
