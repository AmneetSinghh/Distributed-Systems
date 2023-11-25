package com.example.deliveryservice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DeliveryServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(DeliveryServiceApplication.class, args);
	}
}
