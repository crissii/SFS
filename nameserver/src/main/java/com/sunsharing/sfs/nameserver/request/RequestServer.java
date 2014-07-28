package com.sunsharing.sfs.nameserver.request;

import com.sunsharing.sfs.common.netty.AnoServerHandle;
import com.sunsharing.sfs.common.netty.NettyServer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by criss on 14-7-3.
 */
public class RequestServer {

    public RequestServer(int port) throws Exception
    {
        NettyServer server = new NettyServer(port,new AnoServerHandle());
        server.startup();
    }

    public static void main(String []a)
    {
        class a{
            Lock lock = new ReentrantLock();
            public void lock()
            {
                lock.lock();
            }
            public void release()
            {
                lock.unlock();
            }
        }
        final a aa = new a();
         Thread a1 = new Thread(){
          public void run()
          {
              System.out.print("a1:start:");
              aa.lock();
              System.out.print("a1:end:");
          }
        };
        Thread a2 = new Thread(){
          public void run()
          {
              System.out.print("a2:start:");
              aa.lock();
              System.out.print("a2:end:");

          }
        };
        Thread a3 = new Thread(){
            public void run()
            {
                aa.release();
            }
        };

        a1.start();
        a2.start();
        a3.start();



    }

}
