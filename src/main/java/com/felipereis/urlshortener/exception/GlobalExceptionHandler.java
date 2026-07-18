package com.felipereis.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AliasIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> handleAliasIndisponivel(AliasIndisponivelException ex) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UrlNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleUrlNaoEncontrada(UrlNaoEncontradaException ex) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> montarResposta(HttpStatus status, String mensagem) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("mensagem", mensagem);
        return ResponseEntity.status(status).body(body);
    }
}