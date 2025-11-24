package dev.abumuhab.frauddetection.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.abumuhab.frauddetection.account.entities.Account;
import dev.abumuhab.frauddetection.account.entities.Country;
import dev.abumuhab.frauddetection.account.entities.Transaction;
import dev.abumuhab.frauddetection.account.events.TransactionCreatedEvent;
import dev.abumuhab.frauddetection.account.repositories.AccountRepository;
import dev.abumuhab.frauddetection.account.repositories.TransactionRepository;
import dev.abumuhab.frauddetection.common.DomainEvent;
import dev.abumuhab.frauddetection.users.User;
import dev.abumuhab.frauddetection.users.events.UserCreatedEvent;
import dev.abumuhab.frauddetection.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;


    @KafkaListener(id = "account_service_user_events",topics = "user-events")
    public  void accountServiceUserEventsListener(String in) throws JsonProcessingException {
        String eventName = DomainEvent.extractEventNameFromJsonString(in);
        if(eventName == null){
            return;
        }

        switch (eventName){
            case "UserCreatedEvent":
                UserCreatedEvent userCreatedEvent = objectMapper.readValue(in, UserCreatedEvent.class);
                User user = userRepository.findByEmail(userCreatedEvent.getEmail());
                if(user==null){
                    return;
                }
                createAccount(user);
                break;
        }
    }

    Account createAccount(User user){
        Account existing = accountRepository.findByOwner(user);
        if(existing!=null){
            throw new RuntimeException("User bank account already exists");
        }

        Account  account = new Account(0,user);
        return accountRepository.save(account);
    }


    Account getAccount(User user){
        return getAccount(user,false);
    }

    @Transactional("transactionManager")
    Account getAccount(User user,boolean withLock) {
        Account account = withLock ? accountRepository.findWithLockByOwner(user): accountRepository.findByOwner(user);

        if (account == null) {
            account = createAccount(user);
        }

        if (account == null) {
            throw new RuntimeException("Could not get or provision account for user " + user.getId());
        }

        return account;
    }

    @Transactional("transactionManager")
    Transaction createTransaction(User user, TransactionType type, double amount){
        Account  account = getAccount(user,true);
        if (type == TransactionType.DEBIT && account.getBalance() < amount) {
            throw new RuntimeException("Account " + account.getId() + " has insufficient balance");
        }

        if (type == TransactionType.CREDIT) {
            account.setBalance(account.getBalance() + amount);
        } else {
            account.setBalance(account.getBalance() - amount);
        }


        Transaction transaction = new Transaction(user,account,amount,type, Country.random());
        transaction = transactionRepository.save(transaction);

        transaction.addEvent(new TransactionCreatedEvent(transaction.getId(),account.getId(),user.getId()));
        accountRepository.save(account);

        return transactionRepository.save(transaction);
    }
}
