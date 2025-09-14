package ru.mirea.newrav1k.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.mirea.newrav1k.userservice.model.entity.Customer;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("select c from Customer c where c.username = :username")
    Optional<Customer> findByUsername(String username);

    boolean existsByUsername(String username);

}