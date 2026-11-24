package br.edu.ifpa.saudecerta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Sobe o contexto inteiro (Spring, JPA com validate, Flyway e H2). Falha se a configuração estiver quebrada. */
@SpringBootTest
class SaudeCertaApplicationTests {

    @Autowired
    Clock clock;

    @Test
    void contextoSobeComRelogioInjetavel() {
        assertThat(clock).isNotNull();
    }
}
