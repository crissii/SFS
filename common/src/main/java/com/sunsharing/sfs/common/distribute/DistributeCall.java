package com.sunsharing.sfs.common.distribute;

import java.util.concurrent.Callable;

/**
 * Created by criss on 14-7-2.
 */
public interface DistributeCall {

    public Callable<String> call();

    public Runnable rollback();

    public Runnable commit();

}
