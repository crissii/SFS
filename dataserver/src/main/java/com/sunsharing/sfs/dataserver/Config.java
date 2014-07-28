package com.sunsharing.sfs.dataserver;

import com.sunsharing.component.resvalidate.config.annotation.Configuration;
import com.sunsharing.component.resvalidate.config.annotation.ParamField;

/**
 * Created by criss on 14-7-2.
 */
@Configuration(value = "dataserver.properties")
public class Config {
    @ParamField(name = "dataserver_id")
    public static String dataserverId;
    @ParamField(name = "local_ip")
    public static String localIp;
    @ParamField(name = "local_msg_lisen")
    public static String msgLisen;
    @ParamField(name = "nameserver_ip")
    public static String nameServerIp;
    @ParamField(name = "nameserver_port")
    public static String nameServerPort;
    @ParamField(name = "base_path")
    public static String basePath;
    @ParamField(name = "dataserver_file_lisen")
    public static String fileServerPort;

    public static String getBathPath()
    {
        return basePath;
    }

}
