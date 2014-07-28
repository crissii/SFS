package com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.sfs.common.btree.BplusTree;
import com.sunsharing.sfs.common.pro.dataserver.FileSuccessUploadIndex;
import com.sunsharing.sfs.common.utils.GetFileName;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.nameserver.Config;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by criss on 14-7-3.
 */
public class Block {
    Logger logger = Logger.getLogger(Block.class);
    //private Lock lock = new ReentrantLock();
    int blockId;
    private volatile long currentindex;
    List<Integer> dataServers = new ArrayList<Integer>();

    BplusTree tree = new BplusTree(10);


    public void addDataServer(Integer i)
    {
        dataServers.add(i);
    }

    public byte[] getFileInfo(long filename)
    {
        return (byte[])tree.get(filename);
    }

    long addbeforeIndex = 0L;
    long blockInfoLength = 0L;
    public void addFile(FileSuccessUploadIndex index)
    {
        BlockInfoWrite infoWrite = new BlockInfoWrite();
        blockInfoLength = infoWrite.getWriteBlockInfoBefore(index.getBlockId());
        infoWrite.writeBlockInfo(index.getBlockId(),index.getFilename(),
                index.getBlockIndex(),index.getFileSize());
        addbeforeIndex = currentindex;
        //当前索引
        addOffset(index.getBlockIndex()+index.getFileSize());
        //12位
        GetFileName getFileName = new GetFileName();
        byte[] arr = getFileName.decodeFile(index.getFilename());
        long lfileName = ByteUtils.getLong(arr,0);
        byte[] content = new byte[16];
        System.arraycopy(ByteUtils.longToBytes(index.getBlockIndex()),0,content,0,8);
        System.arraycopy(ByteUtils.longToBytes(index.getFileSize()),0,content,8,8);
        tree.insertOrUpdate(lfileName,content);
    }

    public void loadFile(byte[] filename,long blockIndex,long filesize)
    {
        long lfileName = ByteUtils.getLong(filename,0);
        byte[] content = new byte[16];
        System.arraycopy(ByteUtils.longToBytes(blockIndex),0,content,0,8);
        System.arraycopy(ByteUtils.longToBytes(filesize),0,content,8,8);
        tree.insertOrUpdate(lfileName,content);
    }

    public void rollbackAddFile(FileSuccessUploadIndex index)
    {
        if(currentindex!=addbeforeIndex)
        {
            addOffset(addbeforeIndex);
        }

        BlockInfoWrite infoWrite = new BlockInfoWrite();
        long currentLen = infoWrite.getWriteBlockInfoBefore(index.getBlockId());
        if(currentLen>blockInfoLength)
        {
            //缩短长度
            infoWrite.rollbackLen(index.getBlockId(),blockInfoLength);
        }
        //12位
        GetFileName getFileName = new GetFileName();
        byte[] arr = getFileName.decodeFile(index.getFilename());
        long lfileName = ByteUtils.getLong(arr,0);
        tree.remove(lfileName);
    }

