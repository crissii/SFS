package com.sunsharing.sfs.common.pro.ano;

import java.lang.annotation.*;

/**
 * 协议处理注解
 */
@Target(ElementType.TYPE)//这个标注应用于类
@Retention(RetentionPolicy.RUNTIME)//标注会一直保留到运行时
@Documented//将此注解包含在javadoc中
public @interface HandleAno {
    byte action();
}
