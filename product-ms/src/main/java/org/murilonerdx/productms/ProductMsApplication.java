package org.murilonerdx.productms;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class ProductMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductMsApplication.class, args);
	}

}
