package br.edu.ifpa.saudecerta.cadastro.application;

import br.edu.ifpa.saudecerta.cadastro.CadastroApi;
import br.edu.ifpa.saudecerta.cadastro.PacienteResumo;
import br.edu.ifpa.saudecerta.cadastro.ProfissionalResumo;
import br.edu.ifpa.saudecerta.cadastro.domain.PacienteRepository;
import br.edu.ifpa.saudecerta.cadastro.domain.ProfissionalRepository;
import java.util.Optional;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa a interface pública do módulo, usada por Agenda e Prontuário. */
@Service
public class CadastroApiService implements CadastroApi {

    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;

    public CadastroApiService(PacienteRepository pacientes, ProfissionalRepository profissionais) {
        this.pacientes = pacientes;
        this.profissionais = profissionais;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PacienteResumo> buscarPaciente(Long id) {
        return pacientes.buscarPorId(id).map(p -> new PacienteResumo(p.id(), p.nome(), p.ativo()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfissionalResumo> buscarProfissional(Long id) {
        return profissionais.buscarPorId(id).map(p -> new ProfissionalResumo(
                p.id(), p.nome(), java.util.List.copyOf(new TreeSet<>(p.especialidadeIds())), p.ativo()));
    }
}
