package dev.abumuhab.frauddetection.common;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final EntityManager entityManager;
    private final KafkaTemplate<String,String> kafkaTemplate;

    @Transactional
    void publishOutboxEvent(OutboxEvent outboxEvent){
        outboxEvent.setPublished(true);
        entityManager.merge(outboxEvent);
        sendToKafka(outboxEvent);
    }

    @Transactional("kafkaTransactionManager")
    void sendToKafka(OutboxEvent outboxEvent){
        this.kafkaTemplate.send(outboxEvent.getTopic(),outboxEvent.getKey(),outboxEvent.getPayload());
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional("transactionManager")
    void scanForUnpublishedOutboxEvents(){
        TypedQuery<OutboxEvent> query = this.entityManager.createQuery("SELECT o from OutboxEvent o WHERE o.published = :published ORDER BY createdAt DESC LIMIT 500",OutboxEvent.class)
                .setParameter("published",false);
        List<OutboxEvent> events = query.getResultList();
        for (OutboxEvent event : events) {
            publishOutboxEvent(event);
        }
    }
}
