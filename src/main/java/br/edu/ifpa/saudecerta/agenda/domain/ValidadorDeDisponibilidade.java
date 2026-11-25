package br.edu.ifpa.saudecerta.agenda.domain;

import br.edu.ifpa.saudecerta.shared.excecoes.RegraDeNegocioException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ValidadorDeDisponibilidade {

    private static final LocalTime INICIO = LocalTime.of(8, 0);
    private static final LocalTime FIM = LocalTime.of(17, 0);
    private static final int DURACAO_MINUTOS = 30;

    public void validar(LocalDateTime dataHora) {
        if (!gerarHorarios(dataHora.toLocalDate()).contains(dataHora)) {
            throw new RegraDeNegocioException("O horário não pertence à disponibilidade do profissional.");
        }
    }

    public List<LocalDateTime> gerarHorarios(LocalDate data) {
        if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }
        List<LocalDateTime> horarios = new ArrayList<>();
        for (LocalTime horario = INICIO; horario.isBefore(FIM); horario = horario.plusMinutes(DURACAO_MINUTOS)) {
            horarios.add(LocalDateTime.of(data, horario));
        }
        return List.copyOf(horarios);
    }
}
