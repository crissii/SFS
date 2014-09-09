package com.sunsharing.sfs.requestapi;

import com.sun.xml.internal.rngom.parse.host.Base;
import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.component.utils.crypto.Base64;
import com.sunsharing.sfs.common.netty.NettyClient;
import com.sunsharing.sfs.common.netty.ShortNettyClient;
import com.sunsharing.sfs.common.pro.ProClassCache;
import com.sunsharing.sfs.common.pro.api.*;
import com.sunsharing.sfs.common.utils.Base58;
import com.sunsharing.sfs.common.utils.GetFileName;
import org.apache.log4j.Logger;

import java.io.*;
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

    public void read(String filename,OutputStream output,String ip,int port,boolean release)
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
        }finally
        {
            if(release)
            {
                client.cleanResource();
            }
        }
    }
    public String downloadJS(String dfsname,String sourcename,String ip,String port)
    {
        sourcename = tranName(sourcename);
        sourcename = sourcename.replaceAll("\\\\","/");
        File souceFile = new File(sourcename);
        if(souceFile.exists())
        {
            souceFile.renameTo(new File(sourcename+".bak"));
        }
        int i = sourcename.lastIndexOf("/");
        if(i!=-1)
        {
            File f1 = new File(sourcename.substring(0,i));
            if(!f1.exists())
            {
                f1.mkdirs();
            }
        }


        FileOutputStream output = null;
        try
        {
            output = new FileOutputStream(souceFile);
            read(dfsname,output,ip,new Integer(port),true);
            return "success";
        }catch (Exception e)
        {
            logger.error("",e);
            throw new RuntimeException("error");
        }finally {
            if(output!=null)
            {
                try
                {
                    output.close();
                }catch (Exception e)
                {

                }
            }
            exec.shutdown();
        }
    }

    public String addFileJS(String file,String extendLength,String ip,String port,String timeout)
    {
        file = tranName(file);
        File f = new File(file);
        long extend = new Long(extendLength)*1000;
        String result = request(f,null,extend,ip,new Integer(port),new Integer(timeout),true);
        exec.shutdown();
        return result;
    }

    public String updateFileJS(String dfsname,String file,String ip,String port,String timeout)
    {
        file = tranName(file);
        File f = new File(file);

        String result = updateFile(f, dfsname, ip, new Integer(port), new Integer(timeout), true);
        exec.shutdown();
        return result;
    }

    public String updateFile(File file,String updateFilename,String ip,int port,int timeout)
    {
        return request(file,updateFilename,0,ip,port,timeout,false);
    }

    public String updateFile(File file,String updateFilename,String ip,int port,int timeout,boolean clean)
    {
        return request(file,updateFilename,0,ip,port,timeout,clean);
    }

    public String addFile(File file,long extendLength,String ip,int port,int timeout)
    {
        return request(file,null,extendLength,ip,port,timeout,false);
    }

    private String request(File file,String updateFilename,long extendLength,String ip,int port,int timeout,boolean clean)
    {
        if(file.length()+extendLength>=64*1024*1024L)
        {
            throw new RuntimeException(file.getName()+"超过64M");
        }
        long fileLen = file.length();
        ProClassCache.init();
        ShortNettyClient client = new ShortNettyClient();
        int blockId = 0;
        try
        {
            WriteRequest request = new WriteRequest();
            request.setFilesize(fileLen);
            if(StringUtils.isBlank(updateFilename))
            {
                request.setExtendfilesize(extendLength);
            }else
            {
                request.setFilename(updateFilename);
            }

            WriteRequestResult result = (WriteRequestResult)
            client.request(request, ip, port, 300000);
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
                toalPakage = 4;

                String fileName = "";

                GetFileName getFileName = new GetFileName();
                if(StringUtils.isBlank(updateFilename))
                {

                    fileName = getFileName.fileName(file.getName());
                }else
                {
                    fileName = getFileName.decodeFile2Str(updateFilename);
                }

                ArrayList<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
                for(int i=1;i<=toalPakage;i++)
                {
                    FilePackageCall call = new FilePackageCall(file,i,toalPakage,fileLen,
                            fileName,result,timeout,request.getExtendfilesize());
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
                    throw new RuntimeException("上传出错:文件名:"+file.getAbsolutePath()+":"+file.getName());
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
            if(clean)
            {
                client.cleanResource();
            }
        }
    }
    private String tranName(String s)
    {
        s = s.replaceAll("@@@@","(");
        s = s.replaceAll("####",")");
        s = s.replaceAll("----"," ");
        return s;
    }

    public static void main(String[]a) throws Exception
    {

        //String a1 = new String(Base64.decode("LwBVAHMAZQByAHMALwBjAHIAaQBzAHMALwBEAGUAcwBrAHQAbwA="));
        //System.out.println(a1);
       // String s = Base64.encode("/Users/criss/Desktop/tmp/pp副本".getBytes("utf-8"));
//        String s = "@@@@a&&&&&&&&bc####";
//        s = s.replaceAll("@@@@","(");
//        s = s.replaceAll("&&&&"," ");
//        System.out.println(s);
        //System.out.println(s);
        //1112Ue3Q8JFYKyq.txt
        //111kAXhG2qt8zk
//        ExecutorService service =  Executors.newFixedThreadPool(1);
//        Runnable r = new Runnable(){
//            public void run()
//            {
//                try
//                {
//                    Request r = new Request();
//                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                    r.read("1112aAGXZXqdkoY.txt",bytes,"localhost",1320);
//                    System.out.println(new String(bytes.toByteArray(),"UTF-8"));
                    File f = new File("/Users/criss/Downloads/归档.zip");
                    //File f = new File("/Users/criss/Downloads/catalina.out");
                    Request r = new Request();
                    String filename = r.addFile(f,f.length()/4,"localhost",1320,30000);
//                    System.out.println("~~~~~~~~~:"+filename);
//                }catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        };
//        for(int i=0;i<1;i++)
//        {
        //1112SorHNvN9kPx.txt
//             File f = new File("/Users/criss/Desktop/file/1.txt");
//             Request r = new Request();
//             String filename = r.addFileJS("/Users/criss/Desktop/file/1.txt", "2", "localhost", "1320", "100000");
//            // String filename = r.updateFile(f,"1112SorHNvN9kPx.txt","localhost",1320,100000);
//             System.out.println("~~~~~~~~~:"+filename);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            r.read("1112SorHNvN9kPx.txt",out,"localhost",1320);
//            System.out.println(new String(out.toByteArray())+"AAAA");
//        }

//
//        for(int i=0;i<1;i++)
//        {
//            service.execute(r);
//        }

//        File f = new File("/Users/criss/Desktop/file/name1/block/1");
//        RandomAccessFile raf1 = null;
//        try
//        {
//            raf1 = new RandomAccessFile(f,"r");
//            raf1.seek(64*1024*1024L);
//            long len = raf1.readLong();
//            System.out.println(len);
//            byte[] at  = new byte[12];
//            int intlen = raf1.read(at);
//
//           // getFileName.getReturnFileName(file.getName(),result.getBlockId(),fileName);
//            //System.out.println(intlen);
//             len = raf1.readLong();
//            System.out.println(len);
//             len = raf1.readLong();
//            System.out.println(len);
//            len = raf1.readLong();
//            System.out.println(len);
////            byte[] tt = Base58.decode("111kAXhG2qt8zk");
////            long tt2 = ByteUtils.getLong(tt,4);
////            System.out.println(tt2);
//        }catch (Exception e)
//        {
//            e.printStackTrace();
//            //logger.error("读block出错", e);
//            //throw new RuntimeException("读取blockId出错,blockId:"+blockId+":"+e.getMessage());
//        }finally {
//            try
//            {
//                raf1.close();
//            }catch (Exception e)
//            {
//
//            }
//        }
        System.out.println("\u5B50\u8FDB\u7A0B\u5DF2\u5173\u95ED\uFF0C\u4EE3\u7801\uFF1A1");
//
    }

}
