package ru.mirea.newrav1k.transactionservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionFilter;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findAllByTrackerId(UUID trackerId, Specification<Transaction> specification, Pageable pageable);

    Optional<Transaction> findTransactionByTrackerIdAndId(UUID trackerId, UUID transactionId);

    default Specification<Transaction> buildTransactionSpecification(TransactionFilter filter) {
        Specification<Transaction> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (filter.type() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("type"), "%" + filter.type() + "%"));
        }

        if (filter.createdAt() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.createdAt()));
        }

        return specification;
    }

}