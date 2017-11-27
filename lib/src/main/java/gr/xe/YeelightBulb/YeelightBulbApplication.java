package gr.xe.YeelightBulb;

import gr.xe.YeelightBulb.bulb.BulbService;
import gr.xe.YeelightBulb.configuration.YeelightBindingConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class YeelightBulbApplication {

	public static void main(String[] args) {
		SpringApplication.run(YeelightBulbApplication.class, args);
	}


}

@Component
class SampleDataCLR implements CommandLineRunner {


	private final BulbService bulbService;
	private final YeelightBindingConfig yeelightBindingConfig;

	SampleDataCLR(BulbService bulbService, YeelightBindingConfig yeelightBindingConfig) {
		this.bulbService = bulbService;
		this.yeelightBindingConfig = yeelightBindingConfig;
	}


	@Override
	public void run(String... strings) throws Exception {
//		bulbService.sendYeelightCommand(yeelightBindingConfig.location(),"set_power",new Object[]{"off","smooth",500} );
//		Thread.sleep(1000);
//		bulbService.sendYeelightCommand(yeelightBindingConfig.location(),"set_power",new Object[]{"on","smooth",500} );
//		Thread.sleep(1000);
		bulbService.deployment();
		Thread.sleep(1000);
		bulbService.reset();
//		Thread.sleep(1000);
//		bulbService.sendYeelightCommand(yeelightBindingConfig.location(),"toggle",new Object[]{} );
//		Thread.sleep(1000);
//		bulbService.sendYeelightCommand(yeelightBindingConfig.location(),"toggle",new Object[]{} );
//		Thread.sleep(1000);
//		bulbService.sendYeelightCommand(yeelightBindingConfig.location(),"toggle",new Object[]{} );
		Thread.sleep(1000);
		System.exit(0);
	}
}