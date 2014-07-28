package com.sunsharing.sfs.common.pro.api;

import com.sunsharing.sfs.common.pro.Constant;
import com.sunsharing.sfs.common.pro.JsonBodyProtocol;
import com.sunsharing.sfs.common.pro.ano.Protocol;

/**
 * Created by criss on 14-7-3.
 */
@Protocol(action = Constant.WRITE_RESULT)
public class WriteRequestResult extends JsonBodyProtocol {
    /**
     * 成功还是失败
     */
    boolean status;
    /**
     * 失败原因
     */
    String errorMsg;

    /**
     * blockId
     */
    int blockId;

    /**
     * IP
     */
    String leaderDataserverIp;

    /**
     * 端口
     */
    String lenderFileServerPort;

    /**
     * 领导者DataServer
     */
    int leaderDataServer;

    /**
     * 所有block的位置
     */
    String[] dataServer;

    /**
     * 文件大小
     */
    long filesize;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getLeaderDataserverIp() {
        return leaderDataserverIp;
    }

    public void setLeaderDataserverIp(String leaderDataserverIp) {
        this.leaderDataserverIp = leaderDataserverIp;
    }

    public String getLenderFileServerPort() {
        return lenderFileServerPort;
    }

    public void setLenderFileServerPort(String lenderFileServerPort) {
        this.lenderFileServerPort = lenderFileServerPort;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String[] getDataServer() {
        return dataServer;
    }

    public void setDataServer(String[] dataServer) {
        this.dataServer = dataServer;
    }

    public int getLeaderDataServer() {
        return leaderDataServer;
    }

    public void setLeaderDataServer(int leaderDataServer) {
        this.leaderDataServer = leaderDataServer;
    }
}