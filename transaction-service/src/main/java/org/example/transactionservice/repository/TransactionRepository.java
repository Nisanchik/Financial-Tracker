package org.example.transactionservice.repository;

import jakarta.persistence.criteria.Predicate;
import org.example.transactionservice.model.dto.TransactionFilter;
import org.example.transactionservice.model.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

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
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (filter.createdAt() != null) {
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.createdAt()));
//            }
//
//            if (filter.type() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("type"), filter.type()));
//            }
//
//            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
//        };
        return specification;
    }

}