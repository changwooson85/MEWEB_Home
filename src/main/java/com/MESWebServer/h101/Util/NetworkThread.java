package com.MESWebServer.h101.Util;


import com.MESWebServer.h101.Core.H101NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkThread extends Thread{

    IConnector m_connector;

    H101NetworkEvent m_h101NetworkEvent;
    AtomicBoolean m_syncLock;
    AtomicBoolean m_loopStop;

    AtomicBoolean m_reConnect;
    NetworkActionType m_actionType = NetworkActionType.NOT_STATUS;
    @Override
    public void run() {
        while(false == m_loopStop.get()) {
            if (false == m_syncLock.get())
            {
                try {
                    Thread.sleep(1000);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try{
                switch (m_actionType)
                {
                    case CONNECT:{
                        m_connector.SetH101NetworkEvent(m_h101NetworkEvent);
                        if (false == m_connector.Connect()) {
                            m_syncLock.set(false);
                            m_reConnect.set(true);
                            return;
                        }
                        m_syncLock.set(false);
                        break;
                    }
                    case DISCONNECT: {

                        if (m_connector != null)
                            m_connector.Disconnect();
                        m_syncLock.set(false);
                        break;
                    }
                }
            }catch (Exception ex){
                m_syncLock.set(false);
            }
        }
    }

    public NetworkThread(IConnector connector,
                         NetworkActionType actionType, H101NetworkEvent h101NetworkEvent,
                         AtomicBoolean syncLock, AtomicBoolean loopStop, AtomicBoolean reConnect)
    {
        m_connector = connector;
        m_actionType = actionType;
        m_h101NetworkEvent = h101NetworkEvent;
        m_syncLock = syncLock;
        m_loopStop = loopStop;
        m_reConnect = reConnect;
    }
}
