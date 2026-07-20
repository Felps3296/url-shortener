# URL Shortener — Desafio Técnico Topaz

Encurtador de URLs desenvolvido como desafio técnico para a vaga de Desenvolvedor Java na Topaz.

## Sobre o projeto

A aplicação recebe uma URL e devolve um código curto único, que redireciona para a URL original quando acessado. O usuário pode escolher um alias personalizado ou deixar o sistema gerar um código aleatório automaticamente.

## Arquitetura

Arquitetura em camadas (MVC clássico), sem uso de JPA/ORM:

```
Controller → Service → Repository (JdbcTemplate) → PostgreSQL
```

- **Controller**: expõe os endpoints REST
- **Service**: contém a regra de negócio (geração de código, validação de alias)
- **Repository**: acesso ao banco via JdbcTemplate, com SQL explícito
- **Model**: representa a entidade `Url`
- **DTOs**: objetos de entrada e saída da API, separados da entidade
- **Exception**: exceções customizadas + handler global para respostas HTTP consistentes

## Stack técnica

Java 8, Spring Boot 2.7.18, Spring JDBC (JdbcTemplate), PostgreSQL, Docker e Docker Compose, Lombok, JUnit 5, Mockito, GitHub Actions (CI)

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/urls` | Encurta uma URL. Aceita `alias` opcional no corpo da requisição |
| `GET` | `/api/urls/{codigo}` | Redireciona (302) para a URL original |

### Exemplo de requisição

```json
POST /api/urls
{
  "urlOriginal": "https://www.google.com",
  "alias": null
}
```

```json
{
  "urlOriginal": "https://www.google.com",
  "codigoCurto": "aB93xK",
  "urlEncurtada": "http://localhost:8080/aB93xK"
}
```

## Como rodar

O projeto pode ser executado de duas formas: local (IntelliJ) ou totalmente containerizado (Docker). As duas usam o mesmo banco de dados.

### Opção 1, rodando local (IntelliJ)

Sobe só o banco de dados:

```bash
cd infra
docker compose up -d postgres
```

Cria a tabela (primeira execução):

```bash
docker exec -it url-shortener-postgres psql -U url_app -d urlshortener
```

```sql
CREATE TABLE urls (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url_original    TEXT            NOT NULL,
    codigo_curto    VARCHAR(10)     NOT NULL UNIQUE,
    data_criacao    TIMESTAMP       NOT NULL DEFAULT NOW()
);
```

Roda a aplicação pela IDE. A API fica disponível em `http://localhost:8080`.

### Opção 2, rodando via Docker Compose (tudo containerizado)

```bash
cd infra
docker compose up -d --build
```

Isso sobe o banco de dados e a aplicação juntos, já com a imagem construída a partir do `Dockerfile`. A API fica disponível em `http://localhost:8082`.

## Testes

O projeto tem testes em três camadas diferentes:

**`UrlServiceTest`**: testes unitários do Service, usando mock do Repository (com Mockito). Cobrem a geração de código quando não há alias, a validação de alias personalizado disponível, o erro de alias já em uso, e a busca de URL por código (existente e inexistente).

**`UrlRepositoryTest`**: testes que rodam contra o banco Postgres de verdade. Cobrem a verificação de código existente, o salvamento de uma URL nova com geração automática de id, e a busca por código curto (mais detalhes sobre como esses testes evitam poluir o banco na seção de Decisões técnicas, abaixo).

**`UrlControllerTest`**: teste de integração, sobe a aplicação inteira e testa via HTTP real. Cobre o fluxo completo (encurtar uma URL e depois usar o código gerado pra redirecionar), o erro de alias duplicado (409) e o erro de código inexistente (404).

Pra rodar todos:

```bash
./mvnw test
```

Os testes de integração (Controller e Repository) dependem do PostgreSQL estar rodando.

## Decisões técnicas

**Por que JdbcTemplate.** No meu dia a dia eu trabalho tanto com JdbcTemplate quanto com JPA, não foi por falta de domínio de um ou outro. Escolhi JdbcTemplate aqui porque é a ferramenta que uso com mais frequência no meu ambiente de trabalho atual, e porque ela deixa a query explícita no código: eu escrevo exatamente o SQL que vai rodar, sem uma camada de abstração escondendo isso. Para um domínio simples como este (uma tabela só, sem relacionamento), isso é mais direto do que colocar o peso de um ORM completo em cima.

**Java 8.** Mantive Java 8 conforme a stack informada no desafio. Isso também mudou algumas escolhas de código, por exemplo, não usei `record` (que só existe a partir do Java 16) nem `isBlank()` (Java 11), e troquei por classes com Lombok e `trim().isEmpty()`, respectivamente.

**Sobre o WildFly 10.** O desafio menciona WildFly 10 na seção de premissas, mas essa tecnologia não faz parte da minha stack atual, e optei por não usá-la. Prefiro não entregar algo que eu não teria domínio pra explicar de verdade numa conversa técnica. Usei Spring Boot com servidor embutido, mantendo a compatibilidade com Java 8. É uma divergência real em relação ao que foi pedido, e preferi deixar isso claro aqui a tentar disfarçar.

