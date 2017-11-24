package gr.xe.YeelightBulb.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 23/11/2017
 * Time: 4:07 μμ
 * Company: www.xe.gr
 */
@ConfigurationProperties("yeelight")
public class LightProperties {
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
