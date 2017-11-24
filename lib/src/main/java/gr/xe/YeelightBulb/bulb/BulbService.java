package gr.xe.YeelightBulb.bulb;

import gr.xe.YeelightBulb.configuration.YeelightBindingConfig;
import gr.xe.YeelightBulb.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.HashMap;

import static gr.xe.YeelightBulb.types.HSBType.*;

/**
 * Created with IntelliJ IDEA.
 * User: zep
 * Date: 22/11/2017
 * Time: 3:45 μμ
 * Company: www.xe.gr
 */
@Service
public class BulbService {

    private static final Logger logger = LoggerFactory.getLogger(BulbService.class);
    private static final String MCAST_ADDR = "239.255.255.250";
    private static final int MCAST_PORT = 1982;
    private final int BUFFER_LENGTH = 1024;
    //Constants
    private final String TOGGLE = "toggle";
    private final String NIGHTLIGHT = "nightlight";
    private final String SMOOTH = "smooth";
    private final String SET_BRIGHT = "set_bright";
    private final String SET_SCENE = "set_scene";
    private final String SET_CF = "set_cf";
    private final String START_CF = "start_cf";
    private final String STOP_CF = "stop_cf";
    private final String SET_POWER = "set_power";
    private final String SET_CT = "set_ct";
    private final String SET_HSB = "set_hsb";
    private final String SET_RGB = "set_rgb";
    private final YeelightBindingConfig config;
    //devices
    private long msgid = 1;
    //Socket
    private HashMap<String, State> itemRegistry = new HashMap<>();

    public BulbService(YeelightBindingConfig config) {
        this.config = config;
    }


    /**
     * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
     * method and must not be accessed anymore once the deactivate() method was called or before activate()
     * was called.
     */
    public void deployment() throws InterruptedException {
        String deploymentSequence =
                "1000,1," + getRGBValue(BLUE.toColor()) + ",100," +
                "1000,1," + getRGBValue(GREEN.toColor()) + ",100," +
                "1000,1," + getRGBValue(RED.toColor()) + ",100";
        sendYeelightCommand(config.location(), START_CF, new Object[]{0, 0, deploymentSequence});
    }

    public void success() {
        internalReceiveCommand(config.location(), GREEN, SET_RGB);
    }

    public void failure() {
        internalReceiveCommand(config.location(), RED, SET_RGB);
    }

    public void reset() {
        internalReceiveCommand(config.location(), WHITE, SET_RGB);
    }

    private void processYeelightResult(YeelightGetPropsResponse result, String action, String itemName) {
        State newState = null;
        State oldState;
        switch (action) {
            case SET_POWER:
                String power = result.getResult().get(0);
                newState = power.equals("on") ? OnOffType.ON : OnOffType.OFF;
                break;
            case SET_BRIGHT:
                int bright = result.getResult().get(1).isEmpty() ? 0 : Integer.parseInt(result.getResult().get(1));
                newState = new PercentType(bright == 1 ? 0 : bright);
                break;
            case SET_CT:
                int ct = result.getResult().get(2).isEmpty() ? 0 : Integer.parseInt(result.getResult().get(2));
                newState = new PercentType((ct - 1700) / 48);
                break;
            case SET_HSB:
                int hue = result.getResult().get(3).isEmpty() ? 0 : Integer.parseInt(result.getResult().get(3));
                int sat = result.getResult().get(4).isEmpty() ? 0 : Integer.parseInt(result.getResult().get(4));
                int br = result.getResult().get(1).isEmpty() ? 0 : Integer.parseInt(result.getResult().get(1));
                newState = new HSBType(new DecimalType(hue), new PercentType(sat), new PercentType(br == 1 ? 0 : br));
                break;
            case SET_RGB:
                int rgb = result.getResult().get(5).isEmpty() ? 0 : Integer.parseInt(result.getResult().get(5));
                Color col = getRGBColor(rgb);
                newState = new HSBType(col);
                break;
            case NIGHTLIGHT:
                String status = result.getResult().get(6);
                newState = (status.equals("0") || status.equals("")) ? OnOffType.OFF : OnOffType.ON;
                break;
            default:
                logger.error("Unknown Yeelight action: {}", action);

        }

        oldState = itemRegistry.get(itemName);
        if (oldState == null || !oldState.equals(newState)) {
            itemRegistry.put(itemName, newState);
        }
    }


