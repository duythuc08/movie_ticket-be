package com.example.movie_ticket_be.cf.entity;

import com.example.movie_ticket_be.cf.enums.ActionType;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivityLog extends BaseEntity {

    @EmbeddedId
    UserActivityLogId userActivityLogId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    @ManyToOne
    @MapsId("userId")
	@JoinColumn(name = "user_id",nullable = false)
    Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId")
    @JoinColumn(name = "movie_id",nullable = false)
    Movies movie;

    LocalDateTime bestValueAt;
    Integer occurrenceCount;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserActivityLogId implements Serializable {

        @Column(name = "user_id")
        private String userId;

        @Column(name = "movie_id")
        private Long movieId;

        @Enumerated(EnumType.STRING)
        ActionType actionType;
    }
}
