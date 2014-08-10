package com.sunsharing.sfs.dataserver.server.file;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.distribute.DistributeCall;
import com.sunsharing.sfs.common.netty.LongNettyClient;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndex;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndexResult;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndexRollBack;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by criss on 14-7-24.
 */
public class FilePackageUpdateIndexThread implements DistributeCall {
    Logger logger = Logger.getLogger(FilePackageUpdateIndexThread.class);

    FilePakageUpdateIndex index;
    long [] sourceIndex;
    String ip;
    String port;
    FilePakageSave fs;

    public FilePackageUpdateIndexThread(FilePakageUpdateIndex index,long[] arr,String ip,String port,FilePakageSave fs)
    {
        this.index = index;
        this.sourceIndex = arr;
        this.ip = ip;
        this.port = port;
        this.fs = fs;
    }


    @Override
    public Callable<String> call() {

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                LongNettyClient client = new LongNettyClient();
                logger.info("往IP："+ip+":"+":"+port+"发送更新索引:"+index.getMessageId());
                FilePakageUpdateIndexResult result =
                        (FilePakageUpdateIndexResult)client.request(index,ip,new Integer(port),5000);
                if(!result.isStatus())
                {
                    return result.getMsg();
                }else
                {
                    return "success";
                }
            }
        };
    }

    @Override
    public Runnable rollback() {

        return new Runnable() {
            @Override
            public void run() {
                try
                {
                    LongNettyClient client = new LongNettyClient();
                    FilePakageUpdateIndexRollBack rollBack = new FilePakageUpdateIndexRollBack();
                    rollBack.setBlockId(index.getBlockId());
                    rollBack.setMessageId(StringUtils.generateUUID());
                    rollBack.setBlockIndx(sourceIndex[0]);
                    rollBack.setInfoIndx(sourceIndex[1]);
                    if(fs.getOldBlockIndex()!=0)
                    {
                        rollBack.setUpdateFile(true);
                        rollBack.setOldFileSize(fs.getOldFilesize());
                        rollBack.setOldExtendFile(fs.getOldExtendFileSize());
                        rollBack.setFilename(fs.getFileName());
                    }
                    client.requestNoRes(rollBack, ip, new Integer(port));
                }catch (Exception e)
                {
                    logger.error("blockId:"+index.getBlockId()+":",e);
                }
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
