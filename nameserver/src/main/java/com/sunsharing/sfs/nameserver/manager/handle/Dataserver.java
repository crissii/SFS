package com.sunsharing.sfs.nameserver.manager.handle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by criss on 14-8-7.
 */
public class Dataserver {

    public String getDatas(String data)
    {
        List<DataServerStat> stats = DataServerStat.getDataServerStats();
        List<Map> list = new ArrayList<Map>();
        List datas = new ArrayList();
        if(!StringUtils.isBlank(data))
        {
            String[] dd =data.split(",");
            for(int i=0;i<dd.length;i++)
            {
                if(!StringUtils.isBlank(dd[i]))
                {
                    datas.add(dd[i]);
                }
            }
        }
        for(DataServerStat dss:stats)
        {
            Map m = new HashMap();
            boolean isonline = dss.isOnline();
            m.put("online",isonline);
            m.put("dsid",dss.getStatReport().getDataServerId());
            m.put("ip",dss.getStatReport().getIp());
            List<Block> currentBlocks = dss.getCurrentblock();
            String strBlocks = "";
            for(int i=0;i<currentBlocks.size();i++)
            {
                Block b = currentBlocks.get(i);
                strBlocks+= b.getBlockId()+",";
            }
            m.put("curentblock",strBlocks);
            if(datas.size()==0 || datas.contains(dss.getStatReport().getDataServerId()+""))
            {
                list.add(m);
            }
        }
        JSONObject jso = new JSONObject();
        jso.put("status",true);
        jso.put("data",list);
        return jso.toString();
    }

}
