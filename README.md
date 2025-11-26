# A Practical Guide to Kafka Streams: Real-Time Fraud Detection

This repository contains the source code accompanying the article on building a real-time fraud detection system using Spring Boot and the Kafka Streams API.

It serves as a practical, concrete implementation demonstrating how to use stateful and stateless stream transformations (filtering, mapping, windowing, and aggregation) to solve real-world problems within robust software systems.

### 📘 Accompanying Article

**https://medium.com/@abumuhab/a-practical-guide-to-kafka-streams-real-time-fraud-detection-with-spring-boot-a398021c35f9**

Please refer to the article above for a deep dive into the concepts, motivations, and detailed explanations of the code structure.

---

## Project Overview

This project simulates a banking environment with users and accounts. The core focus is a **Fraud Detection Service** built with Kafka Streams that monitors events in real-time to identify suspicious patterns.

The application consumes data from input topics, processes the streams using DSL (Domain Specific Language), and produces alerts to an output topic when fraud criteria are met.

### Data Flow Architecture

1.  **Input Streams:** The application listens to two primary topics:
    - `user-events`: Contains events like user creation (signups).
    - `transaction-events`: Contains all transactions (debits/credits) happening on accounts.
2.  **Processing (Kafka Streams):** The stream topology performs stateful joins, sliding window aggregations, and filtering based on specific business rules.
3.  **Output Stream:** Detected anomalies are dispatched to the `fraud-alerts` topic for downstream action (e.g., blocking an account or notifying an analyst).

## Fraud Detection Scenarios

The application is designed to detect the following four specific fraud scenarios:

| Scenario                           | Type                | Description                                                                                                        | Thresholds (Configurable)           |
| :--------------------------------- | :------------------ | :----------------------------------------------------------------------------------------------------------------- | :---------------------------------- |
| **High Value Transaction**         | Stateless           | Detects any single debit transaction that exceeds a specific amount.                                               | > $200,000                          |
| **High Velocity Transactions**     | Stateful (Windowed) | Detects a burst of debit transactions on a single account within a short time frame.                               | ≥ 5 transactions in 1 minute        |
| **Impossible Geographic Velocity** | Stateful (Windowed) | Detects if a user performs transactions from different geographic locations too quickly to be physically possible. | > 1 location in 30 minutes          |
| **Suspicious Account Creation**    | Stateful (Windowed) | Detects when a single IP address is used to create multiple user accounts within a short period.                   | ≥ 3 signups from one IP in 24 hours |

## Project Structure

While a complete system might include Authentication, User, and Account microservices, this repository focuses on the Kafka Streams implementation.

Key classes of interest:

- **`FraudDetectionStream.java`**: The core configuration class where the Kafka Streams topology is defined, including all filters, maps, windows, and aggregations.
- **`FraudAlert.java`**: The data model representing an alert sent to the output topic.
- **`FraudAlertReason.java`**: Enum defining the types of fraud detected.

## Getting Started

### Prerequisites

To run this application locally, you will need:

- Java 21 or higher
- Maven
- Docker and Docker Compose
- PostgreSQL (provided via Docker Compose)

### 1. Start the Infrastructure

The project includes a Docker Compose configuration that sets up all required services:
- Apache Kafka (KRaft mode - no Zookeeper required)
- PostgreSQL database
- PgAdmin (Database management UI)
- Kafbat UI (Kafka visualization tool)

Navigate to the `frauddetection` directory and start the services:

```bash
cd frauddetection
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Kafka on ports 9092 (external) and 9096 (internal)
- PgAdmin on port 8888 (admin@admin.com / admin)
- Kafbat UI on port 8090

Wait for all services to be healthy:
```bash
docker-compose ps
```

### 2. Build and Run the Application

From the `frauddetection` directory, build and run the Spring Boot application:

```bash
# Build the application
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on port 8080 and automatically:
- Connect to PostgreSQL and create necessary tables
- Connect to Kafka and create required topics
- Start the fraud detection stream processing

### 3. Testing the Fraud Detection

The application includes REST APIs for creating users, accounts, and transactions. You can test the fraud detection scenarios using these endpoints:

#### Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

#### Create an Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"accountType":"CHECKING","balance":10000}'
```

#### Trigger Fraud Scenarios

1. **High Value Transaction** (> $200,000):
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":250000,"type":"DEBIT","location":"New York"}'
```

2. **High Velocity** (≥ 5 transactions in 1 minute):
```bash
# Run this command multiple times quickly
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/transactions \
    -H "Content-Type: application/json" \
    -d '{"accountId":1,"amount":100,"type":"DEBIT","location":"New York"}'
done
```

### 4. Monitor Fraud Alerts

View detected fraud alerts using Kafbat UI:
1. Open http://localhost:8090
2. Navigate to Topics → fraud-alerts
3. View the messages to see fraud detection results

Or use Kafka console consumer:
```bash
docker exec -it frauddetection-kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic fraud-alerts \
  --from-beginning
```

### 5. Shutdown

To stop all services:
```bash
docker-compose down
```

To remove all data (including database):
```bash
docker-compose down -v
```
