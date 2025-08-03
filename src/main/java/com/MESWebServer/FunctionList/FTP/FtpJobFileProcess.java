package com.MESWebServer.FunctionList.FTP;

import com.MESWebServer.DTO.JobFile;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.miracom.oneoone.transceiverx.pending.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class FtpJobFileProcess extends FtpProcess<JobFile> {

    List<JobFile> jobFileList = new ArrayList<>();
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    @Override
    public List<JobFile> actionFromType() {
        return getJobFile();
    }
    /// Job File 참조 로직은 두번의 폴더 구조에서 Data 생성
    private List<JobFile> getJobFile()
    {
        long start = System.currentTimeMillis();
        //data 구조 > 장비 + device + layer
        try {

            client.changeWorkingDirectory(ftpServer.getFilePath());
            FTPFile[] topLevelFiles = client.listFiles(); // 이건 그대로 유지 (디렉토리 목록용)

            long folderCount = Arrays.stream(topLevelFiles)
                    .filter(FTPFile::isDirectory)
                    .count();

            // 포트 사용량 체크
            PortUsageInfo portUsage = getRemotePortUsage(this.ftpServer.getUser(), this.ftpServer.getPassword(), this.ftpServer.getServer());
            int estimatedTotal = (int) (folderCount * 1.2) + portUsage.timeWait + portUsage.established + portUsage.listen;

            log.info("사용중 포트 정보: {}", portUsage);
            log.info("예상 포트 수 (폴더 기반): {} + 현재 사용중 {} = {}", (int) (folderCount * 1.2), portUsage.total, estimatedTotal);

            if (estimatedTotal > 20000) {
                log.warn("포트 수 임계값 초과 → FTP 탐색 중단: {} > {}", estimatedTotal, 20000);
                return jobFileList;
            }

            for (FTPFile deviceDir : topLevelFiles) {
                if (!deviceDir.isDirectory()) continue;

                String deviceName = deviceDir.getName();
                if (deviceName.length() != 4) continue;

                try {
                    if (!client.changeWorkingDirectory(deviceName)) {
                        log.warn("디렉토리 진입 실패: {}", deviceName);
                        continue;
                    }

                    FTPFile[] subFiles = client.listFiles();
//                    try {
//                        subFiles = client.mlistDir(); // 성능 개선 핵심
//                    } catch (IOException e) {
//                        log.warn("MLSD 명령 실패 {}", deviceName);
//                        subFiles = client.listFiles(); // fallback 처리
//                    }

                    for (FTPFile layerFile : subFiles) {
                        if (layerFile.getName().startsWith(".")) continue;//숨김파일 처리
                        if (layerFile.getName().contains(deviceName)) {
                            String layer = layerPatternSplit(layerFile.getName());
                            String timestamp = dataFormat.format(layerFile.getTimestamp().getTime());
                            jobFileList.add(new JobFile(deviceName, layer, timestamp));
                        }
                    }

                    client.changeToParentDirectory();

                } catch (IOException e) {
                    log.warn("장비 처리 오류 - {}: {}", deviceName, e.getMessage(), e);
                }
            }

            return jobFileList;

        } catch (IOException e) {
            log.error("FTP 디렉토리 접근 실패: {}", ftpServer.getFilePath(), e);
            return Collections.emptyList();
        }
        finally {
            log.info("FTP 전체 탐색 소요 시간: {} ms", System.currentTimeMillis() - start);
        }
/*            Arrays.stream(client.listFiles()).forEach(deviceFile->{
                if (deviceFile.isDirectory()){
                    try {
                        //client.changeWorkingDirectory(ftpServer.getFilePath() +"\\" + deviceFile.getName());
                        FTPFile[] subFiles = client.listFiles(deviceFile.getName());
                        Stream.of(subFiles).forEach(layerFile-> {
                            if (layerFile.getName().contains(deviceFile.getName())) {
                                String layer;
                                layer = layerPatternSplit(layerFile.getName());
                                jobFileList.add(new JobFile(deviceFile.getName(),layer, dataFormat.format(layerFile.getTimestamp().getTime())));
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return jobFileList;*/

    }
    private String layerPatternSplit(String target){
        //Pattern pattern = Pattern.compile("[-](.*?)[.]");
        Pattern pattern =  Pattern.compile("-(\\w+)(?=\\.\\w*|$)"); 
        Matcher matcher = pattern.matcher(target);
        if(matcher.find()){
            return matcher.group(1).trim();
            //return matcher.group(1).trim();
        }
        return"";
    }

    public int getRemoteTimeWaitCount(String user, String password, String host) {
        int count = -1;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10000);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("netstat -an | grep TIME_WAIT | wc -l");
            channel.setInputStream(null);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            channel.setOutputStream(output);

            channel.connect();
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            String result = output.toString().trim();
            count = Integer.parseInt(result);

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            log.warn("TIME_WAIT 수 조회 실패: {}", e.getMessage());
        }
        return count;
    }

    class PortUsageInfo {
        int timeWait;
        int established;
        int listen;
        int total;

        @Override
        public String toString() {
            return String.format("TIME_WAIT: %d, ESTABLISHED: %d, LISTEN: %d, TOTAL: %d", timeWait, established, listen, total);
        }
    }

    public PortUsageInfo getRemotePortUsage(String user, String password, String host) {
        PortUsageInfo info = new PortUsageInfo();
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10000);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            // 각 상태별 개수 파악 (Solaris netstat 대응)
            /*String command = ""
                    + "TW=$(netstat -an | grep TIME_WAIT | wc -l); "
                    + "ES=$(netstat -an | grep ESTABLISHED | wc -l); "
                    + "LI=$(netstat -an | grep LISTEN | wc -l); "
                    //+ "TOTAL=$(netstat -an | grep '\\.[0-9]* ' | awk '{print $4}' | awk -F. '{print $NF}' | sort -n | uniq | wc -l); "
                    + "echo \"$TW,$ES,$LI\"";*/
            String command = "netstat -an | grep -c ESTABLISHED; netstat -an | grep -c TIME_WAIT; netstat -an | grep -c LISTEN";

            channel.setCommand(command);
            channel.setInputStream(null);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            channel.setOutputStream(output);
            channel.connect();

            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            String[] parts = output.toString().trim().split("\n");
            info.timeWait = Integer.parseInt(parts[1]);
            info.established = Integer.parseInt(parts[0]);
            info.listen = Integer.parseInt(parts[2]);
            //info.total = Integer.parseInt(parts[3]);

            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            log.warn("포트 사용량 조회 실패: {}", e.getMessage());
        }
        return info;
    }
}
