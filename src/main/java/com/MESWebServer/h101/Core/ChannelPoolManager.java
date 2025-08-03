package com.MESWebServer.h101.Core;

import com.MESWebServer.FunctionList.Common.CommonGetInformation;
import com.MESWebServer.h101.Util.NetworkManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelPoolManager {
    private final ChannelProperties channelProperties;
    private final List<H101Channel> m_listChannel = new LinkedList<>();

/*    public ChannelPoolManager() throws IOException {
        this.maxSize = maxSize;
        for (int i = 0; i < maxSize; i++) {
            m_listChannel.add(new H101Channel("WebServer", "/DEV1/MESServer", "/DEV1/MES_WEB"+ i, "10.150.5.30:10101?pingInterval=60000&pingTimeout=70000"));
        }
        m_listChannel.forEach(n -> n.set_ipAddress(CommonGetInformation.GetLocalIP()));
        NetworkManager.getInstance().setTimeOutorCount(10000,10000,1);
        NetworkManager.getInstance().setConnectType(m_listChannel);
        if (!NetworkManager.getInstance().SessionConnect())
        {
            return;
            //로그
        }
    }*/
    @PostConstruct
    public void initChannels() throws IOException {
        int maxSize = channelProperties.getPoolSize();

        for (int i = 0; i < maxSize; i++) {
            String recvPath = channelProperties.getTuneChannel() + i;
            String fullAddress = channelProperties.getServerIp()
                    + ":10101?pingInterval=" + channelProperties.getPingInterval()
                    + "&pingTimeout=" + channelProperties.getPingTimeout();

            m_listChannel.add(new H101Channel(
                    channelProperties.getProgramId(),
                    channelProperties.getChannelId(),
                    recvPath,
                    fullAddress
            ));
        }

        m_listChannel.forEach(n -> n.set_ipAddress(CommonGetInformation.GetLocalIP()));
        NetworkManager.getInstance().setTimeOutorCount(10000, 10000, 1);
        NetworkManager.getInstance().setConnectType(m_listChannel);

        if (!NetworkManager.getInstance().SessionConnect()) {
            // 실패 시 처리
        }
    }
    public synchronized H101Channel getIdleChannel() throws InterruptedException {
        while (true) {
            for (H101Channel channel : m_listChannel) {
                if (!channel.IsBusy()) {
                    return channel;
                }
            }
            wait(100);
        }
    }
    public synchronized void releaseChannel(H101Channel channel) {
        notifyAll();
    }

    public synchronized void Disconnect() {
        m_listChannel.forEach(n -> n.Disconnect());
    }
}
