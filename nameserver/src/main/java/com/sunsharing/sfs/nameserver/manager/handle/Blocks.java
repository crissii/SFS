package com.sunsharing.sfs.nameserver.manager.handle;

import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;

import java.text.DecimalFormat;

/**
 * Created by criss on 14-8-7.
 */
public class Blocks  {

    public String getDatas(String data)
    {
        if(StringUtils.isBlank(data))
        {
            JSONObject jso = new JSONObject();
            jso.put("status",true);
            jso.put("data","当前block数为:"+BlockCache.getBlockSize());
            return jso.toString();
        }

        try
        {
            int blockId = new Integer(data);
            Block block = BlockCache.getBlockById(blockId);
            if(block==null)
            {
                JSONObject jso = new JSONObject();
                jso.put("status",false);
                jso.put("data","无法找到Block:"+data);
                return jso.toString();
            }
            JSONObject json = new JSONObject();
            json.put("blockId",block.getBlockId());
            json.put("filenum",block.getFilenum()+"");
            long b = block.getCurrentindex();
            double db = b/1024.0/1024;
            DecimalFormat nf = new DecimalFormat();
            nf.setMaximumFractionDigits(2);
            json.put("filesize",nf.format(db)+"M");
            json.put("filesizebyte",b+"");

            JSONObject jso = new JSONObject();
            jso.put("status",true);
            jso.put("data",json);
            return jso.toString();

        }catch (Exception e)
        {
            JSONObject jso = new JSONObject();
            jso.put("status",false);
            jso.put("data","无法找到Block:"+data+",blockId:"+data);
            return jso.toString();
        }



    }

}
