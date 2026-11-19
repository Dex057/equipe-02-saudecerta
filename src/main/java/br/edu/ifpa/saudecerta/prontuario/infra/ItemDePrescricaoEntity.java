
package br.edu.ifpa.saudecerta.prontuario.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pro_item_prescricao")
public class ItemDePrescricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescricao_id", nullable = false)
    private Long prescricaoId;

    @Column(name = "medicamento", nullable = false, length = 255)
    private String medicamento;

    @Column(name = "dosagem", nullable = false, length = 255)
    private String dosagem;

    @Column(name = "posologia", nullable = false, length = 500)
    private String posologia;

    @Column(name = "duracao", nullable = false, length = 255)
    private String duracao;

    protected ItemDePrescricaoEntity() {
        // Exigido pelo JPA
    }

    public ItemDePrescricaoEntity(
            Long prescricaoId,
            String medicamento,
            String dosagem,
            String posologia,
            String duracao) {

        if (prescricaoId == null || prescricaoId <= 0) {
            throw new IllegalArgumentException(
                "Prescricao invalida.");
        }

        this.prescricaoId = prescricaoId;
        this.medicamento = validarTexto(medicamento);
        this.dosagem = validarTexto(dosagem);
        this.posologia = validarTexto(posologia);
        this.duracao = validarTexto(duracao);
    }

    private static String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                "Campo obrigatorio.");
        }
        return texto.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getPrescricaoId() {
        return prescricaoId;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public String getDosagem() {
        return dosagem;
    }

    public String getPosologia() {
        return posologia;
    }

    public String getDuracao() {
        return duracao;
    }
}
