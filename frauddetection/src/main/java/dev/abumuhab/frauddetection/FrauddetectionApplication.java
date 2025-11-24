package dev.abumuhab.frauddetection;

import dev.abumuhab.frauddetection.common.OutboxJpaRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableKafka
@EnableKafkaStreams
@EnableJpaRepositories(repositoryBaseClass = OutboxJpaRepository.class)
public class FrauddetectionApplication {
	public static void main(String[] args) {
		SpringApplication.run(FrauddetectionApplication.class, args);
	}
}
