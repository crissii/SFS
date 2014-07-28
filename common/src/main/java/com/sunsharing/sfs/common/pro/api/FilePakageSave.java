package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;

/**
 * 文件分包传送指令
 * 发送方：客户端
 * User: criss
 * Date: 13-7-12
 * Time: 上午9:50
 * To change this template use File | Settings | File Templates.
 */
@Protocol(action = Constant.FILE_PACKAGE_SAVE)
public class FilePakageSave extends JsonBodyProtocol {
    /**当前包，以1开始计数*/
    int currentPakage;
    /**开始索引*/
    long fromIndex;
    /**结束索引，传输时不包括这个字符*/
    long toIndex;
    /**md5校验，暂时没有实现*/
    String md5="";
    /**文件最后修改时间*/
    long lastmodify;
    /**总包数*/
    int totalPakage;
    /**文件名*/
    String fileName;
    /**文件的总大小totalSize*/
    long totalSize;

    int blockId;

    int leaderServer;

    String[] dataservers;

    int currentDataServer;



    /**文件处理是否成功*/
    boolean fileDoSuccess = false;

    String errorMsg = "";

    public int getCurrentDataServer() {
        return currentDataServer;
    }

    public void setCurrentDataServer(int currentDataServer) {
        this.currentDataServer = currentDataServer;
    }

    public FilePakageSave clone()
    {
        FilePakageSave fs = new FilePakageSave();
        String[] fieldNames = fs.getFiledName();
        for(int i=0;i<fieldNames.length;i++)
        {
            setFieldValueByName(fs,fieldNames[i],this.getFieldValueByName(fieldNames[i]));
        }
        fs.setFileDoSuccess(false);
        fs.setErrorMsg("");
        fs.setCurrentWrite(0L);
        fs.setMessageId(StringUtils.generateUUID());
        return fs;
    }

