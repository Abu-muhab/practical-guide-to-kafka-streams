package dev.abumuhab.frauddetection.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public class OutboxJpaRepository<T extends BaseEntity,ID> extends SimpleJpaRepository<T,ID> {
    private final EntityManager entityManager;
    private final JpaEntityInformation<T,ID> entityInformation;

    OutboxJpaRepository(JpaEntityInformation<T,ID> entityInformation,EntityManager entityManager){
        super(entityInformation,entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }


    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        saveEventsToOutbox(entity.getEvents());
        return super.save(entity);
    }

    @Override
    @Transactional
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        for (S entity : entities) {
            saveEventsToOutbox(entity.getEvents());
        }
        return super.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        saveEventsToOutbox(entity.getEvents());
        super.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {
        for (T entity : entities) {
            saveEventsToOutbox(entity.getEvents());
        }
        super.deleteAll(entities);
    }

    void saveEventsToOutbox(List<DomainEvent> events){
        try{
            for (DomainEvent event : events) {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode jsonNode = objectMapper.valueToTree(event);
                jsonNode.put("event",event.getClass().getSimpleName());

                String topicName = this.entityInformation.getEntityName().toLowerCase()+"-events";
                OutboxEvent outboxEvent = OutboxEvent.builder()
                        .id(event.getId())
                        .createdAt(event.getCreatedAt())
                        .topic(topicName)
                        .payload(objectMapper.writeValueAsString(jsonNode))
                        .build();
                entityManager.persist(outboxEvent);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
