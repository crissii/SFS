package com.sunsharing.sfs.dataserver;

import com.sunsharing.component.resvalidate.config.ConfigContext;
import com.sunsharing.sfs.common.pro.ProClassCache;
import com.sunsharing.sfs.dataserver.block.BlockCache;
import com.sunsharing.sfs.dataserver.server.Data2NameServer;
import com.sunsharing.sfs.dataserver.server.file.FileTransServer;
import com.sunsharing.sfs.dataserver.stat.DataServerStatReport;
import org.apache.log4j.Logger;

/**
 * Created by criss on 14-7-2.
 */
public class DataServerMain {
    static Logger logger = Logger.getLogger(DataServerMain.class);

    public static void main(String[]a)
    {
        ConfigContext.instancesBean(Config.class);
        ProClassCache.init();
        if(a.length>=1)
        {
            if(a[0].equals("1"))
            {
                Config.dataserverId = "1";
                Config.msgLisen = "1318";
                Config.nameServerIp = "localhost";
                Config.nameServerPort = "1317";
                Config.fileServerPort = "1400";
                Config.basePath = "/Users/criss/Desktop/file/name1/";
            }
            if(a[0].equals("2"))
            {
                Config.dataserverId = "2";
                Config.msgLisen = "1319";
                Config.nameServerIp = "localhost";
                Config.nameServerPort = "1317";
                Config.fileServerPort = "1401";
                Config.basePath = "/Users/criss/Desktop/file/name2/";
            }
        }
        //初始化Block
        BlockCache.initFromDisk();
        try
        {
            Data2NameServer statServer =  new Data2NameServer(new Integer(Config.msgLisen));
        }catch (Exception e)
        {
            logger.error("启动服务器报错",e);
            System.exit(1);
        }

        try
        {
            FileTransServer server = new FileTransServer();
            server.start(new Integer(Config.fileServerPort));
        }catch (Exception e)
        {
            logger.error("启动服务器报错",e);
            System.exit(1);
        }

        DataServerStatReport.getInstance().startReport();
    }

}
