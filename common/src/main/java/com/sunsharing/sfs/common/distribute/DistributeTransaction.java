package com.sunsharing.sfs.common.distribute;

import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.distribute.DistributeCall;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 分布式式事务
 * Created by criss on 14-7-2.
 */
public class DistributeTransaction {
    Logger logger = Logger.getLogger(DistributeTransaction.class);

    static ExecutorService exec = Executors.newCachedThreadPool();

    public void rollback(final List<DistributeCall> calls)
    {
        for (int i = 0; i < calls.size(); i++) {
            exec.execute(calls.get(i).rollback());
        }
    }

    public String excute(final List<DistributeCall> calls)
    {
        return excute(calls,this.exec);
    }

    public String excute(final List<DistributeCall> calls,ExecutorService exec)
    {
        ArrayList<Future<String>> results = new ArrayList<Future<String>>();
         //Future 相当于是用来存放Executor执行的结果的一种容器
        for (int i = 0; i < calls.size(); i++) {
            results.add(exec.submit(calls.get(i).call()));
        }
        boolean pass = true;
        String result = "";
        for (Future<String> fs : results) {
            String obj = null;
            try
            {
                obj = fs.get();
                if(StringUtils.isBlank((String)obj) || !obj.toString().equals("success"))
                {
                    pass = false;
                }
                if(!StringUtils.isBlank((String)obj))
                {
                    result+=obj.toString()+"::";
                }
            }catch (Exception e)
            {
                logger.error("分布式事务报错",e);
                result+=e.getMessage()+"::";
                pass=false;
                //throw new RuntimeException("分布式事务报错，错误原因："+e.getMessage());
            }
        }
        if(pass)
        {
            for (int i = 0; i < calls.size(); i++) {
                exec.submit(calls.get(i).commit());
            }
        }else
        {
            for (int i = 0; i < calls.size(); i++) {
                exec.submit(calls.get(i).rollback());
            }
        }
        if(pass)
        {
            return "success";
        }else
        {
            return result;
        }
    }


}
