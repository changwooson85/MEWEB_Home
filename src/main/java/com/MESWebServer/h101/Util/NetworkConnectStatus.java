package com.MESWebServer.h101.Util;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkConnectStatus {

    //time out flag, retry count, retry spec count,
    private AtomicBoolean m_connectTimeOut;
    private int m_curRetryCount;
    private int m_specRetryCount;
    private int m_recvTimeOut;
    private int m_connTimeOut;
    private boolean m_isConnect;


    public NetworkConnectStatus() {
        m_connectTimeOut = new AtomicBoolean(false);
        this.m_isConnect = false;
    }

    public boolean get_isConnect() {
        return m_isConnect;
    }

    public void set_isConnect(boolean m_isConnect) {
        this.m_isConnect = m_isConnect;
    }

    public AtomicBoolean get_connectTimeOut() {
        return m_connectTimeOut;
    }

    public boolean TryConnectAvail(){
        if (m_curRetryCount++< m_specRetryCount){
            return true;
        }
        return false;
    }
    public void ClearRetryCount(){
        m_curRetryCount = 0;
    }
    public void set_specRetryCount(int m_specRetryCount) {
        this.m_specRetryCount = m_specRetryCount;
    }
    public int get_specRetryCount(){
        return this.m_specRetryCount;
    }

    public int get_recvTimeOut() {
        return m_recvTimeOut;
    }

    public void set_recvTimeOut(int recvTimeOut) {
        this.m_recvTimeOut = recvTimeOut;
    }

    public int get_connTimeOut() {
        return m_connTimeOut;
    }

    public void set_connTimeOut(int connTimeOut) {
        this.m_connTimeOut = connTimeOut;
    }
}
