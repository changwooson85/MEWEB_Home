package com.MESWebServer.h101.Util;

import com.MESWebServer.h101.Core.H101NetworkEvent;

public interface IConnector {
    public boolean Connect();
    public boolean IsConnected();
    public void Disconnect();

    public void SetH101NetworkEvent(H101NetworkEvent h101NetworkEvent);
    public void MiddleWarePing();
}
