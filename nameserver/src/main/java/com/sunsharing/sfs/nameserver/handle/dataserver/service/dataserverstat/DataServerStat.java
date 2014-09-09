package com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat;

import com.sunsharing.sfs.common.pro.dataserver.StatReport;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DataServer状态
 * Created by criss on 14-6-24.
 */
public class DataServerStat {
    static Logger logger = Logger.getLogger(DataServerStat.class);
    static List<DataServerStat> dataServers = new ArrayList<DataServerStat>();

    StatReport statReport =new StatReport();

    private Lock dataServerLock = new ReentrantLock();

    //获取当前的Block，需要定时器如果满了就删掉
    List<Block> currentblock = new ArrayList<Block>();

    public List<Block> getCurrentblock() {
        return currentblock;
    }

    public void setCurrentblock(List<Block> currentblock) {
        this.currentblock = currentblock;
    }

    public void addCurrentBlock(Block currentBlock)
    {
        currentblock.add(currentBlock);
        DataServerSave.saveDataServers();
    }

    public void removeCurrentBlock(Block currentBlock)
    {
        currentblock.remove(currentBlock);
        DataServerSave.saveDataServers();
    }

    private long lastUpdateTime = new Date().getTime();
    /**
     * 1 表示在线 0 表示离线
     */
    private volatile int status = 0;


    public int getServerId()
    {
        return statReport.getDataServerId();
    }


    public void refleshStat(StatReport statReport)
    {
        status = 1;
        lastUpdateTime = new Date().getTime();
        if(this.statReport==null)
        {
            this.statReport = statReport;
        }else
        {
            this.statReport.copyStatReport(statReport);
        }
        this.statReport.printStatReport();
    }

    public StatReport getStatReport()
    {
        return statReport;
    }

    public void setOutline()
    {
        status = 0;
    }

    public void tryLock()
    {
        logger.info("dataserver:"+statReport.getDataServerId()+"::获取锁");
        dataServerLock.lock();
    }

    public void releaseLock()
    {
        logger.info("dataserver:"+statReport.getDataServerId()+"::释放锁");
        dataServerLock.unlock();
    }

    public boolean isOnline()
    {
        if(status==1)
        {
            return true;
        }else
        {
            return false;
        }
    }

    public static void setOutline(int serverId)
    {
        for(DataServerStat dsS:dataServers)
        {
            if(dsS.statReport.getDataServerId()==serverId)
            {
                dsS.setOutline();
                break;
            }
        }
    }

    public static List<DataServerStat> getFreeDataServer(int lownum,int comainDataServerId)
    {
        List tmp = new ArrayList();
        for(int i=0;i<dataServers.size();i++)
        {
            if(dataServers.get(i).isOnline() &&
                    dataServers.get(i).getStatReport().getDataServerId()!=comainDataServerId)
            {
                Map m = new HashMap();
                m.put("index",i);
                m.put("score", dataServers.get(i).getStatReport().getBusyScore());
                tmp.add(m);
            }
        }
        Collections.sort(tmp,new Comparator<Map>() {
            @Override
            public int compare(Map dataServerStat, Map dataServerStat2) {
                if((Integer)dataServerStat.get("score")>
                        (Integer)dataServerStat2.get("score"))
                {
                    return 1;
                }else if((Integer)dataServerStat.get("score")<
                        (Integer)dataServerStat2.get("score"))
                {
                    return -1;
                }else
                {
                    return 0;
                }
            }
        });
        List<DataServerStat> result = new ArrayList<DataServerStat>();
        for(int i=0;i<tmp.size();i++)
        {
            if(i<lownum)
            {
                Map m = (Map)tmp.get(i);
                int index = (Integer)m.get("index");
                result.add(dataServers.get(index));
            }
        }
        return result;
    }

    public static DataServerStat getDataServerStat(int serverId)
    {
        for(DataServerStat dsS:dataServers)
        {
            if(dsS.statReport.getDataServerId()==serverId)
            {
                return dsS;
            }
        }
        return null;
    }

    public synchronized static void addDataServer(StatReport statReport,boolean isSave)
    {
        if(statReport.getDataServerId()==0)
        {
            logger.error("收到的StatReport的ServerId为0");
            return;
        }
        DataServerStat dsState = null;
        //查找在线的NameServer
        for(DataServerStat dsS:dataServers)
        {
            if(dsS.statReport.getDataServerId()==statReport.getDataServerId())
            {
                dsState = dsS;
                break;
            }
        }
        if(dsState==null)
        {
            dsState = new DataServerStat();
            dataServers.add(dsState);
            if(isSave)
            DataServerSave.saveDataServers();
        }
        dsState.refleshStat(statReport);
    }

    public static  void outlineCheck()
    {
        for(DataServerStat dsS:dataServers)
        {
            if(new Date().getTime()-dsS.lastUpdateTime>30000)
            {
                dsS.setOutline();
            }
        }
    }

    public  long getlastUpdateTime()
    {
        return lastUpdateTime;
    }




    private static ScheduledExecutorService heartscheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture beeperHandle = null;
    public static void startCheckOutline()
    {
        beeperHandle = heartscheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                //logger.error("进入检查检查在线");
                outlineCheck();
            }
        },5,10, TimeUnit.SECONDS);

        //检查已经满的Block删除
        heartscheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                logger.error("进入检查检查已经满的Block删除");
                for(Iterator iter =  dataServers.iterator();iter.hasNext();)
                {
                    DataServerStat dss = (DataServerStat)iter.next();
                    List<Block> blocks = dss.getCurrentblock();
                    for(Block block:blocks)
                    {
                        if(block.isfull())
                        {
                            dss.removeCurrentBlock(block);
                            break;
                        }
                    }
                }
            }
        },30,30, TimeUnit.SECONDS);

    }

    public static List<DataServerStat> getDataServerStats()
    {
        return dataServers;
    }



}
