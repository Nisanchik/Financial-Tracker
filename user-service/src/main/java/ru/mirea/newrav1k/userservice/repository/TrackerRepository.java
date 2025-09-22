package ru.mirea.newrav1k.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.userservice.model.entity.Tracker;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackerRepository extends JpaRepository<Tracker, UUID> {

    @Query("select t from Tracker t where t.username = :username")
    Optional<Tracker> findByUsername(String username);

    boolean existsByUsername(String username);

}