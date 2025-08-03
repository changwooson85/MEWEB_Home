package com.MESWebServer.h101.Core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "channel")
@Getter
@Setter
public class ChannelProperties {
    private String programId;
    private String channelId;
    private String tuneChannel;
    private String serverIp;
    private int pingInterval;
    private int pingTimeout;
    private int poolSize;
}
