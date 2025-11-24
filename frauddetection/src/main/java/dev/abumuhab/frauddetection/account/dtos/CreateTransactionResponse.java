package dev.abumuhab.frauddetection.account.dtos;

import dev.abumuhab.frauddetection.account.dtos.AccountDto;
import dev.abumuhab.frauddetection.account.dtos.TransactionDto;

public record CreateTransactionResponse (TransactionDto transaction, AccountDto account){
}
