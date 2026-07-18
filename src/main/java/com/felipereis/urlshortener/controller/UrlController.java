package com.felipereis.urlshortener.controller;

import com.felipereis.urlshortener.dto.EncurtarUrlRequest;
import com.felipereis.urlshortener.dto.UrlResponseDTO;
import com.felipereis.urlshortener.model.Url;
import com.felipereis.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<UrlResponseDTO> encurtar(@RequestBody EncurtarUrlRequest request) {
        Url url = urlService.encurtar(request.getUrlOriginal(), request.getAlias());
        return ResponseEntity.ok(new UrlResponseDTO(
                url.getUrlOriginal(),
                url.getCodigoCurto(),
                "http://localhost:8080/" + url.getCodigoCurto()
        ));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Void> redirecionar(@PathVariable String codigo) {
        Url url = urlService.buscarUrlOriginal(codigo);
        return ResponseEntity.status(302).header("Location", url.getUrlOriginal()).build();
    }
}