package ru.mirea.newrav1k.accountservice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.accountservice.model.entity.Account;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Page<Account> findAllByUserId(UUID userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :accountId and a.userId = :userId")
    Optional<Account> findAccountByUserIdAndIdForPessimisticLock(UUID userId, UUID accountId);

    Optional<Account> findAccountByUserIdAndId(UUID userId, UUID accountId);

    void deleteAccountByUserIdAndId(UUID userId, UUID accountId);

}