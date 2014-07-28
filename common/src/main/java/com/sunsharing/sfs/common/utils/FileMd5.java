/**
 * @(#)$CurrentFile
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2013
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author criss
 *<br> 13-8-4 上午10:47
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <pre></pre>
 * <br>----------------------------------------------------------------------
 * <br> <b>功能描述:</b>
 * <br>
 * <br> 注意事项:
 * <br>
 * <br>
 * <br>----------------------------------------------------------------------
 * <br>
 */
public class FileMd5 {
    static Logger logger = Logger.getLogger(FileMd5.class);
    /**
     * 默认的密码字符串组合，apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
    protected static MessageDigest messagedigest = null;
    static{
        try{
            messagedigest = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException nsaex){
            System.err.println(FileMd5.class.getName()+"初始化失败，MessageDigest不支持MD5Util。");
            nsaex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        long begin = System.currentTimeMillis();

        //2EA3E66AC37DF7610F5BD322EC4FFE48 670M 11s kuri双核1.66G 2G内存
        File big = new File("/Users/criss/Desktop/tmp/1.txt");

        String md1=getFileMD5String(big,0,2);
        String md2=getFileMD5String(big,2,2);
        String md3=getFileMD5String(big,4,2);
        String md4=getFileMD5String(big,6,2);
        String md5=getFileMD5String(big,8,2);
        String md6=getFileMD5String(big,10,3);

        long end = System.currentTimeMillis();
        System.out.println(md1);
        System.out.println(md2);
        System.out.println(md3);
        System.out.println(md4);
        System.out.println(md5);
        System.out.println(md6);
    }

    /**
     * 适用于上G大的文件
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static synchronized String getFileMD5String(File file,long position,long length) throws IOException {
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, position, length);
            messagedigest.update(byteBuffer);
            return bufferToHex(messagedigest.digest());
        }catch (Exception e)
        {
            logger.error("获取MD5出错，文件名:"+file.getName()+",start position:"+position+",长度:"+length,e);
            return "";
        }finally {
            try
            {
                in.close();
            }catch (Exception e)
            {

            }
        }
    }

    public static String getFileMD5String(FileChannel ch,long position,long length) throws IOException {
        try
        {
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, position, length);
            messagedigest.update(byteBuffer);
            return bufferToHex(messagedigest.digest());
        }catch (Exception e)
        {
            logger.error("获取MD5出错，文件名:,start position:"+position+",长度:"+length,e);
            return "";
        }
    }

    public static String getMD5String(String s) {
        try
        {
        byte[] bb = s.getBytes("UTF-8");
            return getMD5String(bb);
        }catch (Exception e)
        {

        }

        return null;
    }

    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }


    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    public static boolean checkPassword(String password, String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }
}

