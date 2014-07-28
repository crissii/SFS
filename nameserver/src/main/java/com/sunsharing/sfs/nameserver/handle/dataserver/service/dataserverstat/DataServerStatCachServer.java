package com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat;

import com.sunsharing.sfs.common.netty.AnoServerHandle;
import com.sunsharing.sfs.common.netty.NettyServer;

/**
 * Created by criss on 14-6-25.
 */
public class DataServerStatCachServer {

    public DataServerStatCachServer(int port) throws Exception
    {
        NettyServer server = new NettyServer(port,new AnoServerHandle());
        server.startup();
    }

}
