package com.MESWebServer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncTask")
    public Executor  asyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); //기본 스레드 사이즈를 설정한다
        executor.setMaxPoolSize(10); //최대 스레드 사이즈를 설정한다
        executor.setQueueCapacity(20); //스레드 대기 큐의 사이즈를 설정한다
        executor.setKeepAliveSeconds(120);//해당 초까지 idle상태가 유지되면 스레드를 종료한다 (idle:어떠한 동작 상태도 아닐때)
        executor.setAllowCoreThreadTimeOut(true); // setKeepAliveSeconds에 대한 활성화 여부
        executor.setPrestartAllCoreThreads(true); // 작업 이전에 스레드를 활성화 시킬지에 대한 여부
        executor.setWaitForTasksToCompleteOnShutdown(true);//진행중이던 작업이 완료 된 후 스레드를 종료시킬지에 대한 여부
        executor.setAwaitTerminationSeconds(20); //작업을 몇초동안 기다려줄지에 대한 여부
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());//스레드 작업중 예외가 발생했을대 핸들링 여부
        executor.setThreadNamePrefix("async-Thread");// 스레드의 이름 지정
        executor.initialize();//
        return executor;
    }
}
