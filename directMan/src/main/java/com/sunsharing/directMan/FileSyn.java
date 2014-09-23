package com.sunsharing.directMan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.netty.LongNettyClient;
import com.sunsharing.sfs.common.pro.dirmain.DirMain;
import com.sunsharing.sfs.requestapi.Request;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by criss on 14-9-13.
 */
public class FileSyn {

    ExecutorService service = Executors.newFixedThreadPool(5);

    public void syn(String baseName)
    {

    }

    public void synDir(String baseName,String url) throws Exception
    {
        baseName = baseName.replaceAll("\\\\","/");
        url = url.replaceAll("\\\\","/");

        String filePath = url.substring(baseName.length());

        String baseFile = filePath;
        System.out.println("::"+filePath);
        if(StringUtils.isBlank(filePath))
        {
            filePath = "root";
        }

        String bw = "{\"filePath\":\""+filePath+"\",\"action\":\"search\"}";

//        Socket s = new Socket("192.168.0.236",9009);
//
//        s.getOutputStream().write(bw.getBytes("UTF-8"));
//
//        InputStream input = s.getInputStream();
//
//        StringWriter writer = new StringWriter();
//        IOUtils.copy(input, writer, "utf-8");
//        String result = writer.toString();
//
//        s.close();
//
//        JSONArray array = JSONArray.parseArray(result);
//
//        for(int i=0;i<array.size();i++)
//        {
//            JSONObject obj = array.getJSONObject(i);
//            boolean direct = obj.getBoolean("direct");
//            String source_name = obj.getString("source_name");
//            String dfs_name = obj.getString("dfs_name");
//            if(direct)
//            {
//                java.io.File f = new java.io.File(url+"/"+source_name);
//                if(!f.exists())
//                {
//                    f.mkdirs();
//                }
//                synDir(baseName, url+"/"+source_name);
//            }else
//            {
//                java.io.File f = new java.io.File(url+"/"+source_name);
//                if(!f.exists())
//                {
//                    final String path = url+"/"+source_name;
//                    final String final_dfs = dfs_name;
//                    Runnable run = new Runnable(){
//                        public void run()
//                        {
//                            java.io.File ff = new java.io.File(path);
//                            Request r = new Request();
//                            int k = path.lastIndexOf("/");
//                            String ptmp = path.substring(0,k);
//                            new java.io.File(ptmp).mkdirs();
//                            FileOutputStream out =null;
//                            try
//                            {
//                                 out = new FileOutputStream(ff);
//                                //r.read(final_dfs,out,"192.168.0.169",1320,false);
//                            }catch (Exception e)
//                            {
//                                e.printStackTrace();
//                            }finally {
//                                try
//                                {
//                                    out.close();
//                                }catch (Exception e)
//                                {
//
//                                }
//                            }
//                        }
//                    };
//
//                    service.execute(run);
//
//                }
//            }
//        }

        java.io.File f = new java.io.File(url);

        java.io.File[] ff = f.listFiles();
        if(ff!=null)
        {
            for(int i=0;i<ff.length;i++)
            {
                java.io.File tmp = ff[i];
                if(tmp.isFile())
                {
                    boolean exist =false;
//                    for(int j=0;j<array.size();j++)
//                    {
//                        JSONObject obj = (JSONObject)array.get(j);
//                        boolean direct = obj.getBoolean("direct");
//                        String source_name = obj.getString("source_name");
//                        String dfs_name = obj.getString("dfs_name");
//                        if(!direct && source_name.equals(tmp.getName()))
//                        {
//                            exist = true;
//                            break;
//                        }
//                    }
                    if(!exist)
                    {
                        final String path = url+"/"+tmp.getName();
                        final String relativePath = baseFile+"/"+tmp.getName();
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                try
                                {
                                    java.io.File f = new java.io.File(path);
                                    long lastModify = f.lastModified();
                                    long fileSize = f.length();
                                    long extend = fileSize/4;
                                    Request request = new Request();
                                    String result = "";

                                    result = request.addFile(f,extend,"192.168.0.169",1320,30000);


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
                                }catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                        };
                        service.execute(r);
                    }
                }else
                {
                    synDir(baseName, url+"/"+tmp.getName());
                }
            }
        }


    }




}
