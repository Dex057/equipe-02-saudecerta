package br.edu.ifpa.saudecerta.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** ADR-006, item 6: toda entidade JPA declara @Table com o prefixo do módulo dono (cad_, age_, pro_, seg_, aud_). */
class TabelasPorModuloTest {

    private static final String RAIZ = "br.edu.ifpa.saudecerta";
    private static final Map<String, String> PREFIXOS = Map.of(
            "cadastro", "cad_", "agenda", "age_", "prontuario", "pro_", "seguranca", "seg_");

    @Test
    void entidadesUsamOPrefixoDoModuloDono() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ);

        classes().that().areAnnotatedWith(Entity.class)
                .should(new ArchCondition<JavaClass>("declarar @Table com o prefixo do módulo dono") {
                    @Override
                    public void check(JavaClass entidade, ConditionEvents eventos) {
                        String prefixo = prefixoEsperado(entidade.getPackageName());
                        String tabela = entidade.tryGetAnnotationOfType(Table.class).map(Table::name).orElse("");
                        eventos.add(new SimpleConditionEvent(entidade, tabela.startsWith(prefixo),
                                entidade.getName() + ": @Table(name = \"" + tabela + "\") deveria começar com " + prefixo));
                    }
                })
                .allowEmptyShould(true)
                .check(classes);
    }

    private static String prefixoEsperado(String pacote) {
        if (pacote.startsWith(RAIZ + ".seguranca.auditoria")) {
            return "aud_";
        }
        String modulo = pacote.substring(RAIZ.length() + 1).split("\\.")[0];
        return PREFIXOS.getOrDefault(modulo, "?_");
    }
}
