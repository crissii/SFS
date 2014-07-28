package com.sunsharing.sfs.requestapi;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.netty.NettyClient;
import com.sunsharing.sfs.common.netty.ShortNettyClient;
import com.sunsharing.sfs.common.pro.ProClassCache;
import com.sunsharing.sfs.common.pro.api.*;
import com.sunsharing.sfs.common.utils.Base58;
import com.sunsharing.sfs.common.utils.GetFileName;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by criss on 14-7-4.
 */
public class Request {

    static ExecutorService exec = Executors.newCachedThreadPool();

    Logger logger = Logger.getLogger(Request.class);

    public void read(String filename,OutputStream output,String ip,int port)
    {
        GetFileName getFileName = new GetFileName();
        ProClassCache.init();
        ShortNettyClient client = new ShortNettyClient();
        try
        {
            byte[] arr = getFileName.getBlockIdBlokLength(filename);
            byte[] blockId = new byte[4];
            byte[] filenameByte = new byte[8];
            System.arraycopy(arr,0,blockId,0,4);
            System.arraycopy(arr,4,filenameByte,0,8);
            ReadFile read = new ReadFile();
            read.setBlockId(ByteUtils.getInt(blockId));
            read.setFilename(ByteUtils.getLong(filenameByte,0));
            read.setMessageId(StringUtils.generateUUID());
            ReadFileResult result = (ReadFileResult)client.request(read,ip,port,5000);
            if(!result.isStatus())
            {
                throw new RuntimeException("无法从nameserver取到索引:错误信息:"+result.getMsg());
            }
            //
            String[] dataServs = result.getDataServers();
            Random rand = new Random();
            int index = rand.nextInt(dataServs.length);
            String ipPorts = dataServs[index];
            String dataIp = ipPorts.split(":")[1];
            String dataPort = ipPorts.split(":")[3];

            DataRead dataRead = new DataRead();
            dataRead.setBlockIndex(result.getBlockIndex());
            dataRead.setBlockId(read.getBlockId());
            dataRead.setLength(result.getFileSize());
            dataRead.setMessageId(StringUtils.generateUUID());

            FileReadClient readClient  = new FileReadClient();
            DataReadResult readResult =
                    (DataReadResult)readClient.readFile(dataRead,output,dataIp,new Integer(dataPort),10000);
            if(!readResult.isDoSuccess())
            {
                throw new RuntimeException(readResult.getDoError());
            }
            if(!readResult.isStatus())
            {
                throw new RuntimeException(readResult.getMsg());
            }
            readResult.print();
        }catch (Exception e)
        {
            logger.error("读取文件:"+filename+":出错:",e);
            throw new RuntimeException("读取文件:"+filename+":出错:"+e.getMessage());
        }
    }

    public String request(File file,String ip,int port,int timeout)
    {
        long fileLen = file.length();
        ProClassCache.init();
        ShortNettyClient client = new ShortNettyClient();
        int blockId = 0;
        try
        {
            WriteRequest request = new WriteRequest();
            request.setFilesize(fileLen);
            WriteRequestResult result = (WriteRequestResult)
            client.request(request, ip, port, 10000);
            //result.print();
            if(result.isStatus())
            {
                blockId = result.getBlockId();
                int toalPakage = 0;
                if(fileLen<=1024*1024)
                {
                    //小于1M
                    toalPakage = 1;
                }else
                {
                    toalPakage = (int)(fileLen/(10240*1024));
                }
                if(toalPakage==0)
                {
                    toalPakage = 1;
                }
                toalPakage = 1;

                GetFileName getFileName = new GetFileName();
                String fileName = getFileName.fileName(file.getName());

                ArrayList<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
                for(int i=1;i<=toalPakage;i++)
                {
                    FilePackageCall call = new FilePackageCall(file,i,toalPakage,fileLen,
                            fileName,result,timeout);
                    results.add(exec.submit(call));
                }
                boolean allSuccess = true;
                for (Future<Boolean> fs : results) {
                    try
                    {
                        Boolean b = fs.get();
                        if(!b)
                        {
                            allSuccess = false;
                        }
                    }catch (Exception e)
                    {
                        logger.error("",e);
                        allSuccess = false;
                    }
                }
                if(!allSuccess)
                {
                    throw new RuntimeException("上传出错");
                }
                return getFileName.getReturnFileName(file.getName(),result.getBlockId(),fileName);
            }else
            {
                logger.error(result.getErrorMsg());
                throw new RuntimeException("上传失败，错误原因:"+result.getErrorMsg());
                //return false;
            }

        }catch (Exception e)
        {
            logger.error("发送报错了",e);
            throw new RuntimeException("上传失败，错误原因:"+e.getMessage());
        }finally
        {
            if(blockId!=0)
            {
                ReleaseLock releaseLock = new ReleaseLock();
                releaseLock.setBlockId(blockId);
                try
                {
                    client.requestNoRes(releaseLock, ip, port);
                }catch (Exception e)
                {
                    logger.error("释放锁报错",e);
                }
            }
        }
    }

    public static void main(String[]a) throws Exception
    {

        //1112Ue3Q8JFYKyq.txt
        //111kAXhG2qt8zk
//        ExecutorService service =  Executors.newFixedThreadPool(1);
//        Runnable r = new Runnable(){
//            public void run()
//            {
//                try
//                {
////                    Request r = new Request();
////                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
////                    r.read("1112aAGXZXqdkoY.txt",bytes,"localhost",1320);
////                    System.out.println(new String(bytes.toByteArray(),"UTF-8"));
//                    File f = new File("/Users/criss/Desktop/file/1.txt");
//                    Request r = new Request();
//                    String filename = r.request(f,"localhost",1320,1000000);
//                    System.out.println("~~~~~~~~~:"+filename);
//                }catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        for(int i=0;i<1;i++)
//        {
//            service.execute(r);
//        }

        File f = new File("/Users/criss/Desktop/file/name1/block/1");
        RandomAccessFile raf1 = null;
        try
        {
            raf1 = new RandomAccessFile(f,"r");
            raf1.seek(64*1024*1024L+8);
            long len = raf1.readLong();
            System.out.println(len);
            int intlen = raf1.readInt();
            System.out.println(intlen);
             len = raf1.readLong();
            System.out.println(len);
             len = raf1.readLong();
            System.out.println(len);
            byte[] tt = Base58.decode("111kAXhG2qt8zk");
            long tt2 = ByteUtils.getLong(tt,4);
            System.out.println(tt2);
        }catch (Exception e)
        {
            e.printStackTrace();
            //logger.error("读block出错", e);
            //throw new RuntimeException("读取blockId出错,blockId:"+blockId+":"+e.getMessage());
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
