package com.MESWebServer.FunctionList.FTP;

import com.MESWebServer.DTO.JobFile;
import com.MESWebServer.component.H101Service;
import com.MESWebServer.entity.Real.CrasJobLst;
import com.MESWebServer.entity.Real.CrasJobLstId;
import com.MESWebServer.h101.Core.MESWEBCaster;
import com.MESWebServer.h101.Core.MESWEBType;
import com.MESWebServer.h101.Core.MESWEBType.*;
import com.MESWebServer.service.CrasJobLstService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@Service
@Getter
@AllArgsConstructor
@Slf4j
public class FtpServerProvider {

    List<FtpServer> ftpServerList;
    private final  BlockingQueue<Map<String, List<JobFile>>> blockingQueueList;
    private final H101Service h101Service;
    private final CrasJobLstService crasJobLstService;

/*    FtpServerProvider(BlockingQueue<Map<String, List<JobFile>>> blockingQueue, H101Service h101Service){
        this.blockingQueueList = blockingQueue;
        this.h101Service = h101Service;
    }*/

    public List<FtpServer> getJobFileFTP(){
        MESWEB_MGCMTBLDAT_In_Tag inTag = new MESWEB_MGCMTBLDAT_In_Tag();
        MESWEB_MGCMTBLDAT_Out_Tag outTag = new MESWEB_MGCMTBLDAT_Out_Tag();

        inTag.h_factory ="AFB1";
        inTag.h_proc_step = '1';
        inTag.table_name ="PHOTO_FTP_JOB_FILE";
        try {
            if (!h101Service.getIdleChannel().getM_mesWebCaster().MESWEB_View_MGCMTBLDAT(inTag, outTag)) {

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ftpServerList = new ArrayList<>();
        for(int i = 0; i < outTag._size_data_list; i++){
            ftpServerList.add(new FtpServer(outTag.data_list[i].key_1,
                    FtpCheckType.valueOf(outTag.data_list[i].key_2),
                    outTag.data_list[i].data_1,
                    outTag.data_list[i].data_2,
                    outTag.data_list[i].data_3,
                    outTag.data_list[i].data_4,
                    outTag.data_list[i].data_5));
        }
        return ftpServerList;
    }

/*
    public void putJobFileSend(String serverName, List<JobFile> jobFileList){
        MESWEB_Ftp_Job_File_In_Tag inTag = new MESWEB_Ftp_Job_File_In_Tag();
        MESWEB_Cmn_Out_Tag outTag = new MESWEB_Cmn_Out_Tag();

        inTag.h_factory = "AFB1";
        inTag.h_proc_step ='1';
        inTag.server_name = serverName;
        int limit = 1000;
        for (int id = 0; id < jobFileList.size(); id += limit){
            MESWEB_Ftp_Job_File_In_Tag_data_list[] arrayJobFile = jobFileList.subList(id, min(id+limit, jobFileList.size())).stream().map(m-> new MESWEB_Ftp_Job_File_In_Tag_data_list(
                    m.getDevice(),
                    m.getDevice(),
                    m.getTimeStamp()
            )).toArray(MESWEB_Ftp_Job_File_In_Tag_data_list[]::new);

            inTag._size_data_list = arrayJobFile.length;
            inTag.data_list = arrayJobFile;
            System.out.println("putJobFileSend" + " > " + Thread.currentThread().getName());
            try {
                if (!h101Service.getChannelPoolManager().getIdleChannel().getM_mesWebCaster().MESWEB_Update_Ftp_Job_File(inTag, outTag)) {

                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
*/


    private void putJobFileSend(String serverName, List<JobFile> jobFileList){
        MESWEBType.MESWEB_Ftp_Job_File_In_Tag inTag = new MESWEBType.MESWEB_Ftp_Job_File_In_Tag();
        MESWEBType.MESWEB_Cmn_Out_Tag outTag = new MESWEBType.MESWEB_Cmn_Out_Tag();

        int limit = 5000;
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDateTime = dateFormat.format(currentDate);

        inTag.h_factory = "AFB1";
        inTag.h_proc_step ='1';
        inTag.server_name = serverName;
        inTag.job_time = formattedDateTime;
        for (int id = 0; id < jobFileList.size(); id += limit){
            MESWEBType.MESWEB_Ftp_Job_File_In_Tag_data_list[] arrayJobFile = jobFileList.subList(id, min(id+limit, jobFileList.size())).stream().map(m-> new MESWEBType.MESWEB_Ftp_Job_File_In_Tag_data_list(
                    m.getDevice(),
                    m.getLayer(),
                    m.getTimeStamp()
            )).toArray(MESWEBType.MESWEB_Ftp_Job_File_In_Tag_data_list[]::new);

            //마지막 List 내용인지 확인
            inTag._size_data_list = arrayJobFile.length;
            inTag.data_list = arrayJobFile;
            if (id + limit >= jobFileList.size()){
                inTag.last_list = 'Y';
            }

            //System.out.println("putJobFileSend" + " > " + Thread.currentThread().getName());
            try {
                if (!h101Service.getIdleChannel().getM_mesWebCaster().MESWEB_Update_Ftp_Job_File(inTag, outTag)) {
                    log.error("""
                    [ERROR] MESWEB_Update_Ftp_Job_File
                    """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                    );
                    return;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            /*if (!MESWEBCaster.MESWEB_Update_Ftp_Job_File(inTag, outTag)){
                log.error("""
                [ERROR] MESWEB_Update_Ftp_Job_File
                """ + String.format(": %s/%s/%s",outTag.h_msg, outTag.h_field_msg, outTag.h_msg )
                );
                return;
            }*/
        }
    }
    private List<CrasJobLst>  convertJobFileListToCrasJobLstList(String factory, String resId, List<JobFile> jobFileList){

        List<CrasJobLst> result = new ArrayList<>();
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDateTime = dateFormat.format(currentDate);

        return jobFileList.stream().map(file->
                CrasJobLst.builder().id(new CrasJobLstId(factory, resId
                                ,Optional.ofNullable(file.getDevice()).filter(str->!str.trim().isEmpty()).orElse("NA")
                                ,Optional.ofNullable(file.getLayer()).filter(str->!str.trim().isEmpty()).orElse("NA")))
                        .updateTime(formattedDateTime)
                        .lastModifyTime(Optional.ofNullable(file.getTimeStamp()).orElse("NA"))
                        .deleteFlag(" ")
                        .cmf_1(" ")
                        .cmf_2(" ")
                        .cmf_3(" ")
                        .cmf_4(" ")
                        .cmf_5(" ")
                        .build()).collect(Collectors.toList());
    }
    @Async(value ="asyncTask")
    public void checkFileExists(FtpServer server, List<FtpProcess> ftpProcessList){
        switch(server.getCheckType()){
            case JOB_FILE -> {
                FtpProcess<JobFile> ftpProcess = new FtpJobFileProcess();
                try{
                    if (!ftpProcess.getFtpProcess(server)){
                        return;
                    }
                    ftpProcessList.add(ftpProcess);
                    //장비별 FTP 파일 수집.
                    List<JobFile> jobFileList = ftpProcess.actionFromType();
                    if (jobFileList == null || jobFileList.isEmpty()) {
                        log.warn("FTP 파일 없음 또는 실패: {}", server.getName());
                        return;
                    }
                    log.info("[{}] {} 수집 시작 ({}개)", Thread.currentThread().getName(), server.getName(), jobFileList.size());
                    putJobFileSend(server.getName(), jobFileList);
                    List<CrasJobLst> crasJobLsts = convertJobFileListToCrasJobLstList("AFB1", server.getName(), jobFileList);
                    crasJobLstService.refreshData("AFB1", server.getName(), crasJobLsts);
                    log.info("[{}] {} 저장 완료 ({}개)", Thread.currentThread().getName(), server.getName(), crasJobLsts.size());
                }catch(Exception e){
                    log.error("FTP 처리 중 예외 발생: {}", server.getName(), e);
                }
                finally {
                    if (ftpProcess.isConnect()) {
                        ftpProcess.close();
                    }
                }
                    //Optional<List<JobFile>> jobFileList = Optional.ofNullable(ftpProcess.actionFromType());
                    //ftpProcess.close();


   /*                 jobFileList.filter(list->!list.isEmpty())
                            .ifPresentOrElse(
                                    list-> {
                                        System.out.println("Server Name" + server.getName()  + " > " + Thread.currentThread().getName());
                                        try {
                                            System.out.println("Server Name" + server.getName()  + " list count : " + list.size());
                                            //blockingQueueList.put(Map.of(server.getName(), list));
                                            //ftpProcess.close();
                                            log.info(String.format("%s [START] MES Server Send 및 DataBase Insert (Count:%d) ", server.getName(), list.size()));
                                            putJobFileSend(server.getName(), list);
                                            List<CrasJobLst> crasJobLsts = convertJobFileListToCrasJobLstList("AFB1", server.getName(), list);
                                            crasJobLstService.refreshData("AFB1", server.getName(), crasJobLsts);
                                            log.info("[END] MES Server Send 및 DataBase Insert : " + server.getName() + "COUNT:" + crasJobLsts.size());
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }finally {
                                            if (ftpProcess.isConnect()){
                                                ftpProcess.close();
                                            }
                                        }
                                        //this.putJobFileSend(server.getName(), list);
                                    },
                                    ()-> System.out.println(("ERROR"))
                            );*/

            }
        }
    }
}
