package ru.mirea.newrav1k.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.userservice.model.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    void deleteByToken(String token);

    void deleteAllByTrackerId(UUID trackerId);

    Optional<RefreshToken> findByToken(String token);

}