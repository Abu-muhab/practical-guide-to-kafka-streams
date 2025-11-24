package dev.abumuhab.frauddetection.account.entities;


import dev.abumuhab.frauddetection.account.TransactionType;
import dev.abumuhab.frauddetection.account.dtos.TransactionDto;
import dev.abumuhab.frauddetection.common.BaseEntity;
import dev.abumuhab.frauddetection.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Transaction extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private double amount;

    private TransactionType type;

    private Country originCountry;

    public TransactionDto toDto(){
        return new TransactionDto(getId(),getType(),getAmount());
    }
}
