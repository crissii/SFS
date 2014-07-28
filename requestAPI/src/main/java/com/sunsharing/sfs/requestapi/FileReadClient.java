package com.sunsharing.sfs.requestapi;

import com.sunsharing.sfs.common.netty.*;
import com.sunsharing.sfs.common.netty.channel.MyChannel;
import com.sunsharing.sfs.common.netty.channel.ShortChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by criss on 14-7-27.
 */
public class FileReadClient extends NettyClient {

    Logger logger = Logger.getLogger(FileReadClient.class);

    static Map<String, Object> readCache =
            new ConcurrentHashMap<String, Object>();

    BaseProtocol readFile(BaseProtocol pro,OutputStream out,String ip,int port,int timeout) throws Exception {
        checkMsgId(pro);
        //init
        clientBootstrap = new ClientBootstrap(clientSocketChannelFactory);
        ChannelPipeline pipeline = clientBootstrap.getPipeline();
//        pipeline.addLast("decoder", new ExDecode());
//        pipeline.addLast("encoder", new ExEncode());
//        pipeline.addLast("handler", new ClientHandler());
        pipeline.addLast("fileChunckDecode",new FileReadChunckDecode());
        pipeline.addLast("encoder", new ExEncode());
        pipeline.addLast("handler",new ClientHandler());

        Channel channel = connect(ip,port,pipeline);
        logger.debug("client is connected to netty server " + ip + ":" + port);
        ShortChannel shortChannel = new ShortChannel();
        shortChannel.setChannel(channel);
        logger.info(pro+":请求:"+pro.getMessageId()+"timeout:"+timeout);
        result.put(pro.getMessageId(), new ArrayBlockingQueue<BaseProtocol>(1));
        readCache.put(pro.getMessageId(),out);
        try {

            channel.write(pro);

            //等待返回
            ArrayBlockingQueue<BaseProtocol> blockingQueue = (ArrayBlockingQueue<BaseProtocol>)
                    result.get(pro.getMessageId());
            BaseProtocol result2 = blockingQueue.poll(timeout, TimeUnit.MILLISECONDS);
            logger.info("返回结果:" + result2);
            if (result2 == null) {
                logger.error("等待结果超时!");
                throw new RuntimeException("等待结果超时");
            }
            return result2;
        } catch (Exception e) {
            logger.error("请求出错！", e);
            throw e;
        } finally {
            result.remove(pro.getMessageId());
            readCache.remove(pro.getMessageId());
            if(channel instanceof ShortChannel)
            {
                if (channel != null) {
                    channel.close();
                }
                //clientBootstrap.releaseExternalResources();
            }
        }
    }

}
