package io.kk.notificationservice.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kk.notificationservice.model.Notification;
import io.kk.notificationservice.model.NotificationType;
import io.kk.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcNotificationRepository implements NotificationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private Notification mapRowToNotification(ResultSet rs, int rowNum) throws SQLException {
        try {
            String jsonMetadata = rs.getString("metadata");
            Map<String, Object> metadata = objectMapper.readValue(jsonMetadata,
                    new TypeReference<>() {
                    });

            return Notification.builder()
                    .id(rs.getLong("id"))
                    .recipientId(rs.getLong("recipient_id"))
                    .type(NotificationType.valueOf(rs.getString("type")))
                    .title(rs.getString("title"))
                    .content(rs.getString("content"))
                    .metadata(metadata)
                    .read(rs.getBoolean("read"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON metadata", e);
        }
    }

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            String sql = "INSERT INTO notifications (recipient_id, type, title, content, metadata, created_at, read, ws_sent) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, notification.getRecipientId());
                ps.setString(2, notification.getType().name());
                ps.setString(3, notification.getTitle());
                ps.setString(4, notification.getContent());
                ps.setObject(5, createPgJsonObject(notification.getMetadata()));
                ps.setTimestamp(6, Timestamp.valueOf(notification.getCreatedAt()));
                ps.setBoolean(7, notification.getRead());
                ps.setBoolean(8, false);
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
                Number key = (Number) keyHolder.getKeys().get("id");
                notification.setId(key.longValue());
            }
            return notification;
        } else {
            String sql = "UPDATE notifications SET read = ? WHERE id = ?";
            jdbcTemplate.update(sql, notification.getRead(), notification.getId());
            return notification;
        }
    }

    @Override
    public Optional<Notification> findById(Long id) {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToNotification, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Notification> findByRecipientIdAndRead(Integer recipientId, Boolean read) {
        String sql = "SELECT * FROM notifications WHERE recipient_id = ? AND read = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, this::mapRowToNotification, recipientId, read);
    }

    @Override
    public List<Notification> findUnsentToWebSocket() {
        String sql = "SELECT * FROM notifications WHERE ws_sent = false";
        return jdbcTemplate.query(sql, this::mapRowToNotification);
    }

    @Override
    public void markAsSentToWebSocket(Long id) {
        String sql = "UPDATE notifications SET ws_sent = true WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private PGobject createPgJsonObject(Map<String, Object> metadata) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(objectMapper.writeValueAsString(metadata));
            return jsonObject;
        } catch (JsonProcessingException e) {
            throw new SQLException("Error serializing metadata to JSON", e);
        }
    }
}
