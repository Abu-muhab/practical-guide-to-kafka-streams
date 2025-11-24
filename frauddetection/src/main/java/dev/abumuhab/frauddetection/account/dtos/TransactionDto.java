package dev.abumuhab.frauddetection.account.dtos;

import dev.abumuhab.frauddetection.account.TransactionType;

public record TransactionDto(String id, TransactionType type, double amount) {

}
