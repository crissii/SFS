package com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock;


import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.utils.Base58;
import com.sunsharing.sfs.common.utils.GetFileName;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.nameserver.Config;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.*;

/**
 *
 * Created by criss on 14-7-24.
 */
public class BlockInfoWrite {

    static Logger logger = Logger.getLogger(BlockInfoWrite.class);

    public long getWriteBlockInfoBefore(int blockId)
    {
        String contextPath = Config.getContextPath();
        File blockInfo = new File(contextPath+"blockinfo/"+blockId);
        if(blockInfo.isFile())
        {
            return blockInfo.length();
        }
        return 0L;
    }

    public void rollbackLen(int blockId,long len)
    {
        String contextPath = Config.getContextPath();
        File blockInfo = new File(contextPath+"blockinfo/"+blockId);
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockInfo,"rw");
            raf1.setLength(len);
        }catch (Exception e)
        {
            logger.error("写消息文件出错", e);
            throw new RuntimeException("获取blockId出错");
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }
    }

    public void loadBlockInfo()
    {
        String contextPath = Config.getContextPath();
        File blockInfoDir = new File(contextPath+"blockinfo");
        if(!blockInfoDir.isDirectory())
        {
            blockInfoDir.mkdirs();
            return;
        }
        File[] list = blockInfoDir.listFiles();
        if(list==null)
        {
            return;
        }
        long size = 0L;
        for(int i=0;i<list.length;i++)
        {
            int blockId = new Integer(list[i].getName());
            File blockInfo = new File(contextPath+"blockinfo/"+blockId);

            Block block = BlockCache.getBlockById(blockId);
            if(block==null)
            {
                continue;
            }
            if(blockInfo.isFile())
            {
                RandomAccessFile raf1 = null;
                try
                {
                    raf1 = new RandomAccessFile(blockInfo,"r");
                    byte[] buffer = new byte[12];
                    while(raf1.read(buffer)!=-1)
                    {
                        try
                        {
                        long blockIndex = raf1.readLong();
                        logger.info("blockIndex:"+blockIndex);
                        long filesize = raf1.readLong();
                        logger.info("filesize:"+filesize);
                        long extendfile = raf1.readLong();
                        logger.info("extendfile:"+extendfile);
                        block.loadFile(buffer,blockIndex,filesize,extendfile);
                        size++;
                        if(size%10000==0 && size!=0)
                        {
                            logger.info("加载10000个文件");
                        }
                        }catch (Exception e)
                        {
                            logger.error("blockInfo:"+blockInfo.getName(),e);
                            //e.printStackTrace();
                        }
                    }
                }catch (Exception e)
                {
                    logger.error("写消息文件出错", e);
                    throw new RuntimeException("获取blockId出错");
                }finally {
                    try
                    {
                        raf1.close();
                    }catch (Exception e)
                    {

                    }
                }
            }
        }
        logger.info("成功加载文件数:"+size);
    }

    public void clearLock()
    {
        String contextPath = Config.getContextPath();
        File blockInfoDir = new File(contextPath+"lock");
        if(blockInfoDir.isDirectory())
        {
            File[] files = blockInfoDir.listFiles();
            if(files!=null)
            {
                for(File f:files)
                {
                    f.delete();
                }
            }
        }
    }

    public void rollbackUpdateFile(int blockId,String fileName,long oldFileSize,long oldExtend)
    {
        String contextPath = Config.getContextPath();
        File blockInfo = new File(contextPath+"blockinfo/"+blockId);
        Block block = BlockCache.getBlockById(blockId);

        GetFileName getFileName = new GetFileName();
        //12位
        byte[] arr = getFileName.decodeFile(fileName);

        if(blockInfo.isFile())
        {
            RandomAccessFile raf1 = null;
            try
            {
                raf1 = new RandomAccessFile(blockInfo,"r");
                byte[] buffer = new byte[12];
                while(raf1.read(buffer)!=-1)
                {
                    if(new String(buffer).equals(new String(arr)))
                    {
                        long blockIndex = raf1.readLong();
                        raf1.write(ByteUtils.longToBytes(oldFileSize));
                        raf1.write(ByteUtils.longToBytes(oldExtend));
                        break;
                    }
                }
            }catch (Exception e)
            {
                logger.error("写消息文件出错", e);
                throw new RuntimeException("获取blockId出错");
            }finally {
                try
                {
                    raf1.close();
                }catch (Exception e)
                {

                }
            }
        }
    }

    public void updateBlockInfo(int blockId,String fileName,long filesize)
    {
        String contextPath = Config.getContextPath();
            File blockInfo = new File(contextPath+"blockinfo/"+blockId);
            Block block = BlockCache.getBlockById(blockId);

        GetFileName getFileName = new GetFileName();
        //12位
        byte[] arr = getFileName.decodeFile(fileName);
        byte[] fileInfo = block.getFileInfo(ByteUtils.getLong(arr,0));
            if(blockInfo.isFile())
            {
                RandomAccessFile raf1 = null;
                try
                {
                    raf1 = new RandomAccessFile(blockInfo,"rw");
                    byte[] buffer = new byte[12];
                    while(raf1.read(buffer)!=-1)
                    {
                        if(new String(buffer).equals(new String(arr)))
                        {
                            long blockIndex = raf1.readLong();
                            long oldFilesize = ByteUtils.getLong(fileInfo, 8);
                            long oldExtendSize = ByteUtils.getLong(fileInfo,16);
                            long newEx = oldExtendSize - (filesize-oldFilesize);
                            raf1.write(ByteUtils.longToBytes(filesize));
                            raf1.write(ByteUtils.longToBytes(newEx));
                            break;
                        }
                    }
                }catch (Exception e)
                {
                    logger.error("写消息文件出错", e);
                    throw new RuntimeException("获取blockId出错");
                }finally {
                    try
                    {
                        raf1.close();
                    }catch (Exception e)
                    {

                    }
                }
            }

    }

    public void writeBlockInfo(int blockId,String fileName,long index,long length,long extendlength)
    {
        logger.info("~~~~~~~~~~writeBlockInfo~~~~~~~~~~~~~~");
        String contextPath = Config.getContextPath();
        File blockInfoDir = new File(contextPath+"blockinfo");
        if(!blockInfoDir.isDirectory())
        {
            blockInfoDir.mkdirs();
        }
        File blockInfo = new File(contextPath+"blockinfo/"+blockId);
        GetFileName getFileName = new GetFileName();
        //12位
        byte[] arr = getFileName.decodeFile(fileName);

        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockInfo,"rw");
            raf1.seek(blockInfo.length());
            raf1.write(arr);
            raf1.writeLong(index);
            raf1.writeLong(length);
            raf1.writeLong(extendlength);
        }catch (Exception e)
        {
            logger.error("写消息文件出错", e);
            throw new RuntimeException("获取blockId出错");
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }

    }

    public static void main(String[]a)
    {
//        RandomAccessFile raf1 = null;
//        File f = new File("block");
//        File[] f2 = f.listFiles();
//        for(int i=0;i<f2.length;i++)
//        {
//        try
//        {
//            raf1 = new RandomAccessFile(f2[i],"rw");
//            long l = raf1.readLong();
//            System.out.println(f2[i].getName()+":"+l);
//            /**
//             * 67435051
//             * 66885885
//             * 68747054
//             */
//            //System.out.println(l+1280003+32000);
//
//            raf1.close();
//        }catch (Exception e)
//        {
//
//        }
//        }

//        RandomAccessFile raf1 = null;
//        try
//        {
//            raf1 = new RandomAccessFile("/Users/criss/Desktop/config","r");
//            raf1.seek(4);
//            byte[] buffer = new byte[32];
//            while(raf1.read(buffer)!=-1)
//            {
//                int blockId = ByteUtils.getInt(buffer,0);
//
//                long currentIndex = ByteUtils.getLong(buffer,24);
//                System.out.println(blockId+":"+currentIndex);
//
//            }
//            //long a1 = raf1.readLong();
//            //System.out.println(a1);
//            //raf1.seek(64*1024*1024L);
//            //System.out.println(raf1.readLong());
////            raf1.seek(4);
////            byte[] buffer = new byte[12];
////            //for(int i=0;i<200;i++)
////            {
////            //raf1.read(buffer);
////            //logger.info(Base58.encode(buffer))   ;
////            while(raf1.read(buffer)!=-1)
////            {
////                try
////                {
////                    long blockIndex = raf1.readLong();
////                    logger.info("blockIndex:"+blockIndex);
////                    long filesize = raf1.readLong();
////                    logger.info("filesize:"+filesize);
////                    long extendfile = raf1.readLong();
////                    logger.info("extendfile:"+extendfile);
////                    //block.loadFile(buffer,blockIndex,filesize,extendfile);
////                    //size++;
////                    //if(size%10000==0 && size!=0)
////                    {
////                        //logger.info("加载10000个文件");
////                    }
////                }catch (Exception e)
////                {
////                    //logger.error("blockInfo:"+blockInfo.getName(),e);
////                    e.printStackTrace();
////                }
////            }
////            }
//        }catch (Exception e)
//        {
//            logger.error("写消息文件出错", e);
//            throw new RuntimeException("获取blockId出错");
//        }finally {
//            try
//            {
//                raf1.close();
//            }catch (Exception e)
//            {
//
//            }
//        }


        FileReader fi = null;
        BufferedReader bs = null;
        Map result = new HashMap();
        try{
            fi = new FileReader("/Users/criss/Desktop/config");
            bs = new BufferedReader(fi);
            String ch = null;

            while((ch=bs.readLine())!=null ){
                ch = ch.trim();
                if(!StringUtils.isBlank(ch) && ch.indexOf(":")!=-1)
                {
                    String key = ch.split(":")[0];
                    String value = ch.split(":")[1];
                    if(result.get(key)!=null)
                    {
                        String v = result.get(key)+","+value;
                        result.put(key,v);
                    }else
                    {
                        result.put(key,value);
                    }
                }
                //System.out.println(ch);
                if(ch == null)
                {
                    break;
                }

            }


        }catch(Exception e){
            e.printStackTrace();
            System.out.println("文件复制失败");
        }

        for(Iterator iter = result.keySet().iterator();iter.hasNext();)
        {
            String key = (String)iter.next();
            String v = (String)result.get(key);
            String[] a1 = v.split(",");
            String a11 = a1[0];
            String a10 = a1[1];
            String a12 = a1[2];
            if(!a11.equals(a10) || !a11.equals(a12) || !a10.equals(a12))
            {
                System.out.println(key+":"+v);
            }
        }

        //System.out.println(txt);
    }


}
