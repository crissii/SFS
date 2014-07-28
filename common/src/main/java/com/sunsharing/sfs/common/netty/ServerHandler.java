package com.sunsharing.sfs.common.netty;

import com.sun.xml.internal.rngom.parse.host.Base;
import com.sunsharing.sfs.common.netty.channel.*;
import com.sunsharing.sfs.common.netty.channel.ServerChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.HeartPro;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: ulyn
 * Date: 13-7-12
 * Time: 下午5:56
 * To change this template use File | Settings | File Templates.
 */
public    class ServerHandler extends SimpleChannelHandler {
    private static final Logger logger = Logger.getLogger(ServerHandler.class);
    static ExecutorService service =  Executors.newCachedThreadPool();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        final BaseProtocol basePro = (BaseProtocol) e.getMessage();
        final ChannelHandlerContext content = ctx;
        if(basePro instanceof HeartPro)
        {
            basePro.handler(ctx.getChannel(),basePro);
        }else
        if(basePro instanceof BaseProtocol)
        {
            logger.info("收到请求：" + basePro);
            service.execute(new Runnable() {
                @Override
                public void run() {
                    handle(content.getChannel(),basePro);
                }
            });

        }
    }

    public void handle(Channel ch, BaseProtocol base) {
        base.handler(ch,base);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("处理异常", e.getCause());
        e.getCause().printStackTrace();
        //e.getChannel().close();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("channelConnected");

    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("channelClosed");
        //删除通道
        //XLServer.allChannels.remove(e.getChannel());
        ServerCache.removeChannel(ctx.getChannel());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("channelDisconnected");
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("channelOpen");
        //增加通道
        //XLServer.allChannels.add(e.getChannel());
        ServerCache.addChannel(ctx.getChannel());
    }
}
