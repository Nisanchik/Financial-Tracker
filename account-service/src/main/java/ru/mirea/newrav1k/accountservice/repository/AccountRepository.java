package ru.mirea.newrav1k.accountservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.mirea.newrav1k.accountservice.model.dto.AccountFilter;
import ru.mirea.newrav1k.accountservice.model.entity.Account;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :accountId and a.trackerId = :trackerId")
    Optional<Account> findAccountByTrackerIdAndIdForPessimisticLock(UUID trackerId, UUID accountId);

    Optional<Account> findAccountByTrackerIdAndId(UUID trackerId, UUID accountId);

    boolean existsByTrackerIdAndName(UUID trackerId, String name);

    default Specification<Account> buildAccountSpecification(AccountFilter filter) {
        Specification<Account> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (Objects.nonNull(filter.trackerId())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("trackerId"), filter.trackerId()));
        }

        if (StringUtils.hasText(filter.name())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("name"), "%" + filter.name() + "%"));
        }

        if (Objects.nonNull(filter.currency())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("currency"), filter.currency()));
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