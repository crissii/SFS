package com.sunsharing.sfs.dataserver.block;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndex;
import com.sunsharing.sfs.common.utils.GetFileName;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.dataserver.Config;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by criss on 14-7-2.
 */
public class BlockWrite {
    Logger logger = Logger.getLogger(BlockWrite.class);
    private BlockWrite()
    {

    }

    private static BlockWrite blockWrite = new BlockWrite();

    public static BlockWrite getInstance()
    {
        return blockWrite;
    }

    public synchronized List<Block> loadBlocks()
    {
        List<Block> blocks = new ArrayList<Block>();
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block");
        if(blockDir.isDirectory())
        {
            File[] list = blockDir.listFiles();
            if(list!=null)
            {
                for(int i=0;i<list.length;i++)
                {
                    Block block = new Block();
                    block.setBlockId(new Integer(list[i].getName()));
                    blocks.add(block);
                }

            }
        }
        return blocks;
    }


    public synchronized void writeBlock(int blockId)
    {
        //String contextPath = Path.getContextRealPath();
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block");
        if(!blockDir.isDirectory())
        {
            blockDir.mkdirs();
        }
        File file = new File(contextPath+"block/"+blockId);
        if(file.isFile())
        {
            return;
        }
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(file,"rw");
            raf1.setLength(65*1024*1024L);
            raf1.writeLong(8L);
            raf1.seek(64*1024*1024L);
            raf1.writeLong(8L);
        }catch (Exception e)
        {
            logger.error("写消息文件出错", e);
            throw new RuntimeException("写Block:"+blockId+"文件出错");
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }
    }

    public synchronized void rollbackBlock(int blockId)
    {
        //String contextPath = Path.getContextRealPath();
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        if(blockDir.isFile())
        {
            blockDir.delete();
        }

    }

    public RandomAccessFile openBlock(int blockId) throws Exception
    {
        String contextPath = Config.getBathPath();
        //File blockDir = new File(contextPath+"block/"+blockId);
        return new RandomAccessFile(contextPath+"block/"+blockId, "rw");
    }



    public File getBlockFile(int blockId)
    {
        String contextPath = Config.getBathPath();
        //File blockDir = new File(contextPath+"block/"+blockId);
        return new File(contextPath+"block/"+blockId);
    }

    public void closeBlock(RandomAccessFile rf)
    {
        if(rf!=null)
        {
            try
            {
                rf.close();
            }catch (Exception e)
            {

            }
        }
    }

    public void writePackage(int blockId,int currentPackage)
    {
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockDir,"rw");
            raf1.seek(64 * 1024 * 1024L);
            long len = raf1.readLong();
            raf1.seek(64*1024*1024L+len+currentPackage-1);
            byte[] b = new byte[1];
            b[0] = 1;
            raf1.write(b);
        }catch (Exception e)
        {
            logger.error("写消息文件出错", e);
            throw new RuntimeException("写Block:"+blockId+"文件出错");
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }
    }
    public FileRegion getFileRegion(int blockId,long packageSize,RandomAccessFile raf1)
    {
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        try
        {
            raf1 = new RandomAccessFile(blockDir,"r");
            long len = raf1.readLong();
            return new DefaultFileRegion(raf1.getChannel(), len, packageSize,true);
        }catch (Exception e)
        {
            logger.error("写消息文件出错", e);
            throw new RuntimeException("写Block:"+blockId+"文件出错");
        }
    }

    /**
     * 获取更新索引前的两个位置
     * block开始索引和64*1024*1024L的位置索引
     * @param
     * @return
     */
    public long[] getBeforeIndex(int blockId)
    {
        long [] result = new long[2];
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockDir,"r");
            long source_contentLen = raf1.readLong();
            result[0] = source_contentLen;
            raf1.seek(64*1024*1024L);
            long len = raf1.readLong();
            result[1] = len;
        }catch (Exception e)
        {
            logger.error("读block出错", e);
            throw new RuntimeException("读取blockId出错,blockId:"+blockId+":"+e.getMessage());
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }
        return result;
    }

    /**
     * 回滚索引
     * @param arr
     */
    public void rollbackIndex(int blockId,long[] arr)
    {
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockDir,"rw");
            raf1.seek(0L);
            raf1.writeLong(arr[0]);
            raf1.seek(64*1024*1024L);
            raf1.writeLong(arr[1]);
        }catch (Exception e)
        {
            logger.error("读block出错", e);
            throw new RuntimeException("读取blockId出错,blockId:"+blockId+":"+e.getMessage());
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }
    }

    /**
     * 更新索引
     * @param fs
     */
    public void updateIndex(FilePakageUpdateIndex fs)
    {
        int blockId = fs.getBlockId();
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockDir,"rw");
            long source_contentLen = raf1.readLong();
            long contentLen =source_contentLen + fs.getTotalSize();
            raf1.seek(0L);
            raf1.writeLong(contentLen);
            raf1.seek(64*1024*1024L);
            //12位文件名+8位开始索引+8位的文件长度
            long len = raf1.readLong();
            if(len+28>=65*1024*1024L)
            {
                throw new RuntimeException("文件信息已经写到超过65M");
            }
            raf1.seek(64*1024*1024L);
            raf1.writeLong(len+28);
            GetFileName fileName = new GetFileName();
            byte[] bytes = fileName.decodeFile(fs.getFileName());
            raf1.seek(64*1024*1024L+len);
            raf1.write(bytes);
            raf1.writeLong(source_contentLen);
            raf1.writeLong(fs.getTotalSize());
        }catch (Exception e)
        {
            logger.error("读block出错", e);
            throw new RuntimeException("读取blockId出错,blockId:"+blockId+":"+e.getMessage());
        }finally {
            try
            {
                raf1.close();
            }catch (Exception e)
            {

            }
        }
    }

      
    public boolean fileAllPackageSuccess(int blockId,int pakageSize)
    {
        String contextPath = Config.getBathPath();
        File blockDir = new File(contextPath+"block/"+blockId);
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(blockDir,"r");
            raf1.seek(64*1024*1024L);
            long len = raf1.readLong();
            raf1.seek(64*1024*1024L+len);
            byte[] b = new byte[pakageSize];
            raf1.read(b);
            boolean success = true;
            for(int i=0;i<b.length;i++)
            {
                if(b[i]!=1)
                {
                    success = false;
                    break;
                }
            }
            return success;

        }catch (Exception e)
        {
            logger.error("读block出错", e);
            throw new RuntimeException("读取blockId出错,blockId:"+blockId+":"+e.getMessage());
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
