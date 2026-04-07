package io.kk.userservice.repository.impl;

import io.kk.userservice.model.Role;
import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .password(rs.getString("password"))
            .email(rs.getString("email"))
            .role(Role.valueOf(rs.getString("role")))
            .build();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getRole().name());
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
                Number key = (Number) keyHolder.getKeys().get("id");
                user.setId(key.longValue());
            }
            return user;
        } else {
            String sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getId()
            );
            return user;
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return queryForOptional(sql, id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return queryForOptional(sql, username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return queryForOptional(sql, email);
    }

    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        return jdbcTemplate.query(sql, userRowMapper, role.name());
    }

    @Override
    public List<User> findAllByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        // JDBC nie obsługuje natywnie listy w "IN (?)", musimy wygenerować odpowiednią liczbę znaków zapytania
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = String.format("SELECT * FROM users WHERE id IN (%s)", inSql);

        return jdbcTemplate.query(sql, userRowMapper, ids.toArray());
    }

    @Override
    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, user.getId());
    }

    private Optional<User> queryForOptional(String sql, Object... args) {
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, args);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
