package com.sunsharing.sfs.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by criss on 14-7-2.
 */
public class ExceptionUtils {

    public static String exception2String(Exception e)
    {
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(input));
        return input.toString();
    }

}
