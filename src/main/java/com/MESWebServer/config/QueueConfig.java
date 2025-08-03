package com.MESWebServer.config;

import com.MESWebServer.DTO.JobFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class QueueConfig {

    @Bean
    public BlockingQueue<List<JobFile>> taskQueue(){
        return new LinkedBlockingQueue<List<JobFile>>();
    }
    @Bean
    public BlockingQueue<Map<String, List<JobFile>>> mapBlockingQueue(){
        return new LinkedBlockingQueue<Map<String, List<JobFile>>>();
    }
}
