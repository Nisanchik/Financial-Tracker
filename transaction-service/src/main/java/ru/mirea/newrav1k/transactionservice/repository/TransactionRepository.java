package ru.mirea.newrav1k.transactionservice.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionFilter;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findTransactionByTrackerIdAndId(UUID trackerId, UUID transactionId);

    default Specification<Transaction> buildTransactionSpecification(TransactionFilter filter) {
        Specification<Transaction> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (Objects.nonNull(filter.trackerId())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("trackerId"), filter.trackerId()));
        }

        if (StringUtils.hasText(filter.type())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("type"), "%" + filter.type() + "%"));
        }

        if (Objects.nonNull(filter.createdAtFrom()) && Objects.nonNull(filter.createdAtTo())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get("createdAt"), filter.createdAtFrom(), filter.createdAtTo()));
        } else if (Objects.nonNull(filter.createdAtFrom())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("createdAt"), filter.createdAtFrom()));
        } else if (Objects.nonNull(filter.createdAtTo())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("createdAt"), filter.createdAtTo()));
        }

        return specification;
    }

}