package com.sunsharing.sfs.common.pro.dataserver;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * DataServer状态汇报
 * Created by criss on 14-6-25.
 */
@Protocol(action = Constant.STAT_REPORT)
public class StatReport extends JsonBodyProtocol {
    /**
     * 数据服务器ID
     */
    private int dataServerId;
    /**
     * block总数
     */
    private int blockNum;
    /**
     * 在线写数
     */
    private int writeNum;
    /**
     * 在线读取书
     */
    private int readNum;
    /**
     * 剩余容量
     */
    private String remainingapacity;

    /**
     * DataServer的IP地址
     */
    private String ip;
    /**
     * 消息互联端口
     */
    private String msgPort;
    /**
     * 文件端口
     */
    private String fileServerport;


    public int getDataServerId() {
        return dataServerId;
    }

    public void setDataServerId(int dataServerId) {
        this.dataServerId = dataServerId;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public int getWriteNum() {
        return writeNum;
    }

    public void setWriteNum(int writeNum) {
        this.writeNum = writeNum;
    }

    public int getReadNum() {
        return readNum;
    }

    public void setReadNum(int readNum) {
        this.readNum = readNum;
    }

    public String getRemainingapacity() {
        return remainingapacity;
    }

    public void setRemainingapacity(String remainingapacity) {
        this.remainingapacity = remainingapacity;
    }

    public void copyStatReport(StatReport statReport)
    {
        String[] filedNames = super.getFiledName();
        for(int i=0;i<filedNames.length;i++)
        {
            this.setFieldValueByName(this,filedNames[i],statReport.getFieldValueByName(filedNames[i]));
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMsgPort() {
        return msgPort;
    }

    public void setMsgPort(String msgPort) {
        this.msgPort = msgPort;
    }

    public int getBusyScore()
    {
        //(writeNum*10+readNum);
        return blockNum;
    }

    public String getFileServerport() {
        return fileServerport;
    }

    public void setFileServerport(String fileServerport) {
        this.fileServerport = fileServerport;
    }

    public void printStatReport()
    {
        logger.debug("dataServerId:"+dataServerId);
        logger.debug("blockNum:"+blockNum);
        logger.debug("writeNum:"+writeNum);
        logger.debug("readNum:"+readNum);
        logger.debug("remainingapacity:"+remainingapacity);
    }

}
