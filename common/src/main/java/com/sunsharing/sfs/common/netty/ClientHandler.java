/**
 * @(#)ClientHandler
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2014
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author ulyn
 *<br> 14-2-5 上午10:32
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.netty;

import com.sunsharing.sfs.common.netty.channel.ClientCache;
import com.sunsharing.sfs.common.netty.channel.LongChannel;
import com.sunsharing.sfs.common.netty.channel.MyChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.HeartPro;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * <pre></pre>
 * <br>----------------------------------------------------------------------
 * <br> <b>功能描述:</b>
 * <br>
 * <br> 注意事项:
 * <br>
 * <br>
 * <br>----------------------------------------------------------------------
 * <br>
 */
public class ClientHandler extends SimpleChannelHandler {
    private Logger logger = Logger.getLogger(ClientHandler.class);

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        BaseProtocol basePro = (BaseProtocol) e.getMessage();
        if(basePro instanceof HeartPro)
        {
            logger.debug("收到心跳");
            MyChannel ch = ClientCache.getChannel(e.getChannel());
            if(ch==null)
            {
                logger.warn("连接失去了没法更新心跳");
            }else
            {
                ((LongChannel)ch).refreshHeartBeat();
            }
        }else if(basePro instanceof BaseProtocol)
        {
            logger.info("收到结果:"+basePro+":"+basePro.getMessageId());
            BaseProtocol res = (BaseProtocol)basePro;
            ArrayBlockingQueue<BaseProtocol> queue = NettyClient.result.get(basePro.getMessageId());
            if (queue != null) {
                logger.info(basePro.getMessageId()+":quere:"+queue.size());
                queue.add(res);
                logger.info(basePro.getMessageId()+":quere:"+queue.size());
            }else
            {
                logger.info(basePro.getMessageId()+"queue为空");
            }
            //ctx.getChannel().close();
            MyChannel ch = ClientCache.getChannel(e.getChannel());
            if(ch==null)
            {
                logger.info("疑似短连接，关闭");
                ctx.getChannel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("处理异常", e.getCause());
        MyChannel ch = ClientCache.getChannel(e.getChannel());
        if(ch==null)
        {
            logger.info("疑似短连接，关闭");
            ctx.getChannel().close();
        }
    }
}

