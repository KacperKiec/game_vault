package io.kk.gameservice.repository.impl;

import io.kk.gameservice.model.Review;
import io.kk.gameservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReviewRepository implements ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> Review.builder()
            .id(rs.getLong("id"))
            .guid(rs.getLong("game_id"))
            .userId(rs.getLong("user_id"))
            .content(rs.getString("content"))
            .rating(rs.getInt("rating"))
            .date(rs.getDate("date").toLocalDate())
            .build();

    @Override
    public Review save(Review review) {
        if (review.getId() == null) {
            String sql = "INSERT INTO reviews (game_id, user_id, content, rating, date) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, review.getGuid());
                ps.setLong(2, review.getUserId());
                ps.setString(3, review.getContent());
                ps.setInt(4, review.getRating());
                ps.setDate(5, java.sql.Date.valueOf(review.getDate()));
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
                Number key = (Number) keyHolder.getKeys().get("id");
                review.setId(key.longValue());
            }
            return review;
        } else {
            String sql = "UPDATE reviews SET content = ?, rating = ?, date = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    review.getContent(),
                    review.getRating(),
                    java.sql.Date.valueOf(review.getDate()),
                    review.getId()
            );
            return review;
        }
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        return queryForOptional(sql, reviewId);
    }

    @Override
    public Optional<Review> findByGuidAndUserId(Long guid, Long userId) {
        String sql = "SELECT * FROM reviews WHERE game_id = ? AND user_id = ?";
        return queryForOptional(sql, guid, userId);
    }

    @Override
    public List<Review> findByGuid(Long gameId) {
        String sql = "SELECT * FROM reviews WHERE game_id = ?";
        return jdbcTemplate.query(sql, reviewRowMapper, gameId);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private Optional<Review> queryForOptional(String sql, Object... args) {
        try {
            Review review = jdbcTemplate.queryForObject(sql, reviewRowMapper, args);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
