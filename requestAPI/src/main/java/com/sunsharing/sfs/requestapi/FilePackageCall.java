package com.sunsharing.sfs.requestapi;

import com.sunsharing.sfs.common.netty.FileTransClient;
import com.sunsharing.sfs.common.netty.ShortNettyClient;
import com.sunsharing.sfs.common.pro.api.FilePackageResult;
import com.sunsharing.sfs.common.pro.api.FilePakageSave;
import com.sunsharing.sfs.common.pro.api.WriteRequestResult;
import com.sunsharing.sfs.common.utils.FileMd5;
import com.sunsharing.sfs.common.utils.GetFileName;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;

/**
 * Created by criss on 14-7-25.
 */
public class FilePackageCall implements Callable {

    Logger logger = Logger.getLogger(FilePackageCall.class);

    private int currentPackage;

    private int toalPakage;

    private long fileLen;

    private long extendlen;

    private String source;

    private WriteRequestResult result;

    private int timeout;

    private File file;

    public FilePackageCall(File file,int currentPackage,int toalPakage,long fileLen,String source,
                           WriteRequestResult result,int timeout,long extendlen)
    {
        this.currentPackage = currentPackage;
        this.toalPakage = toalPakage;
        this.fileLen = fileLen;
        this.source = source;
        this.result = result;
        this.timeout = timeout;
        this.file = file;
        this.extendlen = extendlen;
    }

    @Override
    public Object call() throws Exception {
        RandomAccessFile raf = null;
        try
        {
            long perFile = fileLen/toalPakage;
            long from = (currentPackage-1)*perFile;
            long to = 0L;
            if(currentPackage==toalPakage)
            {
                to = fileLen;
            } else
            {
                to = (currentPackage)*perFile;
            }

            try {
                raf = new RandomAccessFile(this.file, "r");
            } catch (FileNotFoundException fnfe) {
                throw new RuntimeException(this.file.getPath()+"文件找不到");
            }

            FilePakageSave filePakageSave = new FilePakageSave();
            filePakageSave.setCurrentPakage(currentPackage);
            filePakageSave.setFromIndex(from);
            filePakageSave.setToIndex(to);
            filePakageSave.setTotalPakage(toalPakage);
            filePakageSave.setTotalSize(fileLen);
            filePakageSave.setFileName(source);
            filePakageSave.setBlockId(result.getBlockId());
            filePakageSave.setLeaderServer(result.getLeaderDataServer());
            filePakageSave.setDataservers(result.getDataServer());
            filePakageSave.setMd5(FileMd5.getFileMD5String(this.file,from,(to-from)));
            if(result.getOldblockIndex()!=0)
            {
                filePakageSave.setOldBlockIndex(result.getOldblockIndex());
                filePakageSave.setOldFilesize(result.getOldFilesize());
                filePakageSave.setOldExtendFileSize(result.getOldExtendFileSize());
            }else
            {
                filePakageSave.setExtendFileSize(this.extendlen);
            }

            long begin = filePakageSave.getFromIndex();
            long remailing = filePakageSave.getToIndex()-filePakageSave.getFromIndex() ;

            final FileRegion region =
                    new DefaultFileRegion(raf.getChannel(), begin, remailing,true);


            FileTransClient client = new FileTransClient();
            FilePackageResult r = (FilePackageResult)client.sendFile(filePakageSave, region,result.getLeaderDataserverIp(),
                    new Integer(result.getLenderFileServerPort()), timeout);
            r.print();
            if(r.isStatus())
            {
                return true;
            }else
            {
                logger.error(r.getMsg());
                return false;
            }
        }catch (Exception e)
        {
            logger.error("错误了",e);
            return false;
        }finally {
            if(raf!=null)
            {
                try
                {
                    raf.close();
                }catch (Exception e)
                {

                }
            }
        }
    }
}
