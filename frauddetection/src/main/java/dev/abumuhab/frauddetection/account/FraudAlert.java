package dev.abumuhab.frauddetection.account;

import dev.abumuhab.frauddetection.account.entities.Country;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@NoArgsConstructor
@Getter
@Setter
@ToString
public class FraudAlert {
    public String userId;
    public FraudAlertReason reason;
    public Set<String> relatedTransactions=new HashSet<>();
    public Set<String> relatedEmails= new HashSet<>();
    public Set<Country> relatedLocations = new HashSet<>();
    public void addRelatedTransaction(String id){
        relatedTransactions.add(id);
    }

    public void addRelatedEmails(String email){
        relatedEmails.add(email);
    }

    public void addRelatedLocation(Country location){
        relatedLocations.add(location);
    }
}
