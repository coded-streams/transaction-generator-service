# Transaction Generator Service

A Spring Boot application for generating realistic financial card transactions and streaming them to Kafka using Avro schemas. This service is part of the fraud detection system for the codedx-transfraud project.

## 🚀 Features

- **Real-time Transaction Generation**: Automatically generates realistic financial transactions
- **Avro Schema Integration**: Uses Avro schemas for type-safe data streaming
- **Kafka Integration**: Streams transactions to Kafka topics for real-time processing
- **H2 Database**: In-memory database for storing customer, card, and transaction data
- **REST API**: Endpoints for data generation and system monitoring
- **Scheduled Generation**: Configurable intervals for automatic transaction generation

## ⚠️ Compatibility Notes

### Java Version
- **Java 11** - This project is built and tested with Java 11 for compatibility with the Avro schema library and downstream Flink processing.

### Spring Boot Version
- **Spring Boot 2.7.18** - Used for compatibility with Java 11 and stable ecosystem dependencies.

### Flink Integration
- The generated Avro events are designed to be consumed by **Apache Flink clusters running Java 11**
- Events are serialized using Avro binary format for efficient processing in Flink
- Schema evolution is supported for backward compatibility with Flink consumers


## 📋 Prerequisites

- **Java 11** (Required for compatibility)
- Maven 3.6+
- Docker and Docker Compose
- Kafka cluster (local or remote)

## 🛠️ Installation & Setup

### 1. Verify Java Version
```bash
java -version
# Should show: java version "11.x.x"
```

### 2. Clone the Repository
```bash
git clone <repository-url>
cd transaction-generator-service
```

### 3. Build the Project
```bash
mvn clean compile
```

### 4. Start Infrastructure
```bash
# Start Kafka cluster
docker-compose -f kafka-compose.yml up -d

# Verify services are running
docker ps
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8085`

## ⚙️ Configuration

### Application Properties
Key configuration in `application.yml`:

```yaml
server:
  port: 8085

spring:
  kafka:
    bootstrap-servers: localhost:9092,localhost:29092
    producer:
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      properties:
        schema.registry.url: http://localhost:8081

app:
  kafka:
    topics:
      transactions: "financial-transactions"
  data:
    generation:
      enabled: true
      initial-customers: 50
      transaction-interval-ms: 10000
```

### Kafka Topics
- `financial-transactions`: Raw transaction data in Avro format (consumed by Flink)
- `fraud-alerts`: Fraud detection alerts (reserved for future use)

## 🔄 Flink Consumption

### Event Flow
```
Transaction Generator (Java 11)
         ↓
Kafka Topic: financial-transactions
         ↓
Flink Cluster (Java 11) - Fraud Detection
         ↓
Kafka Topic: fraud-alerts
```

### Avro Schema Compatibility
- Schemas are compatible with Flink's Avro serialization
- Schema evolution supported for future changes
- Binary format optimized for Flink processing

### Expected Flink Consumer Setup
```java
// Example Flink Kafka consumer
DataStream<CardTransaction> transactions = env
    .addSource(new FlinkKafkaConsumer<>(
        "financial-transactions",
        new AvroDeserializationSchema<>(CardTransaction.class),
        properties
    ));
```

## 📊 API Endpoints

### Data Generation
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/generator/initialize` | Initialize sample customer and card data |
| `GET` | `/api/generator/status` | Get data generation status and statistics |
| `POST` | `/api/generator/reinitialize` | Clear and reinitialize all data |

### Transaction Generation
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/avro-transactions/random` | Generate and send a single random transaction |
| `POST` | `/api/avro-transactions/bulk?count=50` | Generate multiple transactions |
| `GET` | `/api/avro-transactions/health` | Service health check |

### System Monitoring
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/system/status` | System status and database statistics |

## 🎯 Usage Examples

### 1. Initialize Sample Data
```bash
curl -X POST http://localhost:8085/api/generator/initialize
```

### 2. Check System Status
```bash
curl http://localhost:8085/api/system/status
```

### 3. Generate Single Transaction
```bash
curl -X POST http://localhost:8085/api/avro-transactions/random
```

### 4. Generate Bulk Transactions
```bash
curl -X POST "http://localhost:8085/api/avro-transactions/bulk?count=100"
```

### 5. Monitor H2 Database
Access H2 console at: `http://localhost:8085/h2-console`
- JDBC URL: `jdbc:h2:file:./data/transactiondb`
- Username: `sa`
- Password: (leave empty)

## 🔧 Development

### Building from Source
```bash
mvn clean package
```

### Running Tests
```bash
mvn test
```

### Code Generation (Avro Schemas)
```bash
mvn generate-sources
```

## 📈 Generated Data

### Customer Profiles
- Random names, emails, and addresses
- Geographic location data
- Transaction behavior patterns
- Contact information

### Card Information
- Realistic card numbers (Visa/Mastercard)
- Credit limits and available balances
- Expiration dates and CVV codes
- Active/inactive status

### Transaction Types
- **POS**: Point-of-sale transactions
- **ONLINE**: E-commerce transactions
- **ATM**: Cash withdrawals
- **CONTACTLESS**: Tap-to-pay transactions
- **RECURRING**: Subscription payments

### Transaction Features
- Merchant information with categories
- Geographic location data
- Device information for online transactions
- Timestamp and amount data
- Currency information (USD)

## 🐛 Troubleshooting

### Common Issues

1. **Java Version Mismatch**
   ```bash
   # Verify correct Java version
   java -version
   # Should be 11.x.x, not 17.x.x
   ```

2. **Kafka Connection Issues**
   - Verify Kafka is running: `docker ps | grep kafka`
   - Test connection: `kcat -b localhost:9092 -L`
   - Check Schema Registry: `curl http://localhost:8081/subjects`

3. **Database Issues**
   - Check H2 console: `http://localhost:8085/h2-console`
   - Verify data directory permissions

4. **Flink Compatibility**
   - Ensure Flink cluster runs Java 11
   - Verify Avro schema compatibility between services

### Logs
Application logs are available with different levels:
```bash
# View application logs
tail -f logs/application.log

# Debug Kafka issues
logging.level.org.springframework.kafka=DEBUG
```

## 🔮 Future Enhancements

- [ ] Redis integration for caching
- [ ] Metrics and monitoring with Micrometer
- [ ] Custom transaction patterns
- [ ] Fraud pattern simulation
- [ ] Load testing capabilities
- [ ] Docker containerization
- [ ] Kubernetes deployment manifests

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Nestor Martourez** - [nestorabiawuh@gmail.com](mailto:nestorabiawuh@gmail.com)
- **LinkedIn**: [Nestor Abiangang](https://www.linkedin.com/in/nestor-abiangang/)

## 🙏 Acknowledgments

- Apache Kafka community
- Spring Boot framework
- Confluent Schema Registry
- Avro serialization library
- Apache Flink community

---

**Note**: This service is designed for development and testing purposes as part of a larger fraud detection system. Always ensure proper security measures in production environments.

**Compatibility**: Built with Java 11 and Spring Boot 2.7 for seamless integration with Avro schema libraries and Flink processing clusters.
```

## Key Updates Made:

1. **Added "Compatibility Notes" section** at the top highlighting:
   - Java 11 requirement
   - Spring Boot 2.7.18 version
   - Flink integration details

2. **Updated Prerequisites** to emphasize Java 11 requirement

3. **Added Flink Consumption section** explaining:
   - Event flow from generator to Flink
   - Avro schema compatibility
   - Example Flink consumer code

4. **Enhanced Troubleshooting** with Java version verification

5. **Updated footer** with compatibility notice

6. **Added acknowledgments** for Flink community