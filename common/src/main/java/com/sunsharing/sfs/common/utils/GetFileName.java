package com.sunsharing.sfs.common.utils;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.component.utils.base.StringUtils;

import java.util.UUID;

/**
 * Created by criss on 14-7-18.
 */
public class GetFileName {

    public  String fileName(String sourceName)
    {
        String[] names = sourceName.split("\\.");
        //扩展名
        String ext = "";
        if(names.length>1)
        {
            ext = names[1].toLowerCase();
        }
        int ext_int = convertChars2Int(ext);

        String uuid = UUID.randomUUID().toString();
        long  filename = Hash.BKDRHash(uuid);

        byte[] arr = new byte[12];
        byte[] fileByte = ByteUtils.longToBytes(filename);
        byte[] extByte = ByteUtils.intToBytes(ext_int);
        System.arraycopy(fileByte,0,arr,0,8);
        System.arraycopy(extByte,0,arr,8,4);
        return Base58.encode(arr);
    }

    public byte[] getBlockIdBlokLength(String filename)
    {
        String[] names = filename.split("\\.");
        //扩展名
        String f = names[0];
        return Base58.decode(f);
    }

    public String getReturnFileName(String sourceName,int blockId,String destName)
    {
        String[] names = sourceName.split("\\.");
        //扩展名
        String ext = "";
        if(names.length>1)
        {
            ext = names[1].toLowerCase();
        }
        byte[] arr = decodeFile(destName);
        byte[] fileByte = new byte[8];
        System.arraycopy(arr,0,fileByte,0,8);
        byte[] blockByte = ByteUtils.intToBytes(blockId);
        byte[] result = new byte[12];
        System.arraycopy(blockByte,0,result,0,4);
        System.arraycopy(fileByte,0,result,4,8);
        String ss = Base58.encode(result);
        if(!StringUtils.isBlank(ext))
        {
            ss+="."+ext;
        }
        return ss;
    }

    public byte[] decodeFile(String fileName)
    {
        byte[] base = Base58.decode(fileName);
        long filename= ByteUtils.getLong(base,0);
        int ext = ByteUtils.getInt(base,8);
        byte[] arr = new byte[12];
        byte[] fileByte = ByteUtils.longToBytes(filename);
        byte[] extByte = ByteUtils.intToBytes(ext);
        System.arraycopy(fileByte,0,arr,0,8);
        System.arraycopy(extByte,0,arr,8,4);
        return arr;
    }

    public String decodeFile2Str(String fileName)
    {
        String[] names = fileName.split("\\.");
        //扩展名
        String ext = "";
        if(names.length>1)
        {
            ext = names[1].toLowerCase();
        }

        int ext_int = convertChars2Int(ext);

        byte[] arr = new byte[12];
        byte[] base = Base58.decode(names[0]);
        byte[] extByte = ByteUtils.intToBytes(ext_int);
        System.arraycopy(base,4,arr,0,8);
        System.arraycopy(extByte,0,arr,8,4);
        return Base58.encode(arr);
    }

    private static final int BIT_LEN = 5;
    private static final int END_CHAR_BIT =	0;//0
    private static final int MAX_CHAR_BIT =	26;//1~26
    private static final int NUM_BIT = 29;//1D
    private static final int OTHER_BIT = 30;//1E
    private static final int ALL_BIT = 31;//1F

    private static final byte NUM_USE_CHAR = '0';
    private static final byte OTHER_USE_CHAR = '#';

    private static String parseInt2chars(int val){
        byte[] cArr = new byte[6];
        int charLen = 6;
        for(int i = 0;i < 6;++i){
            int offset = i * BIT_LEN;
            //低位表示先出现的字符串？
            int v = (val >> offset) & ALL_BIT;
            if(v == END_CHAR_BIT){
                charLen = i;
                break;
            }
            byte c = OTHER_USE_CHAR;
            if(v == NUM_BIT){
                c = NUM_USE_CHAR;
            }
            else if(v <= MAX_CHAR_BIT){
                c = (byte)('a' + v - 1);
            }
            cArr[i] = c;
        }

        return new String(cArr, 0, charLen);
    }

    private static int convertChars2Int(String s){
        int ret = 0;
        char[] cArr = s.toCharArray();
        //只考虑26个字母  5位  2 ^ 5
        // int 可以存  32位
        // 未压缩 存 6个英文字母

        for(int i = 0;i < cArr.length && i < 6;++i){
            int offset = i * BIT_LEN;
            char c = cArr[i];
            c = Character.toLowerCase(c);
            if('a'<= c && c <= 'z'){
                ret |= (c - 'a' + 1) << offset;
                continue;
            }
            if('0' <= c && c <= '9'){
                ret |= NUM_BIT << offset;
                continue;
            }

            //其他数据
            ret |= OTHER_BIT << offset;
        }
        return ret;
    }

    public static void main(String[]a)
    {
        GetFileName get = new GetFileName();
        String name = get.fileName("1.txt");
        System.out.println(name);
        String returnFile = get.getReturnFileName("1.txt",2,name);
        System.out.println(returnFile);
        byte[] aa = get.getBlockIdBlokLength(returnFile);
        System.out.println("aaa");
    }

}
