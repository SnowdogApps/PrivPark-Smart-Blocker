package pl.snowdog.privparksmartblocker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.contrib.driver.ultrasonicsensor.DistanceListener;
import com.google.android.things.contrib.driver.ultrasonicsensor.UltrasonicSensorDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import pl.snowdog.privparksmartblocker.camera.CameraHandler;
import pl.snowdog.privparksmartblocker.camera.ImagePreprocessor;

public class MainActivity extends Activity implements DistanceListener {

    private final String TAG = this.getClass().getName();
    private CameraHandler mCameraHandler;
    private ImagePreprocessor mImagePreprocessor;
    private UltrasonicSensorDriver mUltrasonicSensorDriver;
    PeripheralManagerService mService;
    private static final String TRIGGER_PIN = "BCM5";
    private static final String ECHO_PIN = "BCM6";
    private static final String LED_WHITE_PIN = "BCM17";
    private static final String LED_WHITE_TWO_PIN = "BCM27";
    private Gpio mWhiteLED;
    private Gpio mSecondWhiteLED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         mService = new PeripheralManagerService();
        initCamera();
        initUltraSonicSensor();
        initLED();


        Log.d(TAG, "Available GPIO: " + mService.getGpioList());
    }


    @Override
    public void onDistanceChange(double distanceInCm) {
        Log.d("Distance", distanceInCm + " cm");
        if (distanceInCm < 25){
            loadPhoto();
        }

    }

    private void initCamera(){
        mImagePreprocessor = new ImagePreprocessor();
        mCameraHandler = CameraHandler.getInstance();
        Handler threadLooper = new Handler(getMainLooper());
        mCameraHandler.initCamera(this, threadLooper,
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Bitmap bitmap = mImagePreprocessor.preprocessImage(imageReader.acquireNextImage(), MainActivity.this);
                        onPhotoReady(bitmap);
                    }
                });

    }

    private void initUltraSonicSensor(){
        mUltrasonicSensorDriver = new UltrasonicSensorDriver(TRIGGER_PIN,
                ECHO_PIN, this);
    }

    private void initLED(){
        try{

            mWhiteLED = mService.openGpio(LED_WHITE_PIN);
            mWhiteLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mWhiteLED.setValue(false);

        }catch (IOException e){
            Log.e(TAG, "LED not found");
        }

    }

    private void onPhotoReady(Bitmap bitmap) {
        //sync to firebase
        Log.d(TAG, "Photo ready");
        try{
            mWhiteLED.setValue(false);
        }catch (IOException e){
            Log.e(TAG, "LED not found");
        }
    }

    private void loadPhoto() {
        Log.d(TAG,"Take a photo");
        try{
            mWhiteLED.setValue(true);
        }catch (IOException e){
            Log.e(TAG, "LED not found");
        }
        mCameraHandler.takePicture();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mUltrasonicSensorDriver.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
