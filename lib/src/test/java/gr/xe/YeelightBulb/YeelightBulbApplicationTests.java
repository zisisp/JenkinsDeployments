package gr.xe.YeelightBulb;

import gr.xe.YeelightBulb.configuration.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit4.*;

@RunWith(SpringRunner.class)
@SpringBootTest({"yeelight.port=123"})
public class YeelightBulbApplicationTests {
    @Autowired
    private YeelightBindingConfig yeelightBindingConfig;

//    @Test
//    public void contextLoads() {
//        System.out.println("yeelightBindingConfig.location() = " + yeelightBindingConfig.location());
//        assertThat(yeelightBindingConfig.location()).isNotNull();
//        assertThat(yeelightBindingConfig.location()).contains("123.123.123.123:123");
//    }

    @SpringBootApplication
    @Import(LightConfiguration.class)
    static class TestConfiguration {
    }
}