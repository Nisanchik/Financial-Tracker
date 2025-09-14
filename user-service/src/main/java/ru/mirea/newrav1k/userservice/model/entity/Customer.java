package ru.mirea.newrav1k.userservice.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "t_customers",
        schema = "customer_management",
        indexes = {
                @Index(name = "idx_customer_ids", columnList = "id"),
                @Index(name = "idx_customer_usernames", columnList = "username")
        }
)
public class Customer extends BaseEntity {

    @Email
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name", nullable = false)
    private String lastname;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Builder.Default
    private List<Authority> authorities = new ArrayList<>(List.of(Authority.ROLE_USER));

}