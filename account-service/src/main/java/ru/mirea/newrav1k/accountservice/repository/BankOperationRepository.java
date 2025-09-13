package ru.mirea.newrav1k.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.accountservice.model.entity.BankOperation;

import java.util.UUID;

@Repository
public interface BankOperationRepository extends JpaRepository<BankOperation, UUID> {

    boolean existsByTransactionId(UUID transactionId);

}