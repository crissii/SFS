package com.sunsharing.sfs.common.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.URLDecoder;

/**
 * Created by criss on 14-6-30.
 */
public class Path {

    static Logger logger  = Logger.getLogger(Path.class);



    public static String getContextRealPath()
    {
        try
        {
            String path = getClassPath();
            path = path.substring(0,path.length()-1);
            return path.substring(0,path.lastIndexOf("/"))+"/";
        }catch (Exception e)
        {
            logger.error("获取全局路径出错",e);
        }
        return null;
    }

    private static String getClassPath() throws Exception{

        String keyfilePath = URLDecoder.decode(Path.class.getProtectionDomain().
                getCodeSource().getLocation().getFile(), "UTF-8");
        keyfilePath = keyfilePath.replaceAll("\\\\", "/");
        File temp = new File(keyfilePath);
        if(temp.isFile() && keyfilePath.endsWith("jar") ==true){
            keyfilePath = keyfilePath.substring(0, keyfilePath.lastIndexOf("/")) + "/";
        }else if(keyfilePath.indexOf("classes") != -1){
            keyfilePath = keyfilePath.substring(0, keyfilePath.indexOf("classes")+7) + "/";
        }
        return keyfilePath;
    }

}
