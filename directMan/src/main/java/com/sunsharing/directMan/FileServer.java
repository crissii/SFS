package com.sunsharing.directMan;

import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.netty.LongNettyClient;
import com.sunsharing.sfs.common.pro.dirmain.DirMain;
import com.sunsharing.sfs.requestapi.Request;

import java.io.FileOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by criss on 14-9-4.
 */
public class FileServer extends Thread {

    //public static ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue(100000);
    JSONObject obj = null;
    FileServer(JSONObject obj)
    {
        this.obj = obj;
    }

    public  void run()
    {
        //while(true)
        {
            try
            {
                //JSONObject obj = (JSONObject)blockingQueue.take();
                //System.out.println("----------------"+blockingQueue.size()+"----------------------");
                String action = (String)obj.get("action");
                if("add".equals(action))
                {
                    String path = (String)obj.getString("filepath");
                    String dfsname = (String)obj.getString("dfsname");
                    String relativePath = (String)obj.getString("relativePath");
                    java.io.File f = new java.io.File(path);
                    long lastModify = f.lastModified();
                    long fileSize = f.length();
                    long extend = fileSize/4;
                    Request request = new Request();
                    String result = "";
                    if(StringUtils.isBlank(dfsname))
                    {
                        result = request.addFile(f,extend,"192.168.0.169",1320,30000);
                    }else
                    {
                        result = request.updateFile(f,dfsname,"192.168.0.169",1320,30000);
                    }

                    LongNettyClient longNettyClient = new LongNettyClient();
                    DirMain d = new DirMain();
                    JSONObject obj1 = new JSONObject();
                    obj1.put("dfsname",result);
                    obj1.put("filePath",relativePath);
                    obj1.put("lastModify",lastModify);
                    obj1.put("action","add");
                    obj1.put("size",""+fileSize);
                    d.setContent(obj1.toJSONString());
                    //192.168.0.236
                    longNettyClient.requestNoRes(d,"192.168.0.236",9098);


                }else if(action.equals("download"))
                {
                    String path = (String)obj.getString("filepath");
                    String dfsname = (String)obj.getString("dfsname");
                    Request r = new Request();
                    int i = path.lastIndexOf("/");
                    String ptmp = path.substring(0,i);
                    new java.io.File(ptmp).mkdirs();
                    FileOutputStream out = new FileOutputStream(path);
                    try
                    {
                    r.read(dfsname,out,"192.168.0.169",1320,false);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }finally {
                        out.close();
                    }

                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
