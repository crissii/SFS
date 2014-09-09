package com.sunsharing.directMan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.netty.AnoServerHandle;
import com.sunsharing.sfs.common.netty.NettyServer;
import com.sunsharing.sfs.common.pro.ProClassCache;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by criss on 14-8-23.
 */
public class HttpMain {

    public static void main(String[]a) throws Exception
    {
//        File.initRoot();
//
//        Server server = new Server(9009);
//
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//
//        // http://localhost:8080/hello
//        context.addServlet(new ServletHolder(new AddFileController()), "/add");
//        context.addServlet(new ServletHolder(new SearchController()), "/getChildren");
//        //context.addServlet(new ServletHolder(new ReadController()), "/read");
//
//        server.start();
//        server.join();
        ProClassCache.init();
        NettyServer server = new NettyServer(9098,new AnoServerHandle());
        server.startup();

        Thread t = new Thread(){
            public void run()
            {
                while(true)
                {
                    try
                    {
                    Thread.sleep(30000);
                    }catch (Exception e)
                    {

                    }
                    try
                    {
                        File.getRoot().sync();
                    }catch (Exception e)
                    {

                    }
                }

            }
        };
        t.start();

        File.initRoot();
        ServerSocket serverSocket = new ServerSocket(9009);
        while(true)
        {
            Socket socket = null;
            try
            {
                     socket = serverSocket.accept();
            InputStream input = socket.getInputStream();
            byte b = 0;
            int len = 0;
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            while((len=input.read())!=-1)
            {
                //System.out.println(len);
                array.write((byte)len);
                //System.out.println(array.toString());
                if(len==125)
                {
                    break;
                }
            }
            JSONObject obj = JSON.parseObject(new String(array.toByteArray(),"UTF-8"));
            System.out.println(new String(array.toByteArray(),"UTF-8"));
            if("dir".equals(obj.getString("action")))
            {
                List<String> arr = new ArrayList<String>();
                File.root.getDirs(null,arr,"");
                socket.getOutputStream().write(JSONArray.toJSONString(arr).getBytes("UTF-8"));
            }else if("fulltext".equals(obj.getString("action")))
            {
                JSONArray jsonarray = FullText.fullText(obj.getString("keyword"));

                System.out.println(jsonarray.toJSONString());
                socket.getOutputStream().write(jsonarray.toJSONString().getBytes("UTF-8"));
            }else
            {
                String filePath = obj.getString("filePath");
                System.out.println(filePath);
                List<File> list = File.getRoot().getChilden(filePath);
                JSONArray jsonarray = new JSONArray();
                for(File f:list)
                {
                    JSONObject tmp = new JSONObject();
                    tmp.put("direct",f.direct);
                    tmp.put("source_name",f.source_name);
                    tmp.put("dfs_name",f.dfs_name);
                    tmp.put("last_modify", f.lastModify);
                    tmp.put("size", f.size);
                    jsonarray.add(tmp);
                }
                System.out.println(jsonarray.toJSONString());
                socket.getOutputStream().write(jsonarray.toJSONString().getBytes("UTF-8"));
            }
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(socket!=null)
                {
                    try
                    {
                        socket.close();
                    }catch (Exception e)
                    {

                    }
                }
            }
        }


    }





}