    public boolean isAllOnline()
    {
        if(dataServers!=null)
        {
            List<Integer> list = new ArrayList<Integer>();
            for(int i=0;i<dataServers.size();i++)
            {
                DataServerStat ds = DataServerStat.getDataServerStat(
                        dataServers.get(i));
                if(ds==null)
                {
                    logger.error("无法找到Server:"+dataServers.get(i));
                    return false;
                }
                if(!ds.isOnline())
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public String[] getOnlineDataServerIpPort()
    {
        List<String> results = new ArrayList<String>();
        if(dataServers!=null)
        {
            for(int i=0;i<dataServers.size();i++)
            {
                DataServerStat ds = DataServerStat.getDataServerStat(
                        dataServers.get(i));
                if(ds!=null && ds.isOnline())
                {
                    results.add(ds.getServerId()+":"+ds.getStatReport().getIp()+":"+ds.getStatReport().getFileServerport()+":"+
                        ds.getStatReport().getMsgPort());
                }
            }
        }
        if(results.size()==0)
        {
            throw new RuntimeException("blockId:"+blockId+"找不到在线服务器");
        }
        return results.toArray(new String[]{});
    }

    public String[] getDataServerIdIpPort()
    {
        if(dataServers!=null)
        {
            String[] ipPorts = new String[dataServers.size()];

            for(int i=0;i<dataServers.size();i++)
            {
                DataServerStat ds = DataServerStat.getDataServerStat(
                        dataServers.get(i));
                if(ds==null)
                {
                    logger.error("blockId:"+blockId+":无法找到Server:"+dataServers.get(i));
                    throw new RuntimeException("blockId:"+blockId+":无法找到Server:"+dataServers.get(i));
                }
                ipPorts[i] = ds.getServerId()+":"+ds.getStatReport().getIp()+":"+ds.getStatReport().getFileServerport()+":"+
                        ds.getStatReport().getMsgPort();
            }
            return ipPorts;
        }
        throw new RuntimeException("blockId:"+blockId+",无法找到dataServers:");
    }

    public List getOnlineServer()
    {
        if(dataServers!=null)
        {
            List<Integer> list = new ArrayList<Integer>();
            for(int i=0;i<dataServers.size();i++)
            {
                if(DataServerStat.getDataServerStat(
                        dataServers.get(i)).isOnline())
                {
                    list.add(dataServers.get(i));
                }
            }
            return list;
        }
        return null;
    }

    public void addOffset(long cur)
    {
        currentindex=cur;
        BlockCache.updateBlockIndex(blockId,currentindex);
    }

    public boolean isfull()
    {
        if(currentindex+64*1024L>=64*1024*1024L)
        {
            return true;
        }else
        {
            return false;
        }
    }

    public boolean canwrite(long filesize)
    {
        if(currentindex+filesize<64*1024*1024L)
        {
            //需要所有的server在线才让写
           if(getOnlineServer().size()==dataServers.size() && !islock())
           {
                return true;
           }else
           {
                return false;
           }
        }else
        {
            return false;
        }
    }

    public  boolean islock()
    {
        String contextPath = Config.getContextPath();
        File f1 = new File(contextPath+"lock/"+blockId);
        if(f1.exists())
        {
            return true;
        }else
        {
            return false;
        }
    }

    public synchronized void tryLock(int seconds)
    {
        String contextPath = Config.getContextPath();
        File f = new File(contextPath+"lock");
        File f1 = new File(contextPath+"lock/"+blockId);
        if(!f.isDirectory())
        {
            f.mkdirs();
            try {
                f1.createNewFile();
            }catch (Exception e)
            {
                logger.error("无法获取,block:"+blockId,e);
                throw new RuntimeException("无法创建lock");
            }
            return;
        }

        if(!f1.exists())
        {
            try {
                f1.createNewFile();
            }catch (Exception e)
            {
                logger.error("无法获取,block:"+blockId,e);
                throw new RuntimeException("无法创建lock:"+blockId);
            }
            return;
        }else
        {
            long trans = new Date().getTime() - f1.lastModified();
            if(trans>=3600*1000L)
            {
                f1.delete();
                try {
                    f1.createNewFile();
                }catch (Exception e)
                {
                    logger.error("无法获取,block:"+blockId,e);
                    throw new RuntimeException("无法创建lock:"+blockId);
                }
                return;
            }
            int l=0;
            while(f1.exists())
            {
                l++;
                try {
                    Thread.sleep(1000);
                }catch (Exception e)
                {

                }
                if(l>=seconds)
                {
                    break;
                }
            }
            //30秒以后
            if(!f1.exists())
            {
                try {
                    f1.createNewFile();
                }catch (Exception e)
                {
                    logger.error("无法获取,blockId:"+blockId,e);
                    throw new RuntimeException("无法创建lock,blockId:"+blockId);
                }
                return;
            }else
            {
                throw new RuntimeException("被其他锁占用,blockId:"+blockId);
            }
        }
    }

    public void releaseLock()
    {
        logger.info("blockId:"+blockId+"::释放锁");
        String contextPath = Config.getContextPath();
        File f = new File(contextPath+"lock/"+blockId);
        if(f.exists())
        {
            f.delete();
        }
    }


    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public long getCurrentindex() {
        return currentindex;
    }

    public void setCurrentindex(long currentindex) {
        this.currentindex = currentindex;
    }

    public void print()
    {
        System.out.println("====================");
        System.out.println("blockId:"+blockId);
        System.out.println("dataServers:"+dataServers.toString());
        System.out.println("currentindex:"+currentindex);
    }


}
