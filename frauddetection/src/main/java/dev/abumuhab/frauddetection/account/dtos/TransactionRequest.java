package dev.abumuhab.frauddetection.account.dtos;

import dev.abumuhab.frauddetection.account.TransactionType;

public record TransactionRequest(TransactionType type, double amount) {
}
