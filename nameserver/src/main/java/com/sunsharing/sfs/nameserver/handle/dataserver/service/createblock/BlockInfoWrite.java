package com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock;


import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.sfs.common.utils.GetFileName;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.nameserver.Config;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.RandomAccessFile;

/**
 *
 * Created by criss on 14-7-24.
 */
public class BlockInfoWrite {

    Logger logger = Logger.getLogger(BlockInfoWrite.class);

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
            System.out.println("filesize:"+blockInfo.length());
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
                        long filesize = raf1.readLong();
                        long extendfile = raf1.readLong();
                        block.loadFile(buffer,blockIndex,filesize,extendfile);
                        size++;
                        if(size%10000==0 && size!=0)
                        {
                            logger.info("加载10000个文件");
                        }
                        }catch (Exception e)
                        {
                            //logger.error("blockInfo:"+blockInfo.getName(),e);
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

}
