package com.felipereis.urlshortener.controller;

import com.felipereis.urlshortener.dto.EncurtarUrlRequest;
import com.felipereis.urlshortener.dto.UrlResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void deveEncurtarUrlERedirecionarComSucesso() {
        // Arrange
        //objeto que seria enviado pelo cliente
        EncurtarUrlRequest request = new EncurtarUrlRequest("https://www.google.com", null);
        String urlEncurtar = "http://localhost:" + port + "/api/urls";

        // Act: encurta a URL
        ResponseEntity<UrlResponseDTO> respostaEncurtar =
                restTemplate.postForEntity(urlEncurtar, request, UrlResponseDTO.class);

        // Assert: Verifica se o POST funcionou
        assertThat(respostaEncurtar.getStatusCode()).isEqualTo(HttpStatus.OK);
        //Confirma que veio um corpo na resposta.
        assertThat(respostaEncurtar.getBody()).isNotNull();

        String codigoCurto = respostaEncurtar.getBody().getCodigoCurto();
        //teste apenas verifica que ele possui 6 caracteres.
        assertThat(codigoCurto).hasSize(6);

        // Monta a URL do endpoint GET usando o código curto gerado no POST
        String urlRedirecionar = "http://localhost:" + port + "/api/urls/" + codigoCurto;

        // Faz uma requisição GET
        ResponseEntity<Void> respostaRedirecionar =
                restTemplate.getForEntity(urlRedirecionar, Void.class);

        // Verifica se a API respondeu com HTTP 302
        assertThat(respostaRedirecionar.getStatusCode()).isEqualTo(HttpStatus.FOUND);

        // Verifica se o header Location aponta para a URL original
        assertThat(respostaRedirecionar.getHeaders().getLocation().toString())
                .isEqualTo("https://www.google.com");
    }

    @Test
    void deveRetornar409QuandoAliasJaEstaEmUso() {
        EncurtarUrlRequest request = new EncurtarUrlRequest("https://www.google.com", "repetido");
        String url = "http://localhost:" + port + "/api/urls";

        // Primeira vez: cria com sucesso
        restTemplate.postForEntity(url, request, UrlResponseDTO.class);

        // Segunda vez: mesmo alias, deve falhar
        ResponseEntity<String> resposta = restTemplate.postForEntity(url, request, String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).contains("repetido");
    }

    @Test
    void deveRetornar404QuandoCodigoNaoExiste() {
        String url = "http://localhost:" + port + "/api/urls/naoexiste123";

        ResponseEntity<String> resposta = restTemplate.getForEntity(url, String.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}