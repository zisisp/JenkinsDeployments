package gr.xe.YeelightBulb;

import gr.xe.YeelightBulb.configuration.LightConfiguration;
import gr.xe.YeelightBulb.configuration.YeelightBindingConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest("yeelight.port=123")
public class YeelightBulbApplicationTests {
    @Autowired
    private YeelightBindingConfig yeelightBindingConfig;

    @Test
    public void contextLoads() {
        System.out.println("yeelightBindingConfig.location() = " + yeelightBindingConfig.location());
        assertThat(yeelightBindingConfig.location()).isNotNull();
        assertThat(yeelightBindingConfig.location()).contains("null:123");
    }

    @SpringBootApplication
    @Import(LightConfiguration.class)
    static class TestConfiguration {
    }
}