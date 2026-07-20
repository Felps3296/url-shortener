package com.felipereis.urlshortener.repository;

import com.felipereis.urlshortener.model.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UrlRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;

    @Test
    void deveRetornarTrueQuandoCodigoCurtoJaExiste() {
        // Arrange: insere uma URL direto no banco
        Url url = new Url("https://google.com", "existe1");
        urlRepository.salvar(url);

        // Act
        boolean existe = urlRepository.existeCodigoCurto("existe1");

        // Assert
        assertThat(existe).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoCodigoCurtoNaoExiste() {
        boolean existe = urlRepository.existeCodigoCurto("naoexiste999");

        assertThat(existe).isFalse();
    }

    @Test
    void deveSalvarUrlEGerarIdAutomaticamente() {
        Url url = new Url("https://github.com", "gh12345");

        Url urlSalva = urlRepository.salvar(url);

        assertThat(urlSalva.getId()).isNotNull();
        assertThat(urlSalva.getUrlOriginal()).isEqualTo("https://github.com");
    }

    @Test
    void deveBuscarUrlPorCodigoCurtoQuandoExiste() {
        urlRepository.salvar(new Url("https://linkedin.com", "li98765"));

        Optional<Url> resultado = urlRepository.buscarPorCodigoCurto("li98765");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUrlOriginal()).isEqualTo("https://linkedin.com");
    }

    @Test
    void deveRetornarOptionalVazioQuandoCodigoNaoExiste() {
        Optional<Url> resultado = urlRepository.buscarPorCodigoCurto("codigoinexistente");

        assertThat(resultado).isEmpty();
    }
}