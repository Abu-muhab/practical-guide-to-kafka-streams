package dev.abumuhab.frauddetection.account.repositories;

import dev.abumuhab.frauddetection.account.entities.Account;
import dev.abumuhab.frauddetection.account.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,String> {
    List<Transaction> findByAccount(Account account);
}
