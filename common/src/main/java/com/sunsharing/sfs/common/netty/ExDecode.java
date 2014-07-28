package com.sunsharing.sfs.common.netty;


import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.ProFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;


/**
 * Created with IntelliJ IDEA.
 * User: criss
 * Date: 13-7-12
 * Time: 下午3:48
 * To change this template use File | Settings | File Templates.
 */
public class ExDecode extends FrameDecoder {
    Logger logger = Logger.getLogger(ExDecode.class);

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        logger.debug("decode:" + buffer);
        BaseProtocol pro = ProFactory.createPro(buffer);
        logger.info("decode:" + pro);
        return pro;
    }
}
