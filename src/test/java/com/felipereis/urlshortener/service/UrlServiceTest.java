package com.felipereis.urlshortener.service;

import com.felipereis.urlshortener.exception.AliasIndisponivelException;
import com.felipereis.urlshortener.exception.UrlNaoEncontradaException;
import com.felipereis.urlshortener.model.Url;
import com.felipereis.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    private UrlService urlService;

    //Sem alias, o Service gera um código de 6 caracteres.
    @Test
    void deveEncurtarUrlComCodigoGeradoQuandoNaoInformaAlias() {
        // Arrange
        urlService = new UrlService(urlRepository);

        when(urlRepository.existeCodigoCurto(anyString())).thenReturn(false);
        when(urlRepository.salvar(any(Url.class))).thenAnswer(invocation -> {
            Url url = invocation.getArgument(0);
            url.setId(1L);
            return url;
        });

        // Act
        Url resultado = urlService.encurtar("https://google.com", null);

        // Assert
        //A URL original ficou correta?
        assertThat(resultado.getUrlOriginal()).isEqualTo("https://google.com");
        //tem 6 caracteres?
        assertThat(resultado.getCodigoCurto()).hasSize(6);
    }

    //Com um alias disponível, ele usa exatamente esse alias como código curto.
    @Test
    void deveEncurtarUrlComAliasPersonalizadoQuandoDisponivel() {
        urlService = new UrlService(urlRepository);

        when(urlRepository.existeCodigoCurto("meu-link")).thenReturn(false);
        when(urlRepository.salvar(any(Url.class))).thenAnswer(invocation -> {
            Url url = invocation.getArgument(0);
            url.setId(2L);
            return url;
        });

        Url resultado = urlService.encurtar("https://google.com", "meu-link");

        assertThat(resultado.getCodigoCurto()).isEqualTo("meu-link");
    }

    //Se o alias já estiver em uso, o Service lança AliasIndisponivelException.
    @Test
    void deveLancarExcecaoQuandoAliasJaExiste() {
        urlService = new UrlService(urlRepository);

        when(urlRepository.existeCodigoCurto("ja-existe")).thenReturn(true);

        assertThatThrownBy(() -> urlService.encurtar("https://google.com", "ja-existe"))
                .isInstanceOf(AliasIndisponivelException.class)
                .hasMessageContaining("ja-existe");
    }

    //Ao buscar um código existente, o Service retorna a Url correspondente.
    @Test
    void deveRetornarUrlQuandoCodigoExiste() {
        urlService = new UrlService(urlRepository);

        Url urlSalva = new Url("https://google.com", "abc123");
        urlSalva.setId(1L);

        //Quando alguém chamar
        when(urlRepository.buscarPorCodigoCurto("abc123")).thenReturn(Optional.of(urlSalva));

        Url resultado = urlService.buscarUrlOriginal("abc123");

        assertThat(resultado.getUrlOriginal()).isEqualTo("https://google.com");
    }

    //Ao buscar um código inexistente, o Service lança UrlNaoEncontradaException.
    @Test
    void deveLancarExcecaoQuandoCodigoNaoExiste() {
        urlService = new UrlService(urlRepository);

        when(urlRepository.buscarPorCodigoCurto("naoexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.buscarUrlOriginal("naoexiste"))
                .isInstanceOf(UrlNaoEncontradaException.class);
    }
}