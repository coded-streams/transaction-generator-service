package com.codedstream.transfraud.service;

import com.codedstream.transfraud.model.entity.Card;
import com.codedstream.transfraud.model.entity.Customer;
import com.codedstream.transfraud.repository.CardRepository;
import com.codedstream.transfraud.repository.CustomerRepository;
import com.codedstream.transfraud.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataGeneratorService {

    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;
//    private final RedisTemplate<String, Object> redisTemplate;
    private final AvroTransactionGeneratorService avroTransactionGeneratorService;

    private final Random random = new Random();
    private final String[] FIRST_NAMES = {"John", "Jane", "Michael", "Sarah", "David", "Lisa", "Robert", "Maria", "William", "Elizabeth", "James", "Jennifer", "Thomas", "Linda", "Christopher", "Susan", "Daniel", "Jessica", "Matthew", "Karen"};
    private final String[] LAST_NAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};
    private final String[] MERCHANT_CATEGORIES = {"Retail", "Restaurant", "Gas Station", "Online Shopping", "Entertainment", "Travel"};
    private final String[] CITIES = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"};

    @Value("${app.data.generation.enabled:true}")
    private boolean generationEnabled;

    @Value("${app.data.generation.initial-customers:100}")
    private int initialCustomers;

    @Value("${app.data.generation.initial-cards-per-customer:2}")
    private int cardsPerCustomer;

    private boolean dataInitialized = false;

    @PostConstruct
    public void autoInitialize() {
        if (generationEnabled) {
            log.info("Auto-initializing sample data on application startup...");
            initializeSampleData();
        }
    }

    public void initializeSampleData() {
        if (!generationEnabled) {
            log.info("Data generation is disabled");
            return;
        }

        // Check if data already exists
        long existingCustomers = customerRepository.count();
        long existingCards = cardRepository.count();

        if (existingCustomers > 0) {
            log.info("Data already exists. Skipping initialization. Existing customers: {}, cards: {}",
                    existingCustomers, existingCards);
            dataInitialized = true;
            return;
        }

        log.info("Initializing sample data with {} customers and {} cards per customer",
                initialCustomers, cardsPerCustomer);

        int totalCardsCreated = 0;
        Set<String> usedEmails = new HashSet<>();

        for (int i = 0; i < initialCustomers; i++) {
            Customer customer = createSampleCustomer(usedEmails);
            if (customer == null) {
                log.warn("Failed to create unique customer, skipping...");
                continue;
            }

            try {
                customerRepository.save(customer);

                for (int j = 0; j < cardsPerCustomer; j++) {
                    Card card = createSampleCard(customer);
                    cardRepository.save(card);
                    totalCardsCreated++;
                }

                // Log progress every 20 customers
                if ((i + 1) % 20 == 0) {
                    log.info("Created {} customers and {} cards so far...", i + 1, totalCardsCreated);
                }
            } catch (Exception e) {
                log.error("Error saving customer {}: {}", i + 1, e.getMessage());
                // Continue with next customer instead of failing completely
            }
        }

        long totalCustomers = customerRepository.count();
        long totalCards = cardRepository.count();

        log.info("Sample data initialization completed. Created {} customers and {} cards",
                totalCustomers, totalCards);

        dataInitialized = true;

        // Verify we have active cards
        List<Card> activeCards = cardRepository.findByIsActiveTrue();
        log.info("Active cards available for transactions: {}", activeCards.size());

        if (activeCards.isEmpty()) {
            log.warn("No active cards found after data initialization!");
        }
    }

    @Scheduled(fixedDelayString = "${app.data.generation.transaction-interval-ms:5000}")
    public void generateScheduledTransaction() {
        if (!generationEnabled) {
            return;
        }

        // Check if data is initialized before generating transactions
        if (!dataInitialized) {
            log.warn("Data not initialized yet. Skipping scheduled transaction generation.");
            return;
        }

        try {
            avroTransactionGeneratorService.generateAndSendRandomTransaction();
        } catch (Exception e) {
            log.error("Error in scheduled transaction generation: {}", e.getMessage(), e);

            // If the error is due to no active cards, try to reinitialize data
            if (e.getMessage() != null && e.getMessage().contains("No active cards")) {
                log.warn("Attempting to reinitialize data due to no active cards...");
                dataInitialized = false;
                initializeSampleData();
            }
        }
    }

    // Add a method to manually trigger data initialization
    public synchronized void reinitializeData() {
        log.info("Manual data reinitialization triggered");
        dataInitialized = false;

        // Optional: Clear existing data
        transactionRepository.deleteAll();
        cardRepository.deleteAll();
        customerRepository.deleteAll();

        // Clear Redis cache
        //redisTemplate.getConnectionFactory().getConnection().flushDb();
        log.info("Cleared existing data and cache");

        // Reinitialize data
        initializeSampleData();
    }

    private Customer createSampleCustomer(Set<String> usedEmails) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String baseEmail = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com";

        // Ensure unique email
        String email = baseEmail;
        int attempt = 1;
        while (usedEmails.contains(email)) {
            email = firstName.toLowerCase() + "." + lastName.toLowerCase() + attempt + "@example.com";
            attempt++;

            // Safety check to prevent infinite loop
            if (attempt > 100) {
                log.error("Unable to generate unique email for {} {}", firstName, lastName);
                return null;
            }
        }

        usedEmails.add(email);

        return Customer.builder()
                .id(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(String.format("+1-%03d-%03d-%04d",
                        random.nextInt(1000), random.nextInt(1000), random.nextInt(10000)))
                .address(Customer.Address.builder()
                        .street(random.nextInt(1000) + " Main St")
                        .city(CITIES[random.nextInt(CITIES.length)])
                        .state("CA")
                        .zipCode(String.format("%05d", random.nextInt(100000)))
                        .country("USA")
                        .latitude(34.0522 + (random.nextDouble() - 0.5) * 10)
                        .longitude(-118.2437 + (random.nextDouble() - 0.5) * 10)
                        .build())
                .createdAt(LocalDateTime.now())
                .averageTransactionAmount(50.0 + random.nextDouble() * 200)
                .typicalTransactionHours("9,10,11,12,13,14,15,16,17,18")
                .build();
    }

    private Card createSampleCard(Customer customer) {
        return Card.builder()
                .id(UUID.randomUUID().toString())
                .cardNumber(generateCardNumber())
                .cardHolderName(customer.getFirstName() + " " + customer.getLastName())
                .expiryDate(LocalDate.now().plusYears(3))
                .cvv(String.format("%03d", random.nextInt(1000)))
                .cardType(random.nextBoolean() ? "VISA" : "MASTERCARD")
                .creditLimit(5000.0 + random.nextDouble() * 10000)
                .availableBalance(1000.0 + random.nextDouble() * 4000)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .customer(customer)
                .build();
    }

    private String generateCardNumber() {
        // Generate a more realistic card number (starts with 4 for Visa, 5 for Mastercard)
        String prefix = random.nextBoolean() ? "4" : "5";
        String middle = String.format("%014d", Math.abs(random.nextLong()) % 100000000000000L);
        return prefix + middle;
    }

    public long getActiveCardCount() {
        return cardRepository.findByIsActiveTrue().size();
    }

    public long getTotalCustomers() {
        return customerRepository.count();
    }

    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    public boolean isDataInitialized() {
        return dataInitialized;
    }

    public String getDataStatus() {
        if (!dataInitialized) {
            return "NOT_INITIALIZED";
        }

        long customers = getTotalCustomers();
        long activeCards = getActiveCardCount();

        if (customers == 0 || activeCards == 0) {
            return "INITIALIZED_BUT_NO_DATA";
        }

        return "READY";
    }
}