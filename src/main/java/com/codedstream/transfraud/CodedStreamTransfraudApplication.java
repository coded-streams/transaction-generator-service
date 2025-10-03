package com.codedstream.transfraud;

import com.codedstream.transfraud.service.DataGeneratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodedStreamTransfraudApplication implements CommandLineRunner {

    private final DataGeneratorService dataGeneratorService;

    public CodedStreamTransfraudApplication(DataGeneratorService dataGeneratorService) {
        this.dataGeneratorService = dataGeneratorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CodedStreamTransfraudApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        dataGeneratorService.initializeSampleData();
    }
}
