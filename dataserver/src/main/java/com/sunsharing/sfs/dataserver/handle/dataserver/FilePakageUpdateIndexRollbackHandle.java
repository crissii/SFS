package com.sunsharing.sfs.dataserver.handle.dataserver;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.dataserver.FilePakageUpdateIndexRollBack;
import com.sunsharing.sfs.dataserver.block.BlockWrite;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-7-24.
 */
@HandleAno(action = Constant.FILE_PACKAGE_UPDATE_ROLLBACK)
public class FilePakageUpdateIndexRollbackHandle implements Handle {

    Logger logger = Logger.getLogger(FilePakageUpdateIndexRollbackHandle.class);
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {

        FilePakageUpdateIndexRollBack rollBack = (FilePakageUpdateIndexRollBack)baseProtocol;
        long [] arr = new long[2];
        arr[0] = rollBack.getBlockIndx();
        arr[1] = rollBack.getInfoIndx();
        BlockWrite.getInstance().rollbackIndex(rollBack.getBlockId(),arr);

    }

}
