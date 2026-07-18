package com.felipereis.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlResponseDTO {
    private String urlOriginal;
    private String codigoCurto;
    private String urlEncurtada;
}