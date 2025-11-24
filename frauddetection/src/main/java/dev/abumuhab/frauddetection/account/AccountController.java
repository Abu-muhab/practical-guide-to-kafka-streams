package dev.abumuhab.frauddetection.account;


import dev.abumuhab.frauddetection.account.dtos.CreateTransactionResponse;
import dev.abumuhab.frauddetection.account.dtos.TransactionRequest;
import dev.abumuhab.frauddetection.account.entities.Account;
import dev.abumuhab.frauddetection.account.entities.Transaction;
import dev.abumuhab.frauddetection.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account/transaction")
    CreateTransactionResponse createTransaction(@AuthenticationPrincipal() UserDetails userDetails, @RequestBody TransactionRequest request){
        Transaction transaction = accountService.createTransaction((User) userDetails,request.type(),request.amount());
        Account account = accountService.getAccount((User) userDetails);
        return new CreateTransactionResponse(transaction.toDto(),account.toDto());
    }
}
