package ru.mirea.newrav1k.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.userservice.model.entity.YandexToken;

import java.util.UUID;

@Repository
public interface YandexTokenRepository extends JpaRepository<YandexToken, UUID> {

}