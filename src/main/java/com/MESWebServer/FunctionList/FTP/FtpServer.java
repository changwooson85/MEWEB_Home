package com.MESWebServer.FunctionList.FTP;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FtpServer {
    private String name;
    private FtpCheckType checkType;
    private String server;
    private String port;
    private String user;
    private String password;
    private String filePath;

}