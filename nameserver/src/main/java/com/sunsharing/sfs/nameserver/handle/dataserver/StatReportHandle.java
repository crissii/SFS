package com.sunsharing.sfs.nameserver.handle.dataserver;

import com.sunsharing.sfs.common.pro.BaseProtocol;
import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.Handle;
import com.sunsharing.sfs.common.pro.ano.HandleAno;
import com.sunsharing.sfs.common.pro.dataserver.StatReport;
import com.sunsharing.sfs.nameserver.handle.dataserver.service.dataserverstat.DataServerStat;
import org.jboss.netty.channel.Channel;

/**
 * Created by criss on 14-6-27.
 */
@HandleAno(action = Constant.STAT_REPORT)
public class StatReportHandle implements Handle {
    @Override
    public void handler(Channel channel,BaseProtocol baseProtocol) {
        DataServerStat.addDataServer((StatReport) baseProtocol,true);
    }
}
