package ru.mirea.nisanchik.categoryservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryFilter;
import ru.mirea.nisanchik.categoryservice.model.entity.Category;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    Page<Category> findAllByTrackerId(UUID trackerId, Pageable pageable);

    Optional<Category> findAllByTrackerIdAndId(UUID trackerId, UUID id);

    Optional<Category> findCategoryByTrackerIdAndId(UUID trackerId, UUID categoryId);

    default Specification<Category> buildSpecificationByFilter(CategoryFilter filter) {
        Specification<Category> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (Objects.nonNull(filter.type())) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), filter.type()));
        }
        if (Objects.nonNull(filter.isSystem())) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isSystem"), filter.isSystem()));
        }
        return specification;
    }
}
