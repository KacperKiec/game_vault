package io.kk.gameservice.integration;

import io.kk.gameservice.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InternalUserRepo {
    private final JdbcTemplate jdbcTemplate;

    public boolean checkIfUserExists(Long id) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)", Boolean.class, id));
    }

    public List<UserDTO> getUsernames(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));

        return jdbcTemplate.query(
                "SELECT id, username FROM users WHERE id IN (" + inSql + ")",
                (rs, rowNum) -> new UserDTO(rs.getLong("id"), rs.getString("username")),
                ids.toArray()
        );
    }
}
