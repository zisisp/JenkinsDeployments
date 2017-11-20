package gr.xe.LightjenkinsCheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LightjenkinsCheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightjenkinsCheckApplication.class, args);
	}
}
