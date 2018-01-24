package gr.xe.jenkins.deployments;

import gr.xe.yeelight.configuration.LightConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Import(LightConfiguration.class)
public class LightjenkinsCheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightjenkinsCheckApplication.class, args);
	}
}
