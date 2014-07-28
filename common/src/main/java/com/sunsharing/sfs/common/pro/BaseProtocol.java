/**
 * @(#)BaseProtocol
 * 版权声明 厦门畅享信息技术有限公司, 版权所有 违者必究
 *
 *<br> Copyright:  Copyright (c) 2014
 *<br> Company:厦门畅享信息技术有限公司
 *<br> @author ulyn
 *<br> 14-2-2 下午8:09
 *<br> @version 1.0
 *————————————————————————————————
 *修改记录
 *    修改者：
 *    修改时间：
 *    修改原因：
 *————————————————————————————————
 */
package com.sunsharing.sfs.common.pro;

import com.sunsharing.component.utils.base.ByteUtils;
import com.sunsharing.component.utils.base.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;


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
public abstract class BaseProtocol implements Handle {

    //协议类型,请求还是响应(1)
    protected byte action;
    //消息主键(32)
    protected String messageId;
    //报文体长度(4)
    protected int bodyLength;

    protected byte[] getHeaderBytes() {
        if(action==0)
        {
            throw new RuntimeException("报文第一个字节不能为0");
        }
        if(StringUtils.isBlank(messageId))
        {
            throw new RuntimeException("报文的消息ID不能为空");
        }
        byte[] header = new byte[37];
        header[0] = action;
        ByteUtils.putString(header,messageId,1);
        ByteUtils.putInt(header, getRealBodyLength(), 33);
        return header;
    }
    protected String readString(int len, ChannelBuffer buffer) {
        byte[] bu = new byte[len];
        buffer.readBytes(bu);
        try {
            String s = new String(bu, "UTF-8").trim();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void setHeader(BaseProtocol pro, ChannelBuffer buffer) {
        pro.action = buffer.readByte();
        pro.messageId = readString(32,buffer);
        pro.bodyLength = buffer.readInt();
    }

    public byte getAction() {
        return action;
    }

    public void setAction(byte action) {
        this.action = action;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    protected abstract int getRealBodyLength();

    public abstract ChannelBuffer generate();

    public abstract BaseProtocol createFromChannel(ChannelBuffer buffer,BaseProtocol pro);

    public abstract void handler(Channel channel,BaseProtocol pro);


}

