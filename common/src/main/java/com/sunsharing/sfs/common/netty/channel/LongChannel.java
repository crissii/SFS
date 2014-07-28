package com.sunsharing.sfs.common.netty.channel;

import com.sunsharing.sfs.common.netty.NettyClient;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.HeartPro;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.Date;
import java.util.concurrent.*;

/**
 * Created by criss on 14-2-19.
 */
public class LongChannel implements MyChannel {

    Logger logger = Logger.getLogger(LongChannel.class);

    NettyClient nettyClient;

    public LongChannel(NettyClient nettyClient,String ip,String port)
    {
        this.nettyClient = nettyClient;
        this.ip = ip;
        this.port = port;
    }

    private int reconnectTimes = 0;
    private int maxIdle = 50;

    Channel channel;
    /**server端的IP*/
    String ip;
    /**server端的PORT*/
    String port;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    LinkedBlockingQueue queue = new LinkedBlockingQueue();

    long heartBeat = new Date().getTime();

    public void refreshHeartBeat()
    {
        heartBeat = new Date().getTime();
    }

    public void addMag(BaseProtocol pro)
    {
        queue.add(pro);
    }

    private ScheduledExecutorService heartscheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture beeperHandle = null;
    public void stop()
    {
        heartscheduler.shutdown();
        queue.add("~stop~");
    }
    volatile boolean connected = true;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void start()
    {
        final LongChannel othis = this;
        beeperHandle = heartscheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if(reconnectTimes>maxIdle)
                {
                    stop();
                    ClientCache.remove(othis);
                    setConnected(false);
                    return;
                }
                if((new Date().getTime()-heartBeat)>30000)
                {
                    logger.error("超过三十秒没有响应了");
                    boolean con = reconnect();
                    setConnected(con);
                    logger.info("重连接状态是:"+con);
                }else
                {
                    if(!channelWriteAble())
                    {
                        boolean con = reconnect();
                        setConnected(con);
                        logger.info("重连接状态是:"+con);
                        if(con==true)
                        {
                            HeartPro heart = new HeartPro();
                            logger.debug("添加心跳");
                            addMag(heart);
                        }
                    }else
                    {
                        setConnected(true);
                        HeartPro heart = new HeartPro();
                        logger.debug("添加心跳");
                        addMag(heart);
                    }
                }
            }
        },10,10, TimeUnit.SECONDS);

        new Thread(){
            public void run()
            {
                logger.info("添加监听");
                while(true)
                {
                    Object pro = null;
                    try
                    {
                        logger.debug("开始取出报文");
                        pro = (Object)queue.take();
                        logger.debug("结束取出报文:"+pro.getClass());
                        if(pro instanceof BaseProtocol)
                        {
                            if(channel!=null && channel.isWritable())
                            {
                                logger.debug("发送报文");
                                channel.write(pro);
                            }else
                            {

                                if(!(pro instanceof HeartPro))
                                {
                                    addMag((BaseProtocol) pro);
                                }
                                try
                                {
                                    Thread.sleep(1000);
                                }catch (Exception e2)
                                {

                                }
                            }
                        }else if(pro instanceof String && pro.equals("~stop~"))
                        {
                            try
                            {
                                channel.close();
                            }catch (Exception e)
                            {

                            }
                            break;
                        }

                    }catch (Exception e)
                    {

                    }
                }
                logger.info("退出监听");
            }
        }.start();
    }

    public boolean channelWriteAble()
    {
        try
        {
            if(channel!=null && channel.isWritable())
            {
                return true;
            }else
            {
                return false;
            }
        }catch (Exception e)
        {
            return false;
        }
    }

    private void closeChannel()
    {
        try
        {
            channel.close();
        }catch (Exception e)
        {

        }
        channel = null;
    }

    public boolean connect()
    {
        try
        {
            channel = nettyClient.connect(ip,new Integer(port));
            refreshHeartBeat();
            reconnectTimes = 0;
            return true;
        }catch (Exception e)
        {
            logger.error("连接异常:"+e.getMessage());
            return false;
        }
    }

    private boolean reconnect()
    {
        logger.info("开始重新连接,ip:"+ip+",port:"+port+",重连次数:"+reconnectTimes);
        reconnectTimes++;
        closeChannel();
        return connect();
    }


    @Override
    public ChannelFuture write(Object obj) {
        addMag((BaseProtocol)obj);
        return null;
    }

    @Override
    public void close() {

    }
}
