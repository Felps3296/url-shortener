package com.felipereis.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EncurtarUrlRequest {
    private String urlOriginal;
    private String alias;
}