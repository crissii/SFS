/**
 * @(#)$CurrentFile
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2013
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author criss
 *<br> 13-8-15 上午10:48
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.btree;

/**
 * <pre></pre>
 * <br>----------------------------------------------------------------------
 * <br> <b>功能描述:</b>
 * <br>
 * <br> 注意事项:
 * <br>
 * <br>
 * <br>----------------------------------------------------------------------
 * <br>
 */

public interface B {
    public Object get(Comparable key);   //查询
    public void remove(Comparable key);    //移除
    //插入或者更新，如果已经存在，就更新，否则插入
    public void insertOrUpdate(Comparable key, Object obj);
}