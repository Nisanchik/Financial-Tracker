package ru.mirea.newrav1k.userservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "t_yandex_tokens",
        schema = "tracker_management",
        indexes = {
                @Index(name = "idx_yandex_token_tracker_ids", columnList = "trackerId")
        }
)
public class YandexToken {

    @Id
    @JsonIgnore
    @Column(name = "tracker_id", nullable = false)
    private UUID trackerId;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

}