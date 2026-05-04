package io.kk.userinsightsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UserInsightsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserInsightsServiceApplication.class, args);
    }

}
