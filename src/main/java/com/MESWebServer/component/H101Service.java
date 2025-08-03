package com.MESWebServer.component;

import com.MESWebServer.h101.Core.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("H101")
@Getter
@RequiredArgsConstructor
public class H101Service{
    private final ChannelPoolManager channelPoolManager;

    public H101Channel getIdleChannel() throws InterruptedException {
        return channelPoolManager.getIdleChannel();
    }
    @PreDestroy
    private void Finish()
    {
        channelPoolManager.Disconnect();
    }

}
