package com.sunsharing.sfs.dataserver.server.file;

import com.sunsharing.sfs.common.distribute.DistributeCall;
import com.sunsharing.sfs.common.netty.FileTransClient;
import com.sunsharing.sfs.common.netty.ShortNettyClient;
import com.sunsharing.sfs.common.pro.api.FilePackageResult;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.dataserver.Config;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.FileRegion;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;

/**
 * Created by criss on 14-7-22.
 */
public class FilePakageThread implements DistributeCall {

    Logger logger = Logger.getLogger(FilePakageThread.class);

    FilePakageSave fps;
    String ip;
    String port;

    public FilePakageThread(FilePakageSave fps,String ip,String port)
    {
        this.fps = fps;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public Callable<String> call() {

        return new Callable<String>() {
            public String call()
            {
                int blockId = fps.getBlockId();
                String contextPath = Config.getBathPath();
                File blockDir = new File(contextPath+"block/"+blockId);
                RandomAccessFile raf1 = null;
                try
                {
                    raf1 = new RandomAccessFile(blockDir,"r");
                    FileTransClient client = new FileTransClient();
                    FileRegion fileRegion = BlockWrite.getInstance().getFileRegion(blockId,fps.getPakagePerSize(),raf1);
                    FilePackageResult result =(FilePackageResult)client.sendFile
                            (fps,fileRegion,ip,new Integer(port),300*1000);
                    if(!result.isStatus())
                    {
                        return "DataServerId:"+fps.getCurrentDataServer()+"处理blockId:"+blockId+":包:"+fps.getCurrentPakage()+":错误信息:"+result.getMsg();
                    }
                    return "success";
                }catch (Exception e)
                {
                    logger.error("DataServerId:"+fps.getCurrentDataServer()+"处理blockId:"+blockId+":包:"+fps.getCurrentPakage(),e);
                    return "DataServerId:"+fps.getCurrentDataServer()+":处理blockId:"+blockId+":包:"+fps.getCurrentPakage()+":错误信息:"+e.getMessage();
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
        };


    }

    @Override
    public Runnable rollback() {
        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    @Override
    public Runnable commit() {
        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }
}
