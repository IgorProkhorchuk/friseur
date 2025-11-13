package de.friseur.friseur;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

/**
 * Entry point for the Friseur appointment scheduling application.
 * Bootstraps the Spring context and starts the embedded server.
 */
@SpringBootApplication
public class FriseurApplication {

	/**
	 * Bootstraps the Spring application context.
	 *
	 * @param args command line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(FriseurApplication.class, args);
	}

}
