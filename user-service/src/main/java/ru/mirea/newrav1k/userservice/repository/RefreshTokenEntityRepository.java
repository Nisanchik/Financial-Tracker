package ru.mirea.newrav1k.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.userservice.model.entity.RefreshTokenEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenEntityRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    void deleteByToken(String token);

    void deleteAllByCustomerId(UUID customerId);

    Optional<RefreshTokenEntity> findByToken(String token);

}