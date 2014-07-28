package com.sunsharing.sfs.dataserver.server.file;

import com.sunsharing.sfs.common.netty.AnoServerHandle;
import com.sunsharing.sfs.common.netty.ExDecode;
import com.sunsharing.sfs.common.netty.ExEncode;
import com.sunsharing.sfs.common.netty.NettyServer;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Created by criss on 14-7-21.
 */
public class FileTransServer {

    public void start( int port ) throws Exception
    {
        NettyServer server = new NettyServer(port,new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();

                pipeline.addLast("encoder", new ExEncode());
                pipeline.addLast("fileChunckDecode",new FileChunckDecode());
                pipeline.addLast("handler",new AnoServerHandle());
                return pipeline;
            }
        });
        server.startup();
    }

}
