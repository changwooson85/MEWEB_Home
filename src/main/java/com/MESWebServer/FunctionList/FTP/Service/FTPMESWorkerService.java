package com.MESWebServer.FunctionList.FTP.Service;

import com.MESWebServer.DTO.JobFile;
import com.MESWebServer.component.H101Service;
import com.MESWebServer.entity.Real.CrasJobLst;
import com.MESWebServer.entity.Real.CrasJobLstId;
import com.MESWebServer.h101.Core.MESWEBCaster;
import com.MESWebServer.h101.Core.MESWEBType;
import com.MESWebServer.repository.Real.CrasJobLstRepository;
import com.MESWebServer.service.CrasJobLstService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@Slf4j
@Service
@AllArgsConstructor
public class FTPMESWorkerService implements Runnable{
    private final  BlockingQueue<Map<String, List<JobFile>>> blockingQueueList;
    private final CrasJobLstService crasJobLstService;
    private final H101Service h101Service;

/*    FTPMESWorkerService(BlockingQueue<Map<String, List<JobFile>>> blockingQueue, CrasJobLstService crasJobLstService){
        this.blockingQueueList = blockingQueue;
        this.crasJobLstService = crasJobLstService;
    }*/

    @Override
    public void run() {
        //ExecutorService executorService = Executors.newFixedThreadPool(5);

        while(!Thread.currentThread().isInterrupted()){
            try{
                Map<String,List<JobFile>> jobFileList = blockingQueueList.take();
                log.info("jobFileList jobCount : ", jobFileList.size());
                for(Map.Entry<String, List<JobFile>> e : jobFileList.entrySet()){
                    log.info(String.format("%s [START] MES Server Send 및 DataBase Insert", e.getKey()));
                    putJobFileSend(e.getKey(), e.getValue());
                    List<CrasJobLst> crasJobLsts = convertJobFileListToCrasJobLstList("AFB1", e.getKey(), e.getValue());
                    crasJobLstService.refreshData("AFB1", e.getKey(), crasJobLsts);
                    log.info("[END] MES Server Send 및 DataBase Insert : " + e.getKey() + "COUNT:" + crasJobLsts.size());
                }
                //executorService.shutdown();
            } catch (InterruptedException e) {
                log.info("[FAIL] MES Server Send 및 DataBase Insert:" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
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
}
