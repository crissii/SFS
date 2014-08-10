package com.sunsharing.sfs.dataserver.handle.dataserver;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndex;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndexResult;
import com.sunsharing.sfs.dataserver.Config;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-23.
 */
@HandleAno(action = Constant.FILE_PACKAGE_UPDATE_INDEX)
public class FilePakageUpdateIndexHandle implements Handle {
    Logger logger = Logger.getLogger(FilePakageUpdateIndexHandle.class);
    @Override
    public void handler(Channel channel, BaseProtocol pro) {
        FilePakageUpdateIndex index = (FilePakageUpdateIndex)pro;
        try
        {
            if(index.isUpdateFile())
            {
                BlockWrite.getInstance().updateFileIndex(index);
            }else
            {
                BlockWrite.getInstance().updateIndex(index);
            }

            FilePakageUpdateIndexResult result = new FilePakageUpdateIndexResult();
            result.setStatus(true);
            result.setMessageId(index.getMessageId());
            channel.write(result);
        }catch (Exception e)
        {
            logger.error("DataServer:"+Config.dataserverId+"更新Index报错,blockId:"+index.getBlockId()+":fileName:"+index.getFileName(),e);
            FilePakageUpdateIndexResult result = new FilePakageUpdateIndexResult();
            result.setStatus(false);
            result.setMsg("DataServer:"+Config.dataserverId+"更新Index报错,blockId:"+index.getBlockId()+":fileName:"+index.getFileName()+"" +
                    ":错误信息:"+e.getMessage());
            result.setMessageId(index.getMessageId());
            channel.write(result);
        }
    }
}
