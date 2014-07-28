package com.sunsharing.sfs.dataserver.stat;

import com.sunsharing.sfs.common.netty.LongNettyClient;
import com.sunsharing.sfs.common.pro.dataserver.StatReport;
import com.sunsharing.sfs.dataserver.Config;
import com.sunsharing.sfs.dataserver.DataServerMain;
import com.sunsharing.sfs.dataserver.block.BlockCache;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by criss on 14-6-26.
 */
public class DataServerStatReport {

    static Logger logger = Logger.getLogger(DataServerMain.class);

    private DataServerStatReport()
    {

    }
    private static DataServerStatReport report = new DataServerStatReport();

    public static DataServerStatReport getInstance()
    {
        return report;
    }

    private static ScheduledExecutorService heartscheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture beeperHandle = null;
    public void startReport()
    {
        beeperHandle = heartscheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                LongNettyClient client = new LongNettyClient();
                StatReport stat = new StatReport();
                stat.setDataServerId(new Integer(Config.dataserverId));
                stat.setBlockNum(BlockCache.getBlockSize());
                stat.setIp(Config.localIp);
                stat.setMsgPort(Config.msgLisen);
                stat.setFileServerport(Config.fileServerPort);
                try
                {
                    client.requestConnectedNoRes(stat, Config.nameServerIp, new Integer(Config.nameServerPort));
                }catch (Exception e){
                    logger.error("上报状态出错",e);
                }
            }
        },5,10, TimeUnit.SECONDS);
    }
}
