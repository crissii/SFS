package com.sunsharing.sfs.nameserver.handle.dataserver;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.dataserver.FileSuccessUploadIndex;
import com.sunsharing.sfs.common.pro.dataserver.FileSuccessUploadIndexResult;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.Block;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockCache;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.createblock.BlockInfoWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-24.
 */
@HandleAno(action = Constant.File_SUCCESS_UPLOAD_INDEX)
public class FileSuccessUploadIndexHandle implements Handle {
    Logger logger = Logger.getLogger(FileSuccessUploadIndexHandle.class);
    @Override
    public void handler(Channel channel, BaseProtocol pro) {
        FileSuccessUploadIndex successUploadIndex = (FileSuccessUploadIndex) pro;
        Block block = BlockCache.getBlockById(successUploadIndex.getBlockId());
        try
        {
            if(block!=null)
            {
                if(!successUploadIndex.isUpdateFile())
                {
                    block.addFile(successUploadIndex);
                }else
                {
                    block.updatFile(successUploadIndex);
                }
            }
            FileSuccessUploadIndexResult result = new FileSuccessUploadIndexResult();
            result.setMessageId(successUploadIndex.getMessageId());
            result.setStatus(true);
            channel.write(result);
        }catch (Exception e)
        {
            logger.error("更新blockId索引:"+successUploadIndex.getBlockId()+"出错",e);
            if(block!=null)
            {
                if(!successUploadIndex.isUpdateFile())
                block.rollbackAddFile(successUploadIndex);
                else
                block.rollbackUpdateFile(successUploadIndex);
            }
            FileSuccessUploadIndexResult result = new FileSuccessUploadIndexResult();
            result.setMessageId(successUploadIndex.getMessageId());
            result.setStatus(false);
            result.setMsg("更新blockId索引:"+successUploadIndex.getBlockId()+"出错："+e.getMessage());
            channel.write(result);
        }
    }
}
