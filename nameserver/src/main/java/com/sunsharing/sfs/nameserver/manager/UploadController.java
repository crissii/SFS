package com.sunsharing.sfs.nameserver.manager;

import com.sunsharing.sfs.nameserver.Config;
import com.sunsharing.sfs.requestapi.Request;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by criss on 14-8-11.
 */
public class UploadController extends HttpServlet {

    Logger logger = Logger.getLogger(MainController.class);

    protected void service(javax.servlet.http.HttpServletRequest request,
                           javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {
        request.setCharacterEncoding("utf-8");  //设置编码

        //获得磁盘文件条目工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //获取文件需要上传到的路径
        File t = new File("upload");
        if(!t.exists())
        {
            t.mkdirs();
        }

        //如果没以下两行设置的话，上传大的 文件 会占用 很多内存，
        //设置暂时存放的 存储室 , 这个存储室，可以和 最终存储文件 的目录不同
        /**
         * 原理 它是先存到 暂时存储室，然后在真正写到 对应目录的硬盘上，
         * 按理来说 当上传一个文件时，其实是上传了两份，第一个是以 .tem 格式的
         * 然后再将其真正写到 对应目录的硬盘上
         */
        factory.setRepository(t);
        //设置 缓存的大小，当上传文件的容量超过该缓存时，直接放到 暂时存储室
        factory.setSizeThreshold(1024*1024) ;

        //高水平的API文件上传处理
        ServletFileUpload upload = new ServletFileUpload(factory);


        try {
            //可以上传多个文件
            List<FileItem> list = (List<FileItem>)upload.parseRequest(request);

            for(FileItem item : list)
            {
                //item.write( new File(path,filename) );//第三方提供的
                String name = item.getName();
                File f = new File("upload/"+name);
                item.write(f);
                Request r = new Request();
                String outname = r.addFile(f,0,"localhost",
                        new Integer(Config.nameserverLisen),10000);
                System.out.println(outname);
                f.delete();
                response.getOutputStream().write(("<script>parent.addfileSuccess('"+outname+"')</script>").getBytes());
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response.getOutputStream().write(("<script>parent.addfileError('"+e.getMessage()+"')</script>").getBytes());
        }
    }


}
