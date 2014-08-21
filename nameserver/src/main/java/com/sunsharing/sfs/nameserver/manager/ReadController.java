package com.sunsharing.sfs.nameserver.manager;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.nameserver.Config;
import com.sunsharing.sfs.requestapi.Request;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;

/**
 * Created by criss on 14-8-11.
 */
public class ReadController extends HttpServlet {

    Logger logger = Logger.getLogger(MainController.class);

    protected void service(javax.servlet.http.HttpServletRequest request,
                           javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {
        request.setCharacterEncoding("utf-8");  //设置编码

        String name = request.getParameter("name");
        if(!StringUtils.isBlank(name)){
            if(name.toLowerCase().endsWith(".jpg"))//使用编码处理文件流的情况：
            {
                response.setContentType("image/jpg;charset=utf-8");//设定输出的类型
            }else if(name.toLowerCase().endsWith(".png"))
            {
                response.setContentType("image/png;charset=utf-8");//设定输出的类型
            }else{
                response.setContentType("application/octet-stream");//设定输出的类型
                response.setHeader("Content-Disposition", "attachment;"
                        + " filename="+new String(name.getBytes("UTF-8"), "ISO8859-1"));
            }
        }


        Request r = new Request();
        r.read(name,response.getOutputStream(),"localhost",new Integer(Config.nameserverLisen));

    }

}
