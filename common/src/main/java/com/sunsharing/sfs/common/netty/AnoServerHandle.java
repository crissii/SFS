package com.sunsharing.sfs.common.netty;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.ProClassCache;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-2.
 */
public class AnoServerHandle extends ServerHandler {

    static Logger logger = Logger.getLogger(AnoServerHandle.class);

    @Override
    public void handle(Channel ch,BaseProtocol base)
    {
        Class handleClass = ProClassCache.getHandlClass(base.getAction());
        try
        {
            com.sunsharing.sfs.common.pro.Handle h = (com.sunsharing.sfs.common.pro.Handle)handleClass.newInstance();
            h.handler(ch,base);
        }catch (Exception e)
        {
            logger.error(base.getAction()+"找不到处理实现类:"+handleClass,e);
        }


    }

}
