package com.sunsharing.sfs.nameserver;

import com.sunsharing.component.resvalidate.config.annotation.Configuration;
import com.sunsharing.component.resvalidate.config.annotation.ParamField;
import com.sunsharing.sfs.common.utils.Path;

/**
 * Created by criss on 14-7-3.
 */
@Configuration(value = "nameserver.properties")
public class Config {

    @ParamField(name = "nameserver_lisen")
    public static String nameserverLisen;

    @ParamField(name = "request_lisen")
    public static String requestLisen;

    @ParamField(name = "lowest_back_num")
    public static String lowestBackNum;

    public static String getContextPath()
    {
        //System.out.println(Path.getContextRealPath());
        //return Path.getContextRealPath();
        return "/Users/criss/Desktop/file/nameserver/";
    }

}
