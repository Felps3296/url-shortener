package com.felipereis.urlshortener.repository;

import com.felipereis.urlshortener.model.Url;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Url> URL_ROW_MAPPER = (rs, rowNum) -> new Url(
            rs.getLong("id"),
            rs.getString("url_original"),
            rs.getString("codigo_curto"),
            rs.getTimestamp("data_criacao").toLocalDateTime()
    );

    public boolean existeCodigoCurto(String codigoCurto) {
        String sql = "SELECT COUNT(*) FROM urls WHERE codigo_curto = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, codigoCurto);
        return count != null && count > 0;
    }

    public Url salvar(Url url) {
        String sql = "INSERT INTO urls (url_original, codigo_curto, data_criacao) VALUES (?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, url.getUrlOriginal());
            ps.setString(2, url.getCodigoCurto());
            return ps;
        }, keyHolder);

        url.setId(keyHolder.getKey().longValue());
        return url;
    }

    public Optional<Url> buscarPorCodigoCurto(String codigoCurto) {
        String sql = "SELECT * FROM urls WHERE codigo_curto = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, URL_ROW_MAPPER, codigoCurto));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}