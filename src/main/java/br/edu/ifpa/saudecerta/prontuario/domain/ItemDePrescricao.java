
package br.edu.ifpa.saudecerta.prontuario.domain;

public final class ItemDePrescricao {

    private final Long id;
    private final String medicamento;
    private final String dosagem;
    private final String posologia;
    private final String duracao;

    public ItemDePrescricao(
            Long id,
            String medicamento,
            String dosagem,
            String posologia,
            String duracao) {

        this.id = id;
        this.medicamento = validarTexto(medicamento, "Medicamento");
        this.dosagem = validarTexto(dosagem, "Dosagem");
        this.posologia = validarTexto(posologia, "Posologia");
        this.duracao = validarTexto(duracao, "Duração");
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                campo + " é obrigatório.");
        }
        return valor.trim();
    }

    public Long getId() {
        return id;
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
