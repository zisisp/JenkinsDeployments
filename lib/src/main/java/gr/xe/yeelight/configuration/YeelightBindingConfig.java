package gr.xe.yeelight.configuration;

import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 22/11/2017
 * Time: 4:49 μμ
 * Company: www.xe.gr
 */
@Component
public class YeelightBindingConfig {
    private final String ip;
    private final int port;

    public String location() {
        return String.format("%s:%d",ip,port);
    }

    public YeelightBindingConfig(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}