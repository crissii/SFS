package com.sunsharing.sfs.nameserver.handle.api;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.api.WriteRequest;
import com.sunsharing.sfs.common.pro.api.WriteRequestResult;
import com.sunsharing.sfs.nameserver.Config;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.CreateBlock;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.Iterator;
import java.util.List;

/**
 * 写请求
 * Created by criss on 14-7-3.
 */
@HandleAno(action = Constant.WRITE_REQUEST)
public class WriteRequestHandle implements Handle {
    Logger logger = Logger.getLogger(WriteRequestHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {
        WriteRequest writeRequest = (WriteRequest)baseProtocol;
        boolean successLock = false;
        //1.获取最空闲的DataServer
        List<DataServerStat> dataServers = DataServerStat.getFreeDataServer(1,0);

        ChannelFuture future = null;

        if(dataServers.size()==0)
        {
            WriteRequestResult result = new WriteRequestResult();
            result.setMessageId(writeRequest.getMessageId());
            result.setStatus(false);
            result.setErrorMsg("取不到在线的DataServer");
            future = channel.write(result);
            return;
        }
        DataServerStat dataServer = dataServers.get(0);
        //2.获取dataServer写的锁
        dataServer.tryLock();
        int writeBlockId = 0;
        try
        {
            List<Block> currentBlocks = dataServer.getCurrentblock();
            boolean createBlock = true;
            try
            {
                for(int i=0;i<currentBlocks.size();i++)
                {
                    if(currentBlocks.get(i).canwrite(writeRequest.getFilesize()))
                    {
                        createBlock = false;
                        writeBlockId = currentBlocks.get(i).getBlockId();
                    }
                }
            }catch (Exception e)
            {
                logger.error("获取block出错",e);
                throw new RuntimeException("获取block出错");
            }

            //3.需要新建Block
            if(createBlock)
            {
                List<DataServerStat> dss =
                        DataServerStat.getFreeDataServer
                                (new Integer(Config.lowestBackNum)-1,dataServer.getServerId());
                if(dss.size()<new Integer(Config.lowestBackNum)-1)
                {
                    throw new RuntimeException("在线服务器服务达到:"+Config.lowestBackNum+"台");
                }
                for(DataServerStat ds:dss)
                {
                    ds.tryLock();
                }
                try
                {
                    CreateBlock cb = new CreateBlock();
                    int blockId = BlockCache.genblockId();
                    int [] dsIds = new int[new Integer(Config.lowestBackNum)];
                    int index = 0;
                    for(Iterator iter = dss.iterator();iter.hasNext();)
                    {
                        DataServerStat tmp = (DataServerStat)iter.next();
                        dsIds[index++] = tmp.getServerId();
                    }
                    dsIds[index] = dataServer.getServerId();
                    String result = cb.createBlock(dsIds,blockId);
                    if(!"success".equals(result))
                    {
                        throw new RuntimeException("无法创建block:"+blockId+"::"+result);
                    }
                    //添加block
                    Block block = BlockCache.addBlock(blockId,dsIds);
                    block.tryLock(5);
                    writeBlockId = block.getBlockId();
                    for(DataServerStat ds:dss)
                    {
                        ds.addCurrentBlock(block);
                    }
                    dataServer.addCurrentBlock(block);
                }catch (Exception e)
                {
                    throw e;
                }finally {
                    for(DataServerStat ds:dss)
                    {
                        ds.releaseLock();
                    }
                }

            }else
            {
                BlockCache.getBlockById(writeBlockId).tryLock(5);
            }
            //4.创建完block,锁住block,通知开始传输
            Block block = BlockCache.getBlockById(writeBlockId);
            boolean isAllOnline = block.isAllOnline();
            if(!isAllOnline)
            {
                WriteRequestResult writeRequestResult = new WriteRequestResult();
                writeRequestResult.setMessageId(writeRequest.getMessageId());
                writeRequestResult.setStatus(false);
                writeRequestResult.setErrorMsg("blockId:" + writeBlockId + ",不是所有的DataServer都在线");
                future = channel.write(writeRequestResult);
                return;
            }

            //block.tryLock(5);
            //成功处理锁定
            String [] dataServerPort = block.getDataServerIdIpPort();
            WriteRequestResult result = new WriteRequestResult();
            result.setMessageId(writeRequest.getMessageId());
            result.setStatus(true);
            result.setBlockId(writeBlockId);
            result.setLeaderDataserverIp(dataServer.getStatReport().getIp());
            result.setLenderFileServerPort(dataServer.getStatReport().getFileServerport());
            result.setLeaderDataServer(dataServer.getServerId());
            result.setFilesize(writeRequest.getFilesize());
            result.setDataServer(dataServerPort);
            channel.write(result);
            successLock = true;
        }catch (Exception e)
        {
            logger.error("...",e);
            WriteRequestResult result = new WriteRequestResult();
            result.setMessageId(writeRequest.getMessageId());
            result.setStatus(false);
            result.setErrorMsg(e.getMessage());
            future = channel.write(result);
            return;
        }finally
        {
            if(future!=null)
            {
                try
                {
                    future.awaitUninterruptibly();
                    channel.close();
                }catch (Exception e)
                {

                }
            }

            dataServer.releaseLock();
            //当block是异常的时候解锁
            if(writeBlockId!=0 && !successLock)
            {
                Block block = BlockCache.getBlockById(writeBlockId);
                if(block!=null)
                {
                    block.releaseLock();
                }
            }

        }




    }
}
