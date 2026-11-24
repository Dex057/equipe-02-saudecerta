package br.edu.ifpa.saudecerta.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regras de dependência verificáveis (arquitetura.md, seção 3.1; ADR-001 e ADR-003).
 * Valem para todos os módulos desde o primeiro commit: se uma regra quebrar, o PR não passa.
 */
class ArquiteturaTest {

    private static final String RAIZ = "br.edu.ifpa.saudecerta";
    private static final List<String> MODULOS = List.of("cadastro", "agenda", "prontuario", "seguranca", "relatorios");

    private static JavaClasses classes;

    @BeforeAll
    static void importa() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ);
    }

    /** Regra 1: o domínio é Java puro. */
    @Test
    void dominioNaoDependeDeJpaNemDeSpringDataNemDeSpringWeb() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..", "org.springframework.data..", "org.springframework.web..")
                .allowEmptyShould(true)
                .check(classes);
    }

    /** Regra 2: api só chama application; nunca infra nem domain. */
    @Test
    void apiNaoAcessaInfraNemDomain() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAnyPackage("..infra..", "..domain..")
                .allowEmptyShould(true)
                .check(classes);
    }

    /** Regra 3: um módulo só usa de outro a interface pública (*Api e *Resumo, na raiz do módulo). */
    @Test
    void modulosSoSeFalamPelaInterfacePublica() {
        for (String origem : MODULOS) {
            for (String destino : MODULOS) {
                if (origem.equals(destino)) {
                    continue;
                }
                noClasses().that().resideInAPackage(RAIZ + "." + origem + "..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                RAIZ + "." + destino + ".api..", RAIZ + "." + destino + ".application..",
                                RAIZ + "." + destino + ".domain..", RAIZ + "." + destino + ".infra..")
                        .allowEmptyShould(true)
                        .because(origem + " não pode acessar o interior de " + destino)
                        .check(classes);
            }
        }
    }

    /** O pacote shared é a base de todos: não pode depender de nenhum módulo (evita ciclos). */
    @Test
    void sharedNaoDependeDeModulos() {
        noClasses().that().resideInAPackage(RAIZ + ".shared..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        RAIZ + ".cadastro..", RAIZ + ".agenda..", RAIZ + ".prontuario..",
                        RAIZ + ".seguranca..", RAIZ + ".relatorios..")
                .allowEmptyShould(true)
                .check(classes);
    }
}
