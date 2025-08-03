package com.MESWebServer.h101.Core;


import com.MESWebServer.h101.Core.*;
import com.MESWebServer.h101.Util.H101AsyncMessageHelper;
import com.MESWebServer.h101.Util.NetworkActionType;
import com.MESWebServer.h101.Util.NetworkConnectStatus;
import com.MESWebServer.h101.Util.NetworkThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkManager implements ThreadNetworkEvent  {

    private static NetworkManager m_networkHelper;
    int m_connectTimeOut;
    int m_recvTimeOut;
    int m_retryCnt;

    private List<H101Channel> m_h101ClientService;
    private List<NetworkThread> m_networkThread;

    private OnConnectFinish m_onConnectFinish;
    private OnDisConnectFinish m_onDisConnectFinish;

    //싱글톤 객체로 생성
    public static NetworkManager getInstance() {
        if (m_networkHelper == null)
            m_networkHelper = new NetworkManager();
        return m_networkHelper;
    }
    private NetworkManager(){
        // m_h101Status = new NetworkConnectStatus();
    }

    public interface OnConnectFinish
    {
        public void OnConnect(String msg);
    }
    public interface  OnDisConnectFinish
    {
        public void OnDisConnect(String msg);
    }


    @Override
    public void onListen(String msg) {
        //Toast.makeText(_refActivity.getApplicationContext(), "Listen 서버 성공", Toast.LENGTH_LONG).show();
        //m_onConnectFinish.OnConnect();
        //((MainActivity)_refActivity.getApplicationContext()).get_dialog().dismiss();
        //Toast.makeText(MainActivity.class, "성공",Toast.LENGTH_SHORT);
    }

    @Override
    public void onConnect(String msg) {
        // Toast.makeText(_refActivity.getApplicationContext(), "서버 연결 성공", Toast.LENGTH_LONG).show();
        //RegisterServerSerice();
        m_onConnectFinish.OnConnect(msg);
    }


    @Override
    public void onDisConnect(String msg) {
        m_onDisConnectFinish.OnDisConnect(msg);
    }

    @Override
    public String onSyncSend(String sendMessage) {
        return "";
    }

    //Server 처리 메시지
    @Override
    public void onAsyncRecieve(Object obj) {
        //StartActivityHelper > mBroadcastReceiver
/*        if (obj instanceof  LTSMSGType.DMSG_LTS_Cmn_Function_Ex_Out_Tag) {

            String receivedMessage = ((LTSMSGType.DMSG_LTS_Cmn_Function_Ex_Out_Tag)obj).h_recvBuf;
            //Toast.makeText(m_activity,((LTSMSGType.DMSG_LTS_Cmn_Function_Out_Tag)obj).h_recvBuf, Toast.LENGTH_SHORT).show();
            H101AsyncMessageHelper.GetInstance().SendBroadCast(receivedMessage);
            //m_context.sendBroadcast(intent);
        }*/
    }
    public void setConnectType(List<H101Channel> h101Channel) {
        //type에 따라 생성 방식 변경
        m_h101ClientService = h101Channel;
    }
    public void setTimeOutorCount(int connectTimeOut, int recvTimeOut, int retryCount){
        this.m_connectTimeOut = connectTimeOut;
        this.m_recvTimeOut = recvTimeOut;
        this.m_retryCnt = retryCount;
    }

    public boolean SessionConnect() {
        if (m_h101ClientService == null) return false;

        if(m_networkThread == null){
            m_networkThread = new ArrayList<>();
        }
        for(H101Channel h101ClientService : m_h101ClientService){

            AtomicBoolean sessionLock = new AtomicBoolean(false);
            AtomicBoolean loopStopLock = new AtomicBoolean(false);
            AtomicBoolean reConnectLock = new AtomicBoolean(false);
            NetworkConnectStatus networkConnectStatus = new NetworkConnectStatus();
            NetworkThread networkThread = new NetworkThread(h101ClientService, NetworkActionType.CONNECT, new H101NetworkEvent() {
                @Override
                public void OnConnet() {
                }
                public void OnDisConnect() {
                }
                @Override
                public void OnTimeOut() {
                }}, sessionLock, loopStopLock, reConnectLock);

            long nowTime = System.currentTimeMillis();
            long checkTime = nowTime + this.m_connectTimeOut;

            try {
                networkThread.start();
                sessionLock.set(true);
                while(true)
                {
                    if (!sessionLock.get())
                    {
                        loopStopLock.set(true);
                        break;
                    }

                    Thread.sleep(100);
                    if (System.currentTimeMillis() > checkTime)
                    {
                        loopStopLock.set(true);
                        break;
                    }
                }

                if (reConnectLock.get())
                {
                    if (networkConnectStatus.TryConnectAvail())
                    {
                        networkConnectStatus.get_connectTimeOut().set(false);
                        return this.SessionConnect();
                    }else{
                        //Error 메시지 처리
                        //this.onDisConnect("Server Connect 실패!");
                        return false;
                    }
                }

                if (!networkConnectStatus.get_connectTimeOut().get()){
                    networkConnectStatus.set_isConnect(true);
                    networkConnectStatus.ClearRetryCount();
                    m_networkThread.add(networkThread);
                }else{
                    //TimeOut 이내 연결 미확인 시
                    networkConnectStatus.get_connectTimeOut().set(true);
                }

            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }
        return m_h101ClientService.size() == m_networkThread.size();
    }

    public void DisConnect() {

    }

    H101NetworkEvent h101NetworkEvent
            = new H101NetworkEvent() {
        @Override
        public void OnConnet() {
           /* //연결 대기 시간 이전에 Connect 된 내용 인지 확인.
            if (m_h101Status.get_connectTimeOut().compareAndSet(false, false)){
                m_h101Status.set_isConnect(true);
            }else{
                //연결 다시 끊기?
                m_h101Status.set_isConnect(false);
                m_h101Status.get_connectTimeOut().set(true);
                //DisConnect();
            }*/
        }
        //H101이 끊어지면 상태값 변경.
        @Override
        public void OnDisConnect() {
      /*      m_h101Status.set_isConnect(false);
            m_h101Status.ClearRetryCount();
            m_h101Status.get_connectTimeOut().set(false);*/
        }
        @Override
        public void OnTimeOut() {
/*            m_h101Status.get_connectTimeOut().set(true);
            if (m_networkThread != null)
                m_networkThread.notify();*/
        }
    };
}
