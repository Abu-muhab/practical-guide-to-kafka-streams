package dev.abumuhab.frauddetection.account.entities;

import dev.abumuhab.frauddetection.account.dtos.AccountDto;
import dev.abumuhab.frauddetection.common.BaseEntity;
import dev.abumuhab.frauddetection.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
    private double balance;

    @OneToOne
    @JoinColumn(name = "user_id",unique = true,nullable = false)
    private User owner;
    public AccountDto toDto(){
        return new AccountDto(getBalance());
    }
}
