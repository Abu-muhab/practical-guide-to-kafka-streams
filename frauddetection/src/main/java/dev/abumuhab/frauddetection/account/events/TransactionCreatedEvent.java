package dev.abumuhab.frauddetection.account.events;

import dev.abumuhab.frauddetection.common.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionCreatedEvent extends DomainEvent {
    private String transactionId;
    private String accountId;
    private String userId;
}
