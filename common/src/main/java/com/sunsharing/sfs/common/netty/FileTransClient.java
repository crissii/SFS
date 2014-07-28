package com.sunsharing.sfs.common.netty;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.netty.channel.MyChannel;
import com.sunsharing.sfs.common.netty.channel.ShortChannel;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.FileRegion;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by criss on 14-7-22.
 */
public class FileTransClient extends ShortNettyClient {

    public BaseProtocol sendFile(BaseProtocol pro,FileRegion region,
                                 String ip,int port,int timeout) throws Exception{
        Channel channel = connect(ip,port);
        logger.debug("client is connected to netty server " + ip + ":" + port);
        ShortChannel shortChannel = new ShortChannel();
        shortChannel.setChannel(channel);
        try
        {
            return sendFile(pro, region, shortChannel, timeout);
        }catch (Exception e)
        {
            logger.error("请求出错！", e);
            throw e;
        }finally {
            if (channel != null) {
                try
                {
                    channel.close();
                }catch (Exception e)
                {

                }
            }
        }
    }

    protected BaseProtocol sendFile(BaseProtocol pro,FileRegion region,MyChannel channel, int timeout) throws Exception {
        checkMsgId(pro);
        result.put(pro.getMessageId(), new ArrayBlockingQueue<BaseProtocol>(1));
        try {
            channel.write(pro);
            channel.write(region);
            //等待返回
            ArrayBlockingQueue<BaseProtocol> blockingQueue = result.get(pro.getMessageId());
            logger.error("等待超时时间:"+timeout);
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
                logger.error("关闭Channel");
                if (channel != null) {
                    channel.close();
                }
                //clientBootstrap.releaseExternalResources();
            }
        }
    }

}
