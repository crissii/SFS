package com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.sfs.common.pro.dataserver.StatReport;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.nameserver.Config;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by criss on 14-7-5.
 */
public class DataServerSave {

    static Logger logger = Logger.getLogger(DataServerSave.class);

    public static synchronized void saveDataServers()
    {
        List<DataServerStat> dss = DataServerStat.dataServers;
        List<Map> datas = new ArrayList<Map>();
        for(DataServerStat ds:dss)
        {
            Map m = new HashMap();
            m.put("dataServerId",ds.getStatReport().getDataServerId());
            m.put("blocknum",ds.getStatReport().getBlockNum());
            m.put("remainingapacity",ds.getStatReport().getRemainingapacity());
            m.put("ip",ds.getStatReport().getIp());
            m.put("nameServerMsgPort",ds.getStatReport().getMsgPort());

            List<Block> blocks = ds.getCurrentblock();
            int[] blockIds = new int[blocks.size()];
            for(int i=0;i<blocks.size();i++)
            {
                blockIds[i] = blocks.get(i).getBlockId();
            }
            m.put("currentBlocks",blockIds);
            datas.add(m);
        }
        RandomAccessFile raf1 = null;
        String contextPath = Config.getContextPath();
        try
        {
            File dataDir = new File(contextPath+"nameserver");
            if(!dataDir.isDirectory())
            {
                //创建目录
                dataDir.mkdirs();
            }
            File blocksFiles = new File(contextPath+"nameserver/dataservers");

            String arrays = JSONArray.toJSONString(datas);
            byte[] bytes = arrays.getBytes("UTF-8");
            raf1 = new RandomAccessFile(blocksFiles,"rw");
            raf1.seek(0);
            raf1.write(ByteUtils.intToBytes(bytes.length));
            raf1.write(bytes);
        }catch (Exception e)
        {
            logger.error("序列化dataservers出错",e);
        }finally {
            if(raf1!=null)
            {
                try
                {
                    raf1.close();
                }catch (Exception e)
                {

                }
            }
        }

    }

    public static void initFromFile()
    {
        BlockCache.initBlock();
        String contextPath = Config.getContextPath();
        FileInputStream in = null;
        try
        {
            File blocksFiles = new File(contextPath+"nameserver/dataservers");
            if(blocksFiles.isFile())
            {
                in = new FileInputStream(blocksFiles);
                byte[] lenByte = new byte[4];
                in.read(lenByte);
                int len = ByteUtils.getInt(lenByte);
                byte[] content = new byte[len];
                in.read(content);
                JSONArray array = JSONArray.parseArray(new String(content,"UTF-8"));
                for(int i=0;i<array.size();i++)
                {
                    JSONObject obj = (JSONObject)array.get(i);
                    int dataServerId = (Integer)obj.get("dataServerId");
                    int blockNum = (Integer)obj.get("blocknum");
                    String remainingapacity = (String)obj.get("remainingapacity");
                    String ip = (String)obj.get("ip");
                    String msgPort = (String)obj.get("nameServerMsgPort");
                    JSONArray blockArray = (JSONArray)obj.get("currentBlocks");
                    List<Block> curentbocks = new ArrayList<Block>();
                    for(int j=0;j<blockArray.size();j++)
                    {
                        curentbocks.add(BlockCache.getBlockById((Integer)blockArray.get(j)));
                    }
                    StatReport sr = new StatReport();
                    sr.setDataServerId(dataServerId);
                    sr.setBlockNum(blockNum);
                    sr.setIp(ip);
                    sr.setMsgPort(msgPort);
                    sr.setRemainingapacity(remainingapacity);
                    DataServerStat.addDataServer(sr,false);
                    DataServerStat ds = DataServerStat.getDataServerStat(dataServerId);
                    ds.setCurrentblock(curentbocks);
                    ds.setOutline();
                }
            }
        }catch(Exception e)
        {
            logger.info("",e);
            throw new RuntimeException("初始化加载失败");
        }finally {
            if(in!=null)
            {
                try
                {
                    in.close();
                }catch (Exception e)
                {

                }
            }
        }
    }

}
