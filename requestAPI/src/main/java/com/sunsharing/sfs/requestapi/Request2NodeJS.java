package com.sunsharing.sfs.requestapi;

import java.lang.reflect.Method;

/**
 * Created by criss on 14-8-22.
 */
public class Request2NodeJS {

    public static void main(String[] a)
    {
        try
        {
        /**
         * a[0] class
         * a[1] method
         * a[2]
         */
        Class<?> demo=null;
        try{
            demo=Class.forName(a[0]);
        }catch (Exception e) {
            //e.printStackTrace();
            System.out.print("{\"status\":false,\"data\":\""+e.getMessage()+"\"}");
        }
        String []  params = new String[a.length-2];
        for(int i=0;i<params.length;i++)
        {
            params[i] = a[i+2];
        }

        try
        {
            Method method[]=demo.getMethods();
            for(int i=0;i<method.length;++i){
                if(method[i].getName().equals(a[1]))
                {
                    Object obj = method[i].invoke(demo.newInstance(),params);
                    //System.out.println(obj);
                    System.out.print("{\"status\":true,\"data\":\""+obj.toString()+"\"}");
                }
            }
        }catch (Exception e)
        {

            System.out.print("{\"status\":false,\"data\":\""+e.getMessage()+"\"}");
        }
        }catch (Exception e)
        {

        }finally {
            System.exit(0);
        }
    }


}