**Motor de geração sincronizado.** O método `encurtar`, no `UrlService`, é `synchronized`. Isso garante que só uma requisição por vez executa esse trecho de código, atendendo ao requisito do desafio. Na prática, isso evita que duas pessoas gerem o mesmo código ao mesmo tempo antes de qualquer uma delas salvar no banco.

**Por que `SecureRandom` em vez de `Random`.** Usei `SecureRandom` pra gerar os códigos curtos. O `Random` comum do Java segue um padrão matemático: se alguém observar alguns códigos gerados em sequência, existe a possibilidade de calcular quais seriam os próximos, o que abriria brecha pra alguém tentar adivinhar links encurtados de outras pessoas. O `SecureRandom` usa fontes de aleatoriedade reais do sistema operacional, então não segue esse padrão previsível.

**Por que `EXISTS` em vez de `COUNT` na verificação de código existente.** A query original usava `SELECT COUNT(*)` pra checar se um código já existia. Troquei por `SELECT EXISTS(...)`, que faz o banco parar de procurar assim que encontra a primeira ocorrência, em vez de continuar contando quantas vezes aquilo aparece. Como eu só preciso saber se existe ou não, isso evita trabalho desnecessário pro banco.

**`StringBuilder` na geração do código.** Cada vez que você concatena uma `String` normal (`+`), o Java cria um objeto novo por trás dos panos e descarta o anterior. Como a geração do código roda em loop (6 vezes, uma por caractere), usei `StringBuilder`, que edita o mesmo objeto a cada `.append()` em vez de ficar criando e descartando strings a cada volta do loop.

**`@Transactional` nos testes de Repository.** Os testes que acessam o banco de verdade usam `@Transactional`. Isso faz cada teste rodar dentro de uma transação que é desfeita automaticamente no final, ou seja, qualquer dado inserido durante o teste não fica salvo de verdade no banco depois que ele termina.

**Sem frontend.** Não implementei interface visual. Optei por concentrar o tempo disponível no backend, nos testes e na documentação, já que é a parte que a vaga avalia com mais peso e onde eu queria entregar o máximo de profundidade possível dentro do prazo.

**CI/CD com banco real, não H2.** Configurei um pipeline no GitHub Actions (`.github/workflows/tests.yml`) que roda automaticamente a cada push. Pra isso, usei um **container de serviço** rodando Postgres dentro do próprio pipeline, ou seja, toda vez que alguém sobe código novo, o GitHub sobe um banco de dados real e temporário, cria a tabela `urls` do zero, e só depois roda a compilação e os testes. Poderia ter optado por H2 (banco em memória), que é o caminho mais comum em pipeline de CI por ser mais rápido de subir, mas preferi Postgres real porque assim os testes rodam exatamente contra o mesmo tipo de banco que a aplicação usa em qualquer outro ambiente (local ou produção). Um teste que passa no H2 pode se comportar diferente no Postgres de verdade (tipos de dado, sintaxe de SQL, comportamento de constraint), e eu não queria correr esse risco.

Isso também traz um ganho prático no dia a dia: eu não preciso subir a aplicação localmente pra saber se o código que acabei de escrever compila e passa nos testes. É só dar `git push`, e o próprio pipeline compila e roda tudo automaticamente, me avisando se algo quebrou antes de qualquer outra pessoa (ou eu mesmo, mais tarde) perceber manualmente.

**Docker: build multi-stage e separação de portas.** O `Dockerfile` usa duas etapas (multi-stage build): a primeira usa uma imagem com Maven completo pra compilar o projeto, e a segunda usa uma imagem bem mais enxuta, só com o Java necessário pra rodar (`eclipse-temurin:8-jre-alpine`), copiando pra dentro dela apenas o `.jar` já pronto. Isso evita carregar Maven, código-fonte e dependências de build dentro da imagem final, ela fica só com o que realmente precisa pra executar. Usei as imagens Eclipse Temurin porque as imagens `openjdk:8-*` foram descontinuadas no Docker Hub, e Temurin é a distribuição que continua sendo mantida.

Além disso, a aplicação pode rodar tanto local (pela IDE) quanto totalmente dentro do Docker, e as duas formas usam portas diferentes (`8080` local, `8082` no Docker Compose) exatamente pra evitar conflito entre elas caso as duas estejam rodando ao mesmo tempo na minha máquina.

## O que faria diferente com mais tempo

- **Rate limiting** no endpoint de redirecionamento. Hoje, alguém poderia tentar várias combinações de código em sequência tentando achar URLs válidas por tentativa e erro. Um limite de requisições por IP/minuto reduziria esse risco.
- **Banco de dados isolado para testes**, separado do banco de desenvolvimento
- **Migração de schema automatizada** (Flyway ou Liquibase), em vez de criar a tabela manualmente
- Se WildFly 10 fosse de fato obrigatório, migraria o empacotamento para `.war` e ajustaria a stack para JAX-RS + CDI, mantendo a mesma separação de camadas
