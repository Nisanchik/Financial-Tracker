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

    Page<Account> findAllByTrackerId(UUID trackerId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :accountId and a.trackerId = :trackerId")
    Optional<Account> findAccountByTrackerIdAndIdForPessimisticLock(UUID trackerId, UUID accountId);

    Optional<Account> findAccountByTrackerIdAndId(UUID trackerId, UUID accountId);

    void deleteAccountByTrackerIdAndId(UUID trackerId, UUID accountId);

}