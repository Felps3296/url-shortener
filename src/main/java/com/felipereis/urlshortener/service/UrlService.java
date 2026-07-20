package com.felipereis.urlshortener.service;

import com.felipereis.urlshortener.exception.AliasIndisponivelException;
import com.felipereis.urlshortener.exception.UrlNaoEncontradaException;
import com.felipereis.urlshortener.model.Url;
import com.felipereis.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UrlService {

    private static final String CARACTERES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TAMANHO_CODIGO = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository urlRepository;

    //Vou usar um alias do usuário ou vou gerar um código?
    public synchronized Url encurtar(String urlOriginal, String aliasPersonalizado) {
        String codigoCurto = (aliasPersonalizado != null && !aliasPersonalizado.trim().isEmpty())
                ? validarAliasDisponivel(aliasPersonalizado)
                : gerarCodigoUnico();

        return urlRepository.salvar(new Url(urlOriginal, codigoCurto));
    }

    //Procura a URL pelo código curto. Devolve a URL original
    public Url buscarUrlOriginal(String codigoCurto) {
        return urlRepository.buscarPorCodigoCurto(codigoCurto)
                .orElseThrow(() -> new UrlNaoEncontradaException(codigoCurto));
    }

    //valida um alias informado pelo usuário
    private String validarAliasDisponivel(String alias) {
        if (urlRepository.existeCodigoCurto(alias)) {
            throw new AliasIndisponivelException(alias);
        }
        return alias;
    }

    //Pergunta ao banco: "esse código já existe?" Se JÁ EXISTE → volta pro passo 1, sorteia outro
    private String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = gerarCodigoAleatorio();
        } while (urlRepository.existeCodigoCurto(codigo));
        return codigo;
    }

    //cria um código aleatório
    private String gerarCodigoAleatorio() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }
}