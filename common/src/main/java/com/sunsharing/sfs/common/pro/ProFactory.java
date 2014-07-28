/**
 * @(#)ProFactory
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2014
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author ulyn
 *<br> 14-2-5 上午1:42
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.pro;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

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
public class ProFactory {
    static Logger logger = Logger.getLogger(ProFactory.class);
    public static BaseProtocol createPro(ChannelBuffer buffer) {
        if (buffer.readableBytes() < 1) {
            return null;
        }
        buffer.markReaderIndex();
        byte action = buffer.readByte();
        buffer.resetReaderIndex();

        Class cls = ProClassCache.getProClass(action);
        try
        {
            BaseProtocol base = (BaseProtocol)cls.newInstance();
            return base.createFromChannel(buffer,base);
        }catch (Exception e)
        {
            logger.error(action+"找不到实现类:"+cls,e);
        }
        return null;
    }
}

