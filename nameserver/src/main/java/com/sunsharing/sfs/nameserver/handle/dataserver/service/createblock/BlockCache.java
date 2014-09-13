package com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.nameserver.Config;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by criss on 14-7-3.
 */
public class BlockCache {

    static Logger logger = Logger.getLogger(BlockCache.class);

    static List<Block> blocks = new ArrayList<Block>();

    public synchronized static int genblockId()
    {
        int blockId = 0;
        try
        {
            String contextPath = Config.getContextPath();
            logger.info("contextPath:"+contextPath);
            File dataDir = new File(contextPath+"nameserver");
            if(!dataDir.isDirectory())
            {
                //创建目录
                dataDir.mkdirs();
            }
            File blocksFiles = new File(contextPath+"nameserver/blocks");

            if(!blocksFiles.isFile())
            {
                //当前没有文件
                blocksFiles.createNewFile();
                blockId = 1;
            }else
            {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(blocksFiles);
                    byte [] bytes = new byte[4];
                    inputStream.read(bytes);
                    blockId = ByteUtils.getInt(bytes)+1;
                }catch (Exception e)
                {
                    logger.error("获取blockId出错",e);
                    throw new RuntimeException("获取blockId出错");
                }finally {
                    if(inputStream!=null)
                    {
                        inputStream.close();
                    }
                }
            }
            RandomAccessFile raf1 = null;
            try
            {
                raf1 = new RandomAccessFile(blocksFiles,"rw");
                raf1.seek(0L);
                raf1.write(ByteUtils.intToBytes(blockId));
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

        }catch (Exception e)
        {
            logger.error("获取BlockId出错",e);
            throw new RuntimeException("获取blockId出错");
        }
        return blockId;
    }

    public static Block getBlockById(int blockId)
    {
        for(Block block:blocks)
        {
            if(block.getBlockId()==blockId)
            {
                return block;
            }
        }
        return null;
    }

    public static int getBlockSize()
    {
        return blocks.size();
    }


    public synchronized static Block addBlock(int blockId,int[] dataserver)
    {
        if(dataserver==null)
        {
            throw new RuntimeException("添加Block::"+blockId+"::dataserver为空");
        }
        int dataserverPad5[] = new int[5];
        for(int i=0;i<dataserver.length;i++)
        {
            dataserverPad5[i] = dataserver[i];
        }
        String contextPath = Config.getContextPath();
        RandomAccessFile raf1 = null;
        try
        {
            File dataDir = new File(contextPath+"nameserver");
            if(!dataDir.isDirectory())
            {
                //创建目录
                dataDir.mkdirs();
            }
            File blocksFiles = new File(contextPath+"nameserver/blocks");

            if(!blocksFiles.isFile())
            {
                throw new RuntimeException("无法找到nameserver");
            }

            raf1 = new RandomAccessFile(blocksFiles,"rw");
            raf1.seek(blocksFiles.length());
            //前四位blockId
            raf1.write(ByteUtils.intToBytes(blockId));
            //20位DataServer
            for(int i=0;i<dataserverPad5.length;i++)
            {
                raf1.write(ByteUtils.intToBytes(dataserverPad5[i]));
            }
            //8为当前索引
            raf1.write(ByteUtils.longToBytes(0L));
            Block block = new Block();
            for(int i=0;i<dataserver.length;i++)
            {
                block.dataServers.add(dataserver[i]);
            }
            block.blockId = blockId;
            block.addOffset(0L);
            blocks.add(block);
            return block;
        }catch (Exception e)
        {
            logger.error("写入Block:"+blockId+":",e);
            throw new RuntimeException("添加Block::"+blockId+"::dataserver:"+ Arrays.toString(dataserver));
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
    public static void updateBlockIndex(int updateBlockId,long currentIndex)
    {
        String contextPath = Config.getContextPath();
        File blocksFiles = new File(contextPath+"nameserver/blocks");
        if(blocksFiles.isFile())
        {
            RandomAccessFile raf1 = null;
            try
            {
                raf1 = new RandomAccessFile(blocksFiles,"rw");
                //前4位为BlockId
                raf1.seek(4);
                byte[] buffer = new byte[4];
                while(raf1.read(buffer)!=-1)
                {
                    int blockId = ByteUtils.getInt(buffer,0);

                    if(blockId==updateBlockId)
                    {
                        raf1.read(new byte[20]);
                        raf1.writeLong(currentIndex);
                        break;
                    }else
                    {
                        raf1.read(new byte[28]);
                    }
                    //bug 干嘛要再读28?

                }
            }catch (Exception e)
            {
                logger.error("读入block出错",e);
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
    }


    public static void initBlock()
    {
        String contextPath = Config.getContextPath();
        File blocksFiles = new File(contextPath+"nameserver/blocks");
        if(blocksFiles.isFile())
        {
            RandomAccessFile raf1 = null;
            try
            {
                raf1 = new RandomAccessFile(blocksFiles,"r");
                raf1.seek(4);
                byte[] buffer = new byte[32];
                while(raf1.read(buffer)!=-1)
                {
                    int blockId = ByteUtils.getInt(buffer,0);
                    List<Integer> dataservers = new ArrayList<Integer>();
                    for(int i=0;i<5;i++)
                    {
                        int dataserver1 = ByteUtils.getInt(buffer,4*(i+1));
                        if(dataserver1!=0)
                        {
                            dataservers.add(dataserver1);
                        }
                    }
                    long currentIndex = ByteUtils.getLong(buffer,24);
                    Block block = new Block();
                    block.dataServers = dataservers;
                    block.blockId = blockId;
                    block.setCurrentindex(currentIndex);
                    blocks.add(block);
                }
                logger.info("加载block数:"+blocks.size());
            }catch (Exception e)
            {
                logger.error("读入block出错",e);
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
    }

}
