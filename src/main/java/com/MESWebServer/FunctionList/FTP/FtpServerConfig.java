package com.MESWebServer.FunctionList.FTP;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FtpServerConfig {
    private List<FtpServer> servers;
}
