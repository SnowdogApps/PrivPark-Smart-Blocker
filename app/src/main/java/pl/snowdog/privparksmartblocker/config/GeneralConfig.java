package pl.snowdog.privparksmartblocker.config;


public class GeneralConfig {
    public static final boolean OFFLINE_MODE = false;

    public static final int INTERVAL_BETWEEN_BLINKS_MS = 100;

    public static double DELTA = 5.0;
    public static final int DISTANCE_ARRAY_SIZE = 3;

    public static int MIN_PHOTO_DISTANCE = 20;
    public static int MIN_FREE_SPOT_DISTANCE = 100;

    public static int MEASURE_ERROR = 1000;
}
