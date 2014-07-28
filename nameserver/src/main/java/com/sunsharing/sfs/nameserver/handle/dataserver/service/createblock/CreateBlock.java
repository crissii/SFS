package com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock;

import com.sunsharing.sfs.common.distribute.DistributeCall;
import com.sunsharing.sfs.common.distribute.DistributeTransaction;
import com.sunsharing.sfs.common.netty.Client;
import com.sunsharing.sfs.common.netty.LongNettyClient;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.nameserver.createblock.*;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 创建空白Block
 * Created by criss on 14-7-2.
 */
public class CreateBlock {

    Logger logger = Logger.getLogger(CreateBlock.class);

    /**
     *
     * @param dataServers
     * @param blockId
     * @return “success” 表示成功
     */
    public String createBlock(int[] dataServers,int blockId)
    {
        if(dataServers==null)
        {
            logger.error("入参dataServers为空，无法创建block");
            return "入参dataServers为空，无法创建block";
        }
        //校验DataServer的状态
        List<DataServerStat> onlineDataServers = new ArrayList<DataServerStat>();
        for(int i=0;i<dataServers.length;i++)
        {
            int serverId = dataServers[i];
            DataServerStat dataStat = DataServerStat.getDataServerStat(serverId);
            if(dataStat==null)
            {
                logger.error("dataServer编号:" + serverId + ",无法从DataServerStat获取到，原因可能离线");
                return "dataServer编号:" + serverId + ",无法从DataServerStat获取到，原因可能离线";
            }
            if(!dataStat.isOnline())
            {
                logger.error("dataServer编号:"+serverId+",已经离线");
                return "dataServer编号:"+serverId+",已经离线";
            }
            onlineDataServers.add(dataStat);
        }
        List<DistributeCall> calls = new ArrayList<DistributeCall>();
        for(DataServerStat dsS:onlineDataServers)
        {
            CreateBlockCall call = new CreateBlockCall(blockId,dsS);
            calls.add(call);
        }

        DistributeTransaction distributeTransaction = new DistributeTransaction();
        return distributeTransaction.excute(calls);
    }

}

class CreateBlockCall implements DistributeCall
{
    Logger logger = Logger.getLogger(CreateBlockCall.class);
    private int blockId;
    private DataServerStat dataStat;
    public CreateBlockCall(int blockId,DataServerStat dataStat)
    {
        this.blockId = blockId;
        this.dataStat = dataStat;
    }

    @Override
    public Callable<String> call() {

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                String ip = dataStat.getStatReport().getIp();
                String port = dataStat.getStatReport().getMsgPort();
                Client client = new LongNettyClient();
                BlockCreateRequest blockCreateRequest = new BlockCreateRequest();
                blockCreateRequest.setBlockId(blockId);
                BaseProtocol baseProtocol =
                        client.request(blockCreateRequest, ip, new Integer(port), 5000);
                if(baseProtocol instanceof BlockCreateResult)
                {
                    return "success";
                }else if(baseProtocol instanceof BlockCreateError)
                {
                    BlockCreateError error = (BlockCreateError)baseProtocol;
                    logger.error(dataStat.getStatReport().getDataServerId()+"" +
                            ":创建Block出错:blockId为"+blockId+":"+error.getErrorMsg());
                    return dataStat.getStatReport().getDataServerId()+"" +
                            ":创建Block出错:blockId为"+blockId+":"+error.getErrorMsg();
                }
                return "success";
            }
        };
    }

    @Override
    public Runnable rollback() {
        return new Runnable() {
            @Override
            public void run() {
                String ip = dataStat.getStatReport().getIp();
                String port = dataStat.getStatReport().getMsgPort();
                Client client = new LongNettyClient();
                BlockCreateRollback blockCreateRequest = new BlockCreateRollback();
                blockCreateRequest.setBlockId(blockId);
                try
                {
                    client.requestNoRes(blockCreateRequest, ip, new Integer(port));
                }catch (Exception e)
                {
                    logger.error(dataStat.getStatReport().getDataServerId()+"" +
                            ":回滚Block出错:blockId为"+blockId+":",e);
                }
            }
        };
    }

    @Override
    public Runnable commit() {
        return new Runnable() {
            @Override
            public void run() {
                String ip = dataStat.getStatReport().getIp();
                String port = dataStat.getStatReport().getMsgPort();
                Client client = new LongNettyClient();
                BlockCreateCommit blockCreateRequest = new BlockCreateCommit();
                blockCreateRequest.setBlockId(blockId);
                try
                {
                    client.requestNoRes(blockCreateRequest, ip, new Integer(port));
                }catch (Exception e)
                {
                    logger.error(dataStat.getStatReport().getDataServerId()+"" +
                            ":提交事务Block出错:blockId为"+blockId+":",e);
                }
            }
        };
    }
}
