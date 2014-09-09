package com.sunsharing.sfs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.directMan.File;
import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.dirmain.DirMain;
import org.jboss.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by criss on 14-9-5.
 */
@HandleAno(action = Constant.DIR_MAIN)
public class DirmainHandle implements Handle {
    @Override
    public void handler(Channel channel, BaseProtocol pro) {
        DirMain read = (DirMain)pro;
        String content = read.getContent();
        JSONObject obj = JSONObject.parseObject(content);
        if("add".equals(obj.getString("action")))
        {
            String dfsname = obj.getString("dfsname");
            String filePath = obj.getString("filePath");
            String lastModify = obj.getString("lastModify");
            String size = obj.getString("size");
            System.out.println(dfsname);
            System.out.println(filePath);
            System.out.println(lastModify);

            if(StringUtils.isBlank(dfsname))
            {
                //目录
                File.getRoot().addDirect(filePath);
            }else
            {
                File.getRoot().addFile(dfsname,filePath,new Long(lastModify),size);
            }
        }
    }
}
