package com.sunsharing.sfs.dataserver.server;

import com.sunsharing.sfs.common.netty.AnoServerHandle;
import com.sunsharing.sfs.common.netty.NettyServer;

/**
 * Created by criss on 14-7-2.
 */
public class Data2NameServer {

    public Data2NameServer(int port) throws Exception
    {
        NettyServer server = new NettyServer(port,new AnoServerHandle());
        server.startup();
    }

}
