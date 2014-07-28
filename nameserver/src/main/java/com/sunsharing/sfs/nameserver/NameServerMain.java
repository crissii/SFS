package com.sunsharing.sfs.nameserver;

import com.sunsharing.component.resvalidate.config.ConfigContext;
import com.sunsharing.sfs.common.pro.ProClassCache;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockInfoWrite;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerSave;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStatCachServer;
import com.sunsharing.sfs.nameserver.request.RequestServer;
import org.apache.log4j.Logger;

/**
 * Created by criss on 14-6-26.
 */
public class NameServerMain {
    static Logger logger = Logger.getLogger(NameServerMain.class);

    public static void main(String[]a)
    {
        ConfigContext.instancesBean(Config.class);
        ProClassCache.init();
        DataServerSave.initFromFile();

        BlockInfoWrite blockInfoWrite = new BlockInfoWrite();
        blockInfoWrite.loadBlockInfo();

        blockInfoWrite.clearLock();

        try
        {
            DataServerStatCachServer statServer =  new DataServerStatCachServer(new Integer(Config.nameserverLisen));
        }catch (Exception e)
        {
            logger.error("启动服务器报错",e);
            System.exit(1);
        }

        try
        {
            RequestServer statServer =  new RequestServer(new Integer(Config.requestLisen));
        }catch (Exception e)
        {
            logger.error("启动服务器报错",e);
            System.exit(1);
        }

        DataServerStat.startCheckOutline();

    }

}
