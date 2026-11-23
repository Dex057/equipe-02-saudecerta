package br.edu.ifpa.saudecerta.shared.excecoes;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/** Traduz exceções para o envelope de erro e o status HTTP do contrato (400, 404, 409, 422, 500). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<RespostaErro> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<RespostaErro> conflito(ConflitoException e) {
        return resposta(HttpStatus.CONFLICT, "CONFLITO", e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    ResponseEntity<RespostaErro> regraDeNegocio(RegraDeNegocioException e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<RespostaErro> validacao(MethodArgumentNotValidException e) {
        String campos = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO", campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<RespostaErro> requisicaoInvalida(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA",
                "Requisição malformada ou com parâmetro inválido.");
    }

@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
ResponseEntity<RespostaErro> metodoNaoPermitido(
        HttpRequestMethodNotSupportedException e) {

    HttpHeaders headers = new HttpHeaders();

    if (e.getSupportedHttpMethods() != null) {
        headers.setAllow(e.getSupportedHttpMethods());
    }

    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .headers(headers)
        .body(RespostaErro.de(
            "METODO_NAO_PERMITIDO",
            "Metodo HTTP nao permitido para este recurso."
        ));
}


    @ExceptionHandler(Exception.class)
    ResponseEntity<RespostaErro> inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro interno.");
    }

    private static ResponseEntity<RespostaErro> resposta(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(RespostaErro.de(codigo, mensagem));
    }
}
