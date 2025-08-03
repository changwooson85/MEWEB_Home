package com.MESWebServer.FunctionList.FTP;

import com.MESWebServer.DTO.JobFile;
import com.MESWebServer.FunctionList.FTP.Service.FTPMESWorkerService;
import com.miracom.oneoone.transceiverx.pending.Job;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Block;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class FtpFileService {

    private final FtpServerProvider ftpServerProvider;
    private List<FtpProcess> ftpProcessList;
    //private final FTPMESWorkerService ftpmesWorkerService;
    //private Thread workerThread;

    public  FtpFileService(FtpServerProvider ftpServerProvider){
        this.ftpServerProvider = ftpServerProvider;
        //this.ftpmesWorkerService =ftpmesWorkerService;
    }

    @Scheduled(cron = "0 43 09 * * *")
    //@Scheduled(fixedRate = 6000000)
    //@Scheduled(cron = "0 40 14 * * *")
    public void checkAllftpServers(){
        if (ftpProcessList != null){
            ftpProcessList.clear();
        }else{
            ftpProcessList = new ArrayList<>();
        }

/*
        if (workerThread == null || !workerThread.isAlive()){
            workerThread = new Thread(ftpmesWorkerService);
            workerThread.setDaemon(true);
            workerThread.start();
        }
*/

        Optional<List<FtpServer>> optionalFtpServerList = Optional.ofNullable(ftpServerProvider.getJobFileFTP());

        optionalFtpServerList.filter(list->!list.isEmpty())
                .ifPresentOrElse(
                        list-> {
                            for(FtpServer server : list){
                                log.info("checkAllftpServers > " + server.getName());
                                System.out.println("checkAllftpServers > " + server.getName());
                                ftpServerProvider.checkFileExists(server, ftpProcessList);
                            }
                        },()->{log.error("checkAllftpServers Filter Error");}
                );
    }
/*    @Async
    public void checkFileExists(FtpServer server){
        switch(server.getCheckType()){
            case JOB_FILE -> {
                FtpProcess<JobFile> ftpProcess = new FtpJobFileProcess();
                if (!ftpProcess.getFtpProcess(server)){
                    return;
                }
                ftpProcessList.add(ftpProcess);
                Optional<List<JobFile>> jobFileList = Optional.ofNullable(ftpProcess.actionFromType());
                jobFileList.filter(list->!list.isEmpty())
                        .ifPresentOrElse(
                                list-> {
                                    ftpServerProvider.putJobFileSend(server.getName(), list);
                                },
                                ()-> System.out.println(("TEST"))
                        );
            }
        }
    }*/
    @PreDestroy
    public void close(){
        if (ftpProcessList != null){
            for(FtpProcess<?> ftpProcess: ftpProcessList){
                ftpProcess.close();
            }
        }
    }
}