    protected void internalReceiveCommand(String location, Command command, String action) {
        switch (action) {
            case SET_POWER:
                if (command instanceof OnOffType) {
                    sendYeelightPowerCommand(location, command.toString().toLowerCase());
                }
                break;
            case NIGHTLIGHT:
                if (command instanceof OnOffType) {
                    sendYeelightNightModeCommand(location, command.equals(OnOffType.ON));
                }
                break;
            case TOGGLE:
                if (command instanceof OnOffType && command.equals(OnOffType.ON)) {
                    sendYeelightToggleCommand(location);
                }
                break;
            case SET_BRIGHT:
                sendYeelightBrightCommand(location, Integer.parseInt(command.toString()));
                break;
            case SET_CF:
                sendYeelightBrightCommand(location, Integer.parseInt(command.toString()));
                break;
            case SET_CT:
                sendYeelightCTCommand(location, 1700 + 48 * Integer.parseInt(command.toString()));
                break;
            case SET_HSB:
                if (command instanceof HSBType) {
                    HSBType hsb = (HSBType) command;
                    sendYeelightHSCommand(location, hsb.getHue().intValue(), hsb.getSaturation().intValue());
                    sendYeelightBrightCommand(location, hsb.getBrightness().intValue());
                } else if (command instanceof OnOffType) {
                    sendYeelightPowerCommand(location, command.toString().toLowerCase());
                }
                break;
            case SET_RGB:
                if (command instanceof HSBType) {
                    HSBType hsb = (HSBType) command;
                    sendYeelightRGBCommand(location, hsb.getRed().intValue(), hsb.getGreen().intValue(), hsb.getBlue().intValue());
                    sendYeelightBrightCommand(location, hsb.getBrightness().intValue());
                } else if (command instanceof OnOffType) {
                    sendYeelightPowerCommand(location, command.toString().toLowerCase());
                }
                break;
            default:
                logger.error("Unknown Yeelight command: {}", action);
        }

    }

    private String sendYeelightToggleCommand(String location) {
        return sendYeelightCommand(location, TOGGLE, new Object[]{});
    }

    private String sendYeelightBrightCommand(String location, int param) {
        return sendYeelightCommand(location, SET_BRIGHT, new Object[]{param == 0 ? 1 : param, SMOOTH, 500});
    }

    private String sendYeelightNightModeCommand(String location, boolean mode) {
        if (mode) {
            return sendYeelightCommand(location, SET_SCENE, new Object[]{NIGHTLIGHT, 1});
        } else {
            return sendYeelightCTCommand(location, 4000);
        }
    }

    private String sendYeelightRGBCommand(String location, int red, int green, int blue) {
        return sendYeelightCommand(location, SET_RGB, new Object[]{getRGBValue(red, green, blue), SMOOTH, 500});
    }

    private String sendYeelightHSCommand(String location, int hue, int saturation) {
        return sendYeelightCommand(location, "set_hsv", new Object[]{hue, saturation, SMOOTH, 500});
    }

    private String sendYeelightCTCommand(String location, int param) {
        return sendYeelightCommand(location, "set_ct_abx", new Object[]{param, SMOOTH, 500});
    }

    private String sendYeelightPowerCommand(String location, String param) {
        return sendYeelightCommand(location, SET_POWER, new Object[]{param, "", 0});
    }

    private String sendYeelightCommand(String location, String action, Object[] params) {
        int index = location.indexOf(":");
        Socket clientSocket = null;
        try {
            String ip = location.substring(0, index);
            int port = Integer.parseInt(location.substring(index + 1));
            clientSocket = new Socket(ip, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String sentence = "{\"id\":" + msgid++ + ",\"method\":\"" + action + "\",\"params\":[" + getProperties(params) + "]}\r\n";
            logger.debug("Sending sentence: {}", sentence);
            outToServer.writeBytes(sentence);
            return inFromServer.readLine();
        } catch (NoRouteToHostException e) {
            logger.debug("Location {} is probably offline", location);
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {

            if (clientSocket != null)
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    //silence
                }
        }
        return null;
    }

    private int getRGBValue(int red, int green, int blue) {
        return red * 65536 + green * 256 + blue;
    }

    private int getRGBValue(Color color) {
        return color.getRed() * 65536 + color.getGreen() * 256 + color.getBlue();
    }

    private Color getRGBColor(int rgb) {
        int red = rgb / 65536;
        int green = (rgb - red * 65536) / 256;
        int blue = rgb - red * 65536 - green * 256;
        return new Color(red, green, blue);

    }

    private String getProperties(Object[] properties) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object o : properties) {
            if (!first)
                builder.append(",");
            else
                first = false;

            if (o instanceof String) {
                builder.append("\"");
                builder.append(o);
                builder.append("\"");
            } else
                builder.append(o);
        }
        return builder.toString();
    }


}