package pl.snowdog.privparksmartblocker.config;


public class BoardConfig {

    private static final String LED_WHITE_PIN = "BCM17";
    private static final String LED_YELLOW_PIN = "BCM27";
    private static final String LED_GREEN_PIN = "BCM22";
    private static final String LED_RED_PIN = "BCM23";

    private static final String ULTRASONIC_TRIGGER_PIN = "BCM5";
    private static final String ULTRASONIC_ECHO_PIN = "BCM6";

    public static String getLedWhitePin() {
        return LED_WHITE_PIN;
    }

    public static String getLedYellowPin() {
        return LED_YELLOW_PIN;
    }

    public static String getLedGreenPin() {
        return LED_GREEN_PIN;
    }

    public static String getLedRedPin() {
        return LED_RED_PIN;
    }

    public static String getUltrasonicTriggerPin() {
        return ULTRASONIC_TRIGGER_PIN;
    }

    public static String getUltrasonicEchoPin() {
        return ULTRASONIC_ECHO_PIN;
    }
}
