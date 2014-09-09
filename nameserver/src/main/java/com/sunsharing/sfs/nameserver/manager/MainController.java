package com.sunsharing.sfs.nameserver.manager;

import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.resvalidate.util.UriUtil;
import com.sunsharing.sfs.common.utils.Path;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.reflect.Method;

/**
 * Created by criss on 14-8-7.
 */
public class MainController extends HttpServlet {

    Logger logger = Logger.getLogger(MainController.class);

    protected void service(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {

        String uri = req.getRequestURI();



        String path = UriUtil.getClassPath();

        path = path.replaceAll("\\\\", "/");

        //String path = "/Users/criss/Desktop/projectDev/SFS/nameserver/src/main/resources";

        logger.info("path:"+path);

        //path +="/";

        byte[] outs = new byte[]{};


        //uri

        if(uri.equals("/") || uri.equals("/jquery-1.7.2.min.js") || uri.equals("/favicon.ico"))
        {
            if(uri.equals("/"))
            {
                uri = "/console.html";
            }

            FileInputStream input = null;
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try
            {
                input = new FileInputStream(path+uri);
                int len = 0;
                while((len = input.read(bytes))!=-1)
                {
                    out.write(bytes,0,len);
                }
                outs = out.toByteArray();

            }catch (Exception e)
            {
                logger.error("",e);
            }finally {
                if(input!=null)
                {
                    try
                    {
                        input.close();
                    }catch (Exception e)
                    {

                    }
                }
            }
        }else if(uri.equals("/remote"))
        {
            String cls = req.getParameter("class");
            String methd = req.getParameter("method");
            String data = req.getParameter("data");

            try{
                Class demo=Class.forName("com.sunsharing.sfs.nameserver.manager.handle."+cls);
                //一般尽量采用这种形式
                Object obj = demo.newInstance();
                Method med = demo.getMethod(methd,String.class);
                String result = (String)med.invoke(obj,data);
                outs = result.getBytes("UTF-8");
            }catch(Exception e){
                logger.error("", e);
                JSONObject json = new JSONObject();
                json.put("status",false);
                outs = json.toJSONString().getBytes("UTF-8");
            }
        }
        System.out.println(uri);
        response.getOutputStream().write(outs);
        response.getOutputStream().close();
    }

}
