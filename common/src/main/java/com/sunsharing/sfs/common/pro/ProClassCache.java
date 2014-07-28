package com.sunsharing.sfs.common.pro;

import com.sunsharing.sfs.common.pro.ano.ClassFilter;
import com.sunsharing.sfs.common.pro.ano.ClassUtils;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.ano.Protocol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by criss on 14-6-30.
 */
public class ProClassCache {

    static Map<Byte,Class> proMap = new HashMap<Byte, Class>();
    static Map<Byte,Class> handleMap = new HashMap<Byte, Class>();

    public static Class getProClass(byte action)
    {
        return proMap.get(action);
    }
    public static Class getHandlClass(byte action)
    {
        return handleMap.get(action);
    }
    public static byte getActionByProClassName(String className)
    {
        for(Iterator iter = proMap.keySet().iterator();iter.hasNext();)
        {
            Byte action = (Byte)iter.next();
            if(proMap.get(action).getName().equals(className))
            {
                return action;
            }
        }
        return 0;
    }

    public synchronized static void init()
    {
        if(proMap.keySet().size()!=0)
        {
            return;
        }

        ClassFilter filter = new ClassFilter() {
            @Override
            public boolean accept(Class clazz) {
                boolean flag = clazz.isAnnotationPresent(Protocol.class);
                boolean handle = clazz.isAnnotationPresent(HandleAno.class);
                return flag || handle;
            }
        };
        List<Class> classes = ClassUtils.scanPackage("com.sunsharing.sfs", filter);
        for(Class cls:classes)
        {
            Protocol des = (Protocol)cls.getAnnotation(Protocol.class);
            if(des!=null)
            {
                proMap.put(Byte.valueOf(des.action()),cls);
            }
            HandleAno handle = (HandleAno)
                    cls.getAnnotation(HandleAno.class);
            if(handle!=null)
            {
                handleMap.put(Byte.valueOf(handle.action()),cls);
            }
        }
    }

}
