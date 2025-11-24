package dev.abumuhab.frauddetection.account;

import dev.abumuhab.frauddetection.account.entities.Transaction;
import dev.abumuhab.frauddetection.account.events.TransactionCreatedEvent;
import dev.abumuhab.frauddetection.account.repositories.TransactionRepository;
import dev.abumuhab.frauddetection.common.DomainEvent;
import dev.abumuhab.frauddetection.users.events.UserCreatedEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Configuration
public class FraudDetectionStream {
    final double TRANSACTION_HIGH_VALUE_THRESHOLD = 200000;
    final int TRANSACTION_VELOCITY_THRESHOLD = 5;
    final int ACCOUNT_CREATION_VELOCITY_THRESHOLD = 3;
    @Autowired
    TransactionRepository transactionRepository;

    @Bean
    public KStream<String,String> fraudDetectionStreams(StreamsBuilder streamsBuilder){
        //Serde definitions for the various entities our streams interact with
        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
        JsonSerde<FraudAlert>  fraudAlertJsonSerde = new JsonSerde<>(FraudAlert.class);
        JsonSerde<UserCreatedEvent> userCreatedEventJsonSerde = new JsonSerde<>(UserCreatedEvent.class);

        // Base streams declarations
        KStream<String, String> transactionEventsStream = streamsBuilder.stream("transaction-events");
        KStream<String,String> userEvents = streamsBuilder.stream("user-events");

        // filter out transaction events to focus on created debit alert actions
        KStream<String, Transaction> transactionsStream = transactionEventsStream
                .filter((key,value)-> Objects.equals(DomainEvent.extractEventNameFromJsonString(value), TransactionCreatedEvent.class.getSimpleName()))
                .map((key, value)->{
                    TransactionCreatedEvent transactionCreatedEvent = DomainEvent.fromJsonString(value,TransactionCreatedEvent.class);
                    if(transactionCreatedEvent == null || transactionCreatedEvent.getTransactionId()==null){
                        return KeyValue.pair(key,null);
                    }
                    Optional<Transaction> transaction = this.transactionRepository.findById(transactionCreatedEvent.getTransactionId());
                    return transaction.map(transaction1 -> KeyValue.pair(key, transaction1)).orElseGet(() -> KeyValue.pair(key, null));
                }).filter((key,value)->value!=null);
        KStream<String, Transaction> debitTransactionsStream = transactionsStream
                .filter((key,value)-> value.getType() == TransactionType.DEBIT)
                .selectKey((key,value)-> value.getUser().getId());

        //high threshold debit transaction fraud alert stream
        KStream<String,FraudAlert> highThresholdFraudAlertStream = debitTransactionsStream
                .filter((key,value)-> value.getAmount() > TRANSACTION_HIGH_VALUE_THRESHOLD)
                .map((key, value)->{
                   final FraudAlert alert = new FraudAlert();
                   alert.userId = value.getUser().getId();
                   alert.reason = FraudAlertReason.HIGH_TRANSACTION_VALUE;
                   alert.addRelatedTransaction(value.getId());
                   return KeyValue.pair(key,alert);
                });

        //impossible geographic velocity alert stream
        KStream<String,FraudAlert> impossibleGeographicVelocityFraudAlertStream =debitTransactionsStream
                .groupByKey(Grouped.with(Serdes.String(),transactionSerde))
                .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(1)))
                .aggregate(
                        FraudAlert::new,
                        (key,value,aggregate)->{
                            aggregate.userId = value.getUser().getId();
                            aggregate.reason = FraudAlertReason.IMPOSSIBLE_GEOGRAPHIC_VELOCITY;
                            aggregate.addRelatedLocation(value.getOriginCountry());
                            return  aggregate;
                        },
                        Materialized.with(Serdes.String(),fraudAlertJsonSerde)
                )
                .toStream()
                .map((key,value)-> KeyValue.pair(key.key(),value))
                .filter((key,value)->value.getRelatedLocations().size()> 1);

        // high velocity debit transactions fraud alert stream
        KStream<String,FraudAlert> highVelocityFraudAlertStream = debitTransactionsStream
                .groupByKey(Grouped.with(Serdes.String(),transactionSerde))
                .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(1)))
                .aggregate(
                        FraudAlert::new,
                        (key,value,aggregate)->{
                            aggregate.userId = value.getUser().getId();
                            aggregate.reason = FraudAlertReason.HIGH_TRANSACTION_VELOCITY;
                            aggregate.addRelatedTransaction(value.getId());
                            return  aggregate;
                        },
                        Materialized.with(Serdes.String(),fraudAlertJsonSerde)
                )
                .toStream()
                .map((key,value)-> KeyValue.pair(key.key(),value))
                .filter((key,value)->value.getRelatedTransactions().size()>= TRANSACTION_VELOCITY_THRESHOLD);

        // Suspicious account creation fraud alert stream
        KStream<String,FraudAlert> suspiciousSignupsFraudAlertStream = userEvents
                .filter((key,value)->Objects.equals(DomainEvent.extractEventNameFromJsonString(value), UserCreatedEvent.class.getSimpleName()))
                .map((key,value)->KeyValue.pair(key,DomainEvent.fromJsonString(value, UserCreatedEvent.class)))
                .groupBy((key,value)->value.getIpAddress(),Grouped.with(Serdes.String(),userCreatedEventJsonSerde))
                .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofDays(1)))
                .aggregate(
                        FraudAlert::new,
                        (key,value,aggregate)->{
                            aggregate.reason = FraudAlertReason.SUSPICIOUS_SIGNUP;
                            aggregate.addRelatedEmails(value.getEmail());
                            return  aggregate;
                        },
                        Materialized.with(Serdes.String(),fraudAlertJsonSerde)
                )
                .toStream()
                .map((key,value)-> KeyValue.pair(key.key(),value))
                .filter((key,value)->value.getRelatedEmails().size()>= ACCOUNT_CREATION_VELOCITY_THRESHOLD);


        //combined fraud alert stream to fraud-alerts topic
        KStream<String,FraudAlert> fraudAlerts = highThresholdFraudAlertStream
                .merge(highVelocityFraudAlertStream)
                .merge(suspiciousSignupsFraudAlertStream)
                .merge(impossibleGeographicVelocityFraudAlertStream);

        fraudAlerts
                .peek((key,value)->System.out.println("Peek fraud alert: "+value))
                .to("fraud-alerts", Produced.with(Serdes.String(),fraudAlertJsonSerde));

        return transactionEventsStream;
    }
}
