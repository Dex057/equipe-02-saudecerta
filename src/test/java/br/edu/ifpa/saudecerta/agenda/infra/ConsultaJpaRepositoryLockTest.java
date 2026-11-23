package br.edu.ifpa.saudecerta.agenda.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

class ConsultaJpaRepositoryLockTest {

    @Test
    void onlyUpdateLookupUsesPessimisticWriteLock() throws NoSuchMethodException {
        Lock lock = ConsultaJpaRepository.class
                .getMethod("buscarParaAtualizacao", Long.class)
                .getAnnotation(Lock.class);

        assertNotNull(lock);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, lock.value());
        assertNull(ConsultaJpaRepository.class
                .getMethod("findById", Object.class)
                .getAnnotation(Lock.class));
    }
}
