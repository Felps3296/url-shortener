package com.felipereis.urlshortener.exception;

public class AliasIndisponivelException extends RuntimeException {
    public AliasIndisponivelException(String alias) {
        super("O alias '" + alias + "' já está em uso.");
    }
}