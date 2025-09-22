package ru.mirea.newrav1k.userservice.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.newrav1k.userservice.model.enums.Authority;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "t_trackers",
        schema = "tracker_management",
        indexes = {
                @Index(name = "idx_tracker_ids", columnList = "id"),
                @Index(name = "idx_tracker_usernames", columnList = "username")
        }
)
public class Tracker extends BaseEntity {

    @Email
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private List<Authority> authorities = new ArrayList<>(List.of(Authority.ROLE_USER));

}