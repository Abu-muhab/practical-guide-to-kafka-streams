package dev.abumuhab.frauddetection.account.repositories;

import dev.abumuhab.frauddetection.account.entities.Account;
import dev.abumuhab.frauddetection.users.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface AccountRepository extends JpaRepository<Account,String> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    Account findWithLockByOwner(User owner);

    Account findByOwner(User owner);
}
