package pl.snowdog.privparksmartblocker.led;


import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import pl.snowdog.privparksmartblocker.config.BoardConfig;


public class LedManager {

    private static Gpio mWhiteLED;
    private static Gpio mYellowLED;
    private static Gpio mGreenED;
    private static Gpio mRedLED;

    private static final String TAG = "LedManager";


    public static void initLED(PeripheralManagerService service) {
        try {
            mWhiteLED = service.openGpio(BoardConfig.getLedWhitePin());
            mWhiteLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mGreenED = service.openGpio(BoardConfig.getLedGreenPin());
            mGreenED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mYellowLED = service.openGpio(BoardConfig.getLedYellowPin());
            mYellowLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mYellowLED.setValue(true);

            mRedLED = service.openGpio(BoardConfig.getLedRedPin());
            mRedLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            Log.e(TAG, "LED not found");
        }
    }

    public static void turnOnYellowLedOnly() {
        try {
            mGreenED.setValue(false);
            mYellowLED.setValue(true);
            mRedLED.setValue(false);
            mWhiteLED.setValue(false);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public static void turnOnGreenLedOnly() {
        try {
            mGreenED.setValue(true);
            mYellowLED.setValue(false);
            mRedLED.setValue(false);
            mWhiteLED.setValue(false);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void turnOnRedLedOnly() {
        try {
            mGreenED.setValue(false);
            mYellowLED.setValue(false);
            mRedLED.setValue(true);
            mWhiteLED.setValue(false);

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void turnOnWhiteLed() {
        try {
            mWhiteLED.setValue(true);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void turnOffWhiteLed() {
        try {
            mWhiteLED.setValue(false);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void negateYellowLedState() {

        try {
            mYellowLED.setValue(!mYellowLED.getValue());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public static void closeAllPins() {
        try {
            mWhiteLED.close();
            mYellowLED.close();
            mRedLED.close();
            mGreenED.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
