package com.sunsharing.sfs.common.pro;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 将属性名包装成报文体
 * Created by criss on 14-6-25.
 */
public  class JsonBodyProtocol extends BaseProtocol {
    protected static Logger logger = Logger.getLogger(JsonBodyProtocol.class);
    public JsonBodyProtocol()
    {
        //System.out.println("className:"+this.getProClass());
        this.action = ProClassCache.getActionByProClassName(this.getClass().getName());
        if(action==0)
        {
            throw new RuntimeException("无法取到第一个action,请检查报文的编码");
        }
    }
    @Override
    protected int getRealBodyLength() {
        try
        {
            return getBody().getBytes("UTF-8").length;
        }catch (Exception e)
        {
            logger.error("转换报文",e);
            return 0;
        }
    }

    @Override
    public ChannelBuffer generate() {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        buffer.writeBytes(getHeaderBytes());
        try
        {
            buffer.writeBytes(getBody().getBytes("UTF-8"));
        }catch (Exception e)
        {
            logger.error("转换报文",e);
        }
        return buffer;
    }

    @Override
    public BaseProtocol createFromChannel(ChannelBuffer buffer,BaseProtocol pro) {
        if (buffer.readableBytes() < 37) {
            return null;
        }
        buffer.markReaderIndex();

        //BaseProtocol pro = ProFactory.createPro(buffer);
        setHeader(pro, buffer);


        if (buffer.readableBytes() < pro.bodyLength) {
            buffer.resetReaderIndex();
            return null;
        }
        String jsonBody = readString(pro.bodyLength,buffer);
        JSONObject obj = JSONObject.parseObject(jsonBody);
        String fileNames[] = getFiledName();
        for(int i=0;i<fileNames.length;i++)
        {
            Object v = obj.get(fileNames[i]);
            if(v instanceof JSONArray)
            {
                JSONArray array = (JSONArray)v;
                String oo[] = new String[array.size()];
                for(int j=0;j<array.size();j++)
                {
                    oo[j] = (String)array.get(j);
                }
                setFieldValueByName(pro,fileNames[i],oo);
            }else
            {
                setFieldValueByName(pro,fileNames[i],v);
            }
        }
        return pro;
    }

    @Override
    public void handler(Channel channel,BaseProtocol pro) {

    }

    private String getBody()
    {
        JSONObject bodyObj = new JSONObject();
        String [] filename = getFiledName();
        for(int i=0;i<filename.length;i++)
        {
            String property = filename[i];
            if(!property.equals("action") &&
               !property.equals("messageId") &&
               !property.equals("bodyLength") &&
               !property.equals("logger"))
            {
                Object value = getFieldValueByName(property);
                if(value!=null)
                {
                    bodyObj.put(property,value);
                }
            }
        }
        return bodyObj.toJSONString();
    }

    protected String[] getFiledName(){
        Field[] fields=this.getClass().getDeclaredFields();
        String[] fieldNames=new String[fields.length];
        for(int i=0;i<fields.length;i++){
            fieldNames[i]=fields[i].getName();
        }
        return fieldNames;
    }
    /**
     * 根据属性名获取属性值
     * */
    protected Object getFieldValueByName(String fieldName) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            Field[] fields=this.getClass().getDeclaredFields();
            Class cls = null;
            for(int i=0;i<fields.length;i++){
                if(fields[i].getName().equals(fieldName))
                {
                    cls = fields[i].getType();
                }
            }
            String pre = "get";
            if(cls.equals(Boolean.TYPE))
            {
                pre = "is";
            }
            String getter = pre + firstLetter + fieldName.substring(1);
            Method method = this.getClass().getMethod(getter, new Class[] {});
            Object value = method.invoke(this, new Object[] {});
            return value;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    public void print()
    {
        String[] fileds = getFiledName();
        for(int i=0;i<fileds.length;i++)
        {
            if(!fileds[i].equals(action) &&
                    !fileds[i].equals(messageId) &&
                    !fileds[i].equals(bodyLength))
            {
                Object value = getFieldValueByName(fileds[i]);
                System.out.println(fileds[i]+"::"+value);
            }
        }
    }
    /**
     * 根据属性名写属性值
     * */
    protected void setFieldValueByName(Object obj,String fieldName,Object v) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "set" + firstLetter + fieldName.substring(1);
            Method[] methods = this.getClass().getMethods();
            for(int i=0;i<methods.length;i++)
            {
                if(methods[i].getName().equals(getter))
                {

                    try
                    {
                        methods[i].invoke(obj, new Object[] {v});
                    }catch (Exception e)
                    {
                        logger.error(this.getClass().getName()+":"+methods[i].getName()+":"+v.getClass().getName()+":"+v);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage()+"错误函数:",e);
        }
    }

}
