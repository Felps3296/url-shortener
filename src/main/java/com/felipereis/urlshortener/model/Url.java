package com.felipereis.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Url {
    private Long id;
    private String urlOriginal;
    private String codigoCurto;
    private LocalDateTime dataCriacao;

    public Url(String urlOriginal, String codigoCurto) {
        this.urlOriginal = urlOriginal;
        this.codigoCurto = codigoCurto;
    }
}