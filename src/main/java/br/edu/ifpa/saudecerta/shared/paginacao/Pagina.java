package br.edu.ifpa.saudecerta.shared.paginacao;

import java.util.List;
import java.util.function.Function;

/** Página de resultados, independente de Spring Data (o domínio não pode importá-lo). */
public record Pagina<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <T> Pagina<T> de(List<T> conteudo, int pagina, int tamanho, long totalElementos) {
        int totalPaginas = tamanho == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanho);
        return new Pagina<>(List.copyOf(conteudo), pagina, tamanho, totalElementos, totalPaginas);
    }

    public <R> Pagina<R> mapear(Function<T, R> funcao) {
        return new Pagina<>(conteudo.stream().map(funcao).toList(), pagina, tamanho, totalElementos, totalPaginas);
    }
}
