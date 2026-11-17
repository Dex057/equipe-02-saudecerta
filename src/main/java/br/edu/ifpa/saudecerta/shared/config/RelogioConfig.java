package br.edu.ifpa.saudecerta.shared.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio injetável: as regras que dependem de "hoje" (RN03) recebem o Clock, e o teste usa um Clock fixo. */
@Configuration
public class RelogioConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Belem"));
    }
}
