/**
 * @(#)NettyClient
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2014
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author ulyn
 *<br> 14-2-5 上午10:19
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.netty;

import com.sun.xml.internal.rngom.parse.host.Base;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.netty.channel.MyChannel;
import com.sunsharing.sfs.common.netty.channel.ShortChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

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
public abstract class NettyClient {
    Logger logger = Logger.getLogger(NettyClient.class);

    public static final int CONNECT_TIMEOUT = 5000;

    public static Map<String, ArrayBlockingQueue<BaseProtocol>> result =
            new ConcurrentHashMap<String, ArrayBlockingQueue<BaseProtocol>>();

    // 因ChannelFactory的关闭有DirectMemory泄露，采用静态化规避
    // https://issues.jboss.org/browse/NETTY-424
    protected final static ChannelFactory clientSocketChannelFactory = new
            NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    protected ClientBootstrap clientBootstrap;

    public Channel connect(String ip,int port) throws Exception{
        clientBootstrap = new ClientBootstrap(clientSocketChannelFactory);
        ChannelPipeline pipeline = clientBootstrap.getPipeline();
        pipeline.addLast("decoder", new ExDecode());
        pipeline.addLast("encoder", new ExEncode());
        pipeline.addLast("handler", new ClientHandler());
        return connect(ip,port,pipeline);
    }

    public Channel connect(String ip, int port,ChannelPipeline pipeline) throws Exception {
        clientBootstrap.setOption("tcpNoDelay", true);
        clientBootstrap.setOption("keepAlive", true);
        clientBootstrap.setOption("connectTimeoutMillis", CONNECT_TIMEOUT);
        clientBootstrap.setOption("reuseAddress", true); //注意child前缀
        ChannelFuture future = clientBootstrap.connect(new InetSocketAddress(ip, new Integer(port)));
        final CountDownLatch channelLatch = new CountDownLatch(1);
        final String ipFinal = ip;
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (cf.isSuccess()) {
//                    channel = cf.getChannel();
                    channelLatch.countDown();
                } else {
//                    throw new RpcException(RpcException.CONNECT_EXCEPTION, "client failed to connect to server "
//                            + ipFinal + ", error message is:" + cf.getCause() == null ? "unknown" : cf.getCause().getMessage(), cf.getCause());
                }
            }
        });
        channelLatch.await(5, TimeUnit.SECONDS);
        if (future.isSuccess()) {
            return future.getChannel();
        } else {
            throw new RuntimeException("client failed to connect to server "
                    + ipFinal + ", port:" + port + "error message is:");
        }

    }

    public  void cleanResource()
    {
        if(clientBootstrap!=null)
        {
            clientBootstrap.releaseExternalResources();
        }else
        {
            clientSocketChannelFactory.releaseExternalResources();
        }
    }

    protected ChannelFuture getNoResult(BaseProtocol pro, MyChannel channel) throws Exception{
        checkMsgId(pro);
        return channel.write(pro);
    }

    protected BaseProtocol getResult(BaseProtocol pro, MyChannel channel, int timeout) throws Exception {
        checkMsgId(pro);
        logger.info(pro+":请求:"+pro.getMessageId()+"timeout:"+timeout);
        result.put(pro.getMessageId(), new ArrayBlockingQueue<BaseProtocol>(1));
        try {

            channel.write(pro);

            //等待返回
            ArrayBlockingQueue<BaseProtocol> blockingQueue = result.get(pro.getMessageId());
            BaseProtocol result = blockingQueue.poll(timeout, TimeUnit.MILLISECONDS);
            logger.info("返回结果:" + result);
            if (result == null) {
                logger.error("等待结果超时!");
                throw new RuntimeException("等待结果超时");
            }
            return result;
        } catch (Exception e) {
            logger.error("请求出错！", e);
            throw e;
        } finally {
            result.remove(pro.getMessageId());
            if(channel instanceof ShortChannel)
            {
                if (channel != null) {
                    channel.close();
                }
                //clientBootstrap.releaseExternalResources();
            }
        }
    }

    protected void checkMsgId(BaseProtocol pro)
    {
        String name = pro.getClass().getName().toLowerCase();
        if(name.indexOf("result")!=-1)
        {
            //结果报文
            if(StringUtils.isBlank(pro.getMessageId()))
            {
                throw new RuntimeException("消息ID不能为空");
            }
        }else
        {
            //请求报文
            if(StringUtils.isBlank(pro.getMessageId()))
            {
                pro.setMessageId(StringUtils.generateUUID());
            }
        }

    }



}

