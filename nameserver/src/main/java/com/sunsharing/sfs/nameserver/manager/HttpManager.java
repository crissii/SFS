package com.sunsharing.sfs.nameserver.manager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by criss on 14-8-7.
 */
public class HttpManager {

    public void start()throws Exception
    {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // http://localhost:8080/hello
        context.addServlet(new ServletHolder(new MainController()), "/");
        context.addServlet(new ServletHolder(new UploadController()), "/upload");
        context.addServlet(new ServletHolder(new ReadController()), "/read");

        server.start();
        server.join();
    }

}