    public void setCurrentDataServer(String ip)
    {
        for(int i=0;i<dataservers.length;i++)
        {
            String str = dataservers[i];
            String [] arr = str.split(",");
            if(ip.equals(arr[1]))
            {
                setCurrentDataServer(arr[0]);
            }
        }
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isFileDoSuccess() {
        return fileDoSuccess;
    }

    public void setFileDoSuccess(boolean fileDoSuccess) {
        this.fileDoSuccess = fileDoSuccess;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public int getLeaderServer() {
        return leaderServer;
    }

    public void setLeaderServer(int leaderServer) {
        this.leaderServer = leaderServer;
    }

    public String[] getDataservers() {
        return dataservers;
    }

    public void setDataservers(String[] dataservers) {
        this.dataservers = dataservers;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalPakage() {
        return totalPakage;
    }

    public void setTotalPakage(int totalPakage) {
        this.totalPakage = totalPakage;
    }

    public int getCurrentPakage() {
        return currentPakage;
    }

    public void setCurrentPakage(int currentPakage) {
        this.currentPakage = currentPakage;
    }

    public long getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(long fromIndex) {
        this.fromIndex = fromIndex;
    }

    public long getToIndex() {
        return toIndex;
    }


    public void setToIndex(long toIndex) {
        this.toIndex = toIndex;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getLastmodify() {
        return lastmodify;
    }

    public void setLastmodify(long lastmodify) {
        this.lastmodify = lastmodify;
    }


    public boolean isLeader(int currentDataServer)
    {
        if(leaderServer == currentDataServer)
        {
            return true;
        }else
        {
            return false;
        }
    }

    //    @Override
//    public void handler(Channel channel) {
//        boolean b = lastCheck();
//        if(b)
//        {
//           //成功完成
//           FilePakageSaveResult result = new FilePakageSaveResult();
//           result.filePath = this.filePath;
//           channel.write(result).awaitUninterruptibly();
//            try
//            {
//                channel.close();
//            }catch (Exception e)
//            {
//
//            }
//        }else
//        {
//           //文件已经传送完成,还没结束
//           try
//           {
//                channel.close();
//           }catch (Exception e)
//           {
//
//           }
//        }
//    }


    /**判断是否所有的包都已经结束*/
//    private boolean  lastCheck()
//    {
//        Lock lock = (Lock) Cache.getLock(getServerTempFileName());
//        //获取文件的锁
//        lock.lock();
//        RandomAccessFile raf = null;
//        try
//        {
//            VisualNode vn = Nodes.getInstance().getVNodeByTime(getLastmodify().substring(0,8));
//            String path = vn.getPath();
//            if(!path.endsWith("/"))
//            {
//                path += "/";
//            }
//            int packageSize = getTotalPakage();
//            raf = new RandomAccessFile(path+getServerTempFileName()+".log", "rw");
//            raf.seek(FileSend.beginFileSize+getCurrentPakage()-1);
//            byte[] b = new byte[1];
//            b[0] = '1';
//            raf.write(b);
//            b = new byte[packageSize];
//            raf.seek(FileSend.beginFileSize);
//            raf.read(b);
//            boolean success = true;
//            int allready = 0;
//            for(int i=0;i<packageSize;i++)
//            {
//                if(b[i]==0)
//                {
//                    success = false;
//                }else
//                {
//                    allready++;
//                }
//            }
//            logger.info("成功接收文件包"+fileName+"："+allready+"/"+packageSize);
//            if(success)
//            {
//                logger.info("成功完成文件："+fileName);
//                //成功完成
//                Cache.removeLock(getServerTempFileName());
//                raf.close();
//                new File(path+getServerTempFileName()+".log").delete();
//                //fs.get
//                String d = getLastmodify().substring(0,8);
//                File datePath = new File(path+d);
//                if(!datePath.exists())
//                {
//                    datePath.mkdirs();
//                }
//                File dest = new File(path+d+"/"+fileName);
//                if(dest.exists())
//                {
//                    dest.delete();
//                }
//                FileChannel fcin = null;
//                FileChannel fcout = null;
//                try
//                {
//                    fcin = new FileInputStream(path+getServerTempFileName()).getChannel();
//                    fcout = new FileOutputStream(dest).getChannel();
//                    long size = fcin.size();
//                    fcin.transferTo(0, size, fcout);
//                }catch (Exception e)
//                {
//                    logger.error("拷贝出错",e);
//                    throw new RuntimeException("拷贝出错");
//                }finally {
//                    if(fcin!=null)
//                    {
//                        try
//                        {
//                            fcin.close();
//                        }catch (Exception e)
//                        {
//
//                        }
//                    }
//                    if(fcout!=null)
//                    {
//                        try
//                        {
//                            fcout.close();
//                        }catch (Exception e)
//                        {
//
//                        }
//                    }
//                }
//                File tt = new File(path+getServerTempFileName());
//                if(tt.exists())
//                {
//                    tt.delete();
//                }
//
//                return true;
//            }else
//            {
//                return false;
//            }
//
//        }catch (Exception e)
//        {
//            logger.error("异常",e);
//        }finally {
//            lock.unlock();
//            try
//            {
//                raf.close();
//            }catch (Exception e)
//            {
//
//            }
//        }
//        return false;
//    }


    public long getTotalSize() {
        return totalSize;
    }public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public volatile long currentWrite=0;

    /**
     * 计算当前传了多少数据
     * @param lon
     */
    public synchronized void increate(long lon)
    {
        currentWrite+=lon;
    }

    public long getCurrentWrite() {
        return currentWrite;
    }

    public void setCurrentWrite(long currentWrite) {
        this.currentWrite = currentWrite;
    }

    /**
     * 获取当前包的总大小
     * @return
     */
    public long getPakagePerSize()
    {
        return toIndex-fromIndex;
    }



    /**
     * 还剩下多少数据
     * @return
     */
    public long getLastLen()
    {
        return  getPakagePerSize()-currentWrite;
    }

    /**
     * 是否已经传送完成
     * @return
     */
    public synchronized boolean isLast()
    {
        if(currentWrite==getPakagePerSize())
        {
            return true;
        }else
        {
            return false;
        }
    }

}
