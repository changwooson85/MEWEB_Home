package com.MESWebServer.h101.Core;

import com.miracom.oneoone.transceiverx.session.SessionImpl;
import com.miracom.oneoone.transceiverx.session.management.SessionMonitor;

public class CusSessionMonitor extends SessionMonitor {
    private SessionImpl mSession;
    public CusSessionMonitor(SessionImpl session) {
        super(session);
        mSession = session;
    }

    @Override
    public void start(){
        if (mSession.isStarted()==false) return;
        if (mSession.isConnected() ==false) return;
        super.ping();
    }
}
