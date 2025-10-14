package ru.mirea.nisanchik.categoryservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mirea.nisanchik.categoryservice.model.entity.Category;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findAllByTrackerId(UUID trackerId, Pageable pageable);

    Optional<Category> findAllByTrackerIdAndId(UUID trackerId, UUID id);

    Optional<Category> findCategoryByTrackerIdAndId(UUID trackerId, UUID categoryId);

}
