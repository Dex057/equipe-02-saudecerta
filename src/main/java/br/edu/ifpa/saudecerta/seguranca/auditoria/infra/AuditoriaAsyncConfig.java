package br.edu.ifpa.saudecerta.seguranca.auditoria.infra;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Executor da auditoria de leitura (ADR-002): fila limitada com política caller-runs, para nunca descartar um
 * evento por estouro de fila, e drenagem no desligamento (graceful shutdown).
 */
@Configuration
@EnableAsync
class AuditoriaAsyncConfig {

    @Bean("auditoriaExecutor")
    ThreadPoolTaskExecutor auditoriaExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(200);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setThreadNamePrefix("auditoria-");
        return executor;
    }
}
