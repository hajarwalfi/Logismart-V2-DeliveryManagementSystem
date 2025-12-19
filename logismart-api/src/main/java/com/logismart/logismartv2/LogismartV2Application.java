package com.logismart.logismartv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.logismart.logismartv2",
		"com.logismart.security"
})
@EnableJpaRepositories(basePackages = {
		"com.logismart.logismartv2.repository",
		"com.logismart.security.repository"
})
@EntityScan(basePackages = {
		"com.logismart.logismartv2.entity",
		"com.logismart.security.entity"
})
public class LogismartV2Application {

	public static void main(String[] args) {
		SpringApplication.run(LogismartV2Application.class, args);
	}

}
