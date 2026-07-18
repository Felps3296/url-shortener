package com.felipereis.urlshortener.exception;

public class UrlNaoEncontradaException extends RuntimeException {
    public UrlNaoEncontradaException(String codigo) {
        super("Nenhuma URL encontrada para o codigo: " + codigo);
    }
}