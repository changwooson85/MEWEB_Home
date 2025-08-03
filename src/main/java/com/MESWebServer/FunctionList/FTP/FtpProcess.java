package com.MESWebServer.FunctionList.FTP;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.MESWebServer.DTO.JobFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;

@Getter
@Setter
@Slf4j
public abstract class FtpProcess<T> {

    protected FTPClient client;
    protected FtpServer ftpServer;
    private List<T> resultList;

    public boolean getFtpProcess(FtpServer ftpServer) {
        this.ftpServer = ftpServer;
        client = new FTPClient();
        try{
            client.setConnectTimeout(10000);
            client.connect(ftpServer.getServer(), Integer.parseInt(ftpServer.getPort()));

            int reply = client.getReplyCode();  //응답코드가 비정상이면 종료한다.
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                return false;
            }
            if (!client.login(ftpServer.getUser(), ftpServer.getPassword())){
                client.disconnect();
                return false;
            }
            log.info("ftp Login  > " + ftpServer.getName());
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setSoTimeout(3 * 10000);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }
    public List<T> actionFromType(){
        return resultList;
    }

    public boolean isConnect(){
        if( null == client) return false;
        return client.isConnected();
    }
    public void close(){
        if (null != client){
            try {
                client.logout();
                client.disconnect();
            } catch (IOException e) {
                //
            }
        }
    }
}
