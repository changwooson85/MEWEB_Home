package com.MESWebServer.h101.Core;

import com.MESWebServer.h101.CallBack.MESWEBCallBack;
import com.MESWebServer.h101.Util.IConnector;
import com.miracom.oneoone.transceiverx.Session;
import com.miracom.oneoone.transceiverx.parser.DeliveryType;
import lombok.Getter;

@Getter
public class H101Channel implements IConnector {
    //channel 정보
    //program 정보
    private String m_programID;
    private String m_channelID; //Server CHANNEL 정보
    private String m_connectContents;
    private String m_multiTuneChannelID;
    private H101NetworkEvent m_h101NetworkEvent;
    private String m_ipAddress;
    private h101stub m_h101stub;
    private MESWEBCaster m_mesWebCaster;

    public H101Channel(String m_programID, String channelID, String tuneChannel, String connectionString)
    {
        this.m_programID =m_programID;
        this.m_channelID = channelID;
        this.m_connectContents = connectionString;
        this.m_multiTuneChannelID = tuneChannel;
        this.m_h101stub = new h101stub();
        this.m_mesWebCaster = new MESWEBCaster();
    }

    public void Clear()
    {
        m_programID = "";
        m_channelID = "";
        m_connectContents = "";
        m_multiTuneChannelID = "";
        m_ipAddress = "";
    }

    public void set_ipAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    public boolean IsBusy(){
        return m_mesWebCaster.isBusy();
    }

    @Override
    public boolean Connect() {
        m_mesWebCaster.setMESWEBChannel(this.m_channelID, this.m_h101stub);
        m_mesWebCaster.setMESWEBTTL(10 * 1000);
        if (m_h101stub.init(m_programID, m_ipAddress,
                Session.SESSION_INTER_STATION_MODE | Session.SESSION_PUSH_DELIVERY_MODE,
                m_connectContents,0, m_h101NetworkEvent) == false) {

            return false;
        }

        m_h101stub.registerDispatcher("MESWEB", new MESWEBCallBack());
        m_h101stub.tune(m_multiTuneChannelID, DeliveryType.MULTICAST);
        return true;
    }

    @Override
    public boolean IsConnected() {
        return m_h101stub.IsSessionConnect();
    }

    @Override
    public void Disconnect() {
        try {
            m_h101stub.untune(m_multiTuneChannelID, DeliveryType.MULTICAST);
            m_h101stub.term();
        } catch (Exception e) {

        }
    }

    @Override
    public void SetH101NetworkEvent(H101NetworkEvent h101NetworkEvent) {
        this.m_h101NetworkEvent = h101NetworkEvent;
    }

    @Override
    public void MiddleWarePing() {

    }
}
