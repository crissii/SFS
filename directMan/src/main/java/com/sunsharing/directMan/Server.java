package com.sunsharing.directMan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by criss on 14-9-4.
 */
public class Server {
    public static void main(String[]a) throws Exception
    {
//        FileServer s = new FileServer();
//        s.start();

        ExecutorService service = Executors.newFixedThreadPool(5);

        ServerSocket serverSocket = new ServerSocket(9010);
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
                   // System.out.println(len);
                    array.write((byte)len);
                    //System.out.println(array.toString());
                    if(len==93)
                    {
                        break;
                    }
                }
                JSONArray obj = JSONArray.parseArray(new String(array.toByteArray(), "UTF-8"));
                System.out.println(new String(array.toByteArray(),"UTF-8"));
                for(int i=0;i<obj.size();i++)
                {
                    FileServer s = new FileServer((JSONObject)obj.get(i));
                    service.execute(s);
                }
                //FileServer.blockingQueue.add(obj.get(i));
                socket.getOutputStream().write("success".getBytes());
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
