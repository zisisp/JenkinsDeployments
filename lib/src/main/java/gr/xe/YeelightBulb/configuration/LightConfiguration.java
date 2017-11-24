package gr.xe.YeelightBulb.configuration;

import gr.xe.YeelightBulb.bulb.BulbService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 23/11/2017
 * Time: 4:24 μμ
 * Company: www.xe.gr
 */
@Configuration
@EnableConfigurationProperties(LightProperties.class)
public class LightConfiguration {
    @Bean
    public YeelightBindingConfig yeelightBindingConfig(LightProperties lightProperties) {
        return new YeelightBindingConfig(lightProperties.getIp(), lightProperties.getPort());
    }

    @Bean
    public BulbService bulbService(YeelightBindingConfig yeelightBindingConfig) {
        return new BulbService(yeelightBindingConfig);
    }
}
