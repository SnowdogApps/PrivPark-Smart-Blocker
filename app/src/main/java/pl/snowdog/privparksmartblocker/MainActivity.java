package pl.snowdog.privparksmartblocker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.contrib.driver.ultrasonicsensor.DistanceListener;
import com.google.android.things.contrib.driver.ultrasonicsensor.UltrasonicSensorDriver;
import com.google.android.things.pio.PeripheralManagerService;

import pl.snowdog.privparksmartblocker.camera.CameraHandler;
import pl.snowdog.privparksmartblocker.camera.ImagePreprocessor;
import pl.snowdog.privparksmartblocker.config.BoardConfig;
import pl.snowdog.privparksmartblocker.config.GeneralConfig;
import pl.snowdog.privparksmartblocker.db.DatabaseListener;
import pl.snowdog.privparksmartblocker.db.RemoteDbProvider;
import pl.snowdog.privparksmartblocker.led.LedManager;

import static pl.snowdog.privparksmartblocker.config.GeneralConfig.DELTA;
import static pl.snowdog.privparksmartblocker.config.GeneralConfig.DISTANCE_ARRAY_SIZE;
import static pl.snowdog.privparksmartblocker.config.GeneralConfig.INTERVAL_BETWEEN_BLINKS_MS;
import static pl.snowdog.privparksmartblocker.config.GeneralConfig.MEASURE_ERROR;
import static pl.snowdog.privparksmartblocker.config.GeneralConfig.MIN_FREE_SPOT_DISTANCE;
import static pl.snowdog.privparksmartblocker.config.GeneralConfig.MIN_PHOTO_DISTANCE;

public class MainActivity extends Activity implements DistanceListener, DatabaseListener {

    private final String TAG = this.getClass().getName();
    private CameraHandler mCameraHandler;
    private ImagePreprocessor mImagePreprocessor;
    private boolean mIsPhotoTaken = false;
    private boolean mBlinking = true;
    private UltrasonicSensorDriver mUltrasonicSensorDriver;
    private PeripheralManagerService mService;
    private Handler mHandler = new Handler();

    private double[] mTempDistance = new double[DISTANCE_ARRAY_SIZE];
    private int mDistanceCounter = 0;

    private RemoteDbProvider mDbProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService = new PeripheralManagerService();
        initDb();
        initCamera();
        initUltraSonicSensor();
        LedManager.initLED(mService);
        initTempDistanceArray();
        Log.d(TAG, "Available GPIO: " + mService.getGpioList());
    }

    @Override
    public void onDistanceChange(double distanceInCm) {
        if (mDbProvider.isSpotAvailable()) {
            if (distanceInCm < MEASURE_ERROR) {
                Log.d("Distance", distanceInCm + " cm");
                if (distanceInCm < MIN_PHOTO_DISTANCE && isCarPark(distanceInCm)) {
                    loadPhoto();
                } else {
                    if (distanceInCm > MIN_FREE_SPOT_DISTANCE) {
                        mIsPhotoTaken = false;
                        LedManager.turnOnYellowLedOnly();
                        mDbProvider.setState(false);
                    }
                }
            }
        }
    }

    private void initCamera() {
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

    private void initUltraSonicSensor() {
        mUltrasonicSensorDriver = new UltrasonicSensorDriver(BoardConfig.getUltrasonicTriggerPin(),
                BoardConfig.getUltrasonicEchoPin(), this);
    }

    private void initDb() {
        mDbProvider = new RemoteDbProvider();
        mDbProvider.initDb(this);
    }


    private void initTempDistanceArray() {
        for (int i = 0; i < mTempDistance.length; i++) {
            mTempDistance[i] = 1000.0;
        }

    }

    private void onPhotoReady(Bitmap bitmap) {
        Log.d(TAG, "Photo ready");
        LedManager.turnOffWhiteLed();
        mBlinking = true;
        mHandler.removeCallbacks(mBlinkRunnable);
        mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        startRecognition(bitmap);
    }

    private void loadPhoto() {
        if (!mIsPhotoTaken) {
            Log.d(TAG, "Take a photo");
            LedManager.turnOnWhiteLed();
            mCameraHandler.takePicture();
            mIsPhotoTaken = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LedManager.closeAllPins();
        try {
            mUltrasonicSensorDriver.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.removeCallbacks(mBlinkRunnable);
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {

            LedManager.negateYellowLedState();
            Log.d(TAG, "blinking: " + mBlinking);
            if (mBlinking) {
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            }

        }
    };

    private void startRecognition(Bitmap bitmap) {
        mDbProvider.setCarPlate("zz 1256");
        if (GeneralConfig.OFFLINE_MODE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlinking = false;
                    mHandler.removeCallbacks(mBlinkRunnable);
                    LedManager.turnOnGreenLedOnly();
                }
            }, 2000);
        }
    }

    private boolean isCarPark(double distance) {
        mTempDistance[mDistanceCounter] = distance;
        mDistanceCounter++;
        mDistanceCounter = mDistanceCounter % DISTANCE_ARRAY_SIZE;
        double sum = 0.0;
        Log.d(TAG, "distance counter: " + mDistanceCounter);
        Log.d(TAG, "sum " + sum);
        for (int i = 1; i < mTempDistance.length; i++) {
            sum += mTempDistance[i] - mTempDistance[i - 1];
            Log.d(TAG, "table: " + mTempDistance[i]);
        }
        double average = sum / DISTANCE_ARRAY_SIZE;
        Log.d(TAG, "average: " + average);
        if (Math.abs(average) < DELTA) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onDbStateChange(boolean value) {
        if (value) {
            mBlinking = false;
            mHandler.removeCallbacks(mBlinkRunnable);
            LedManager.turnOnGreenLedOnly();
        }

    }

    @Override
    public void onDbSpotAvailabilityChange(boolean value) {
        if (!value) {
            LedManager.turnOnRedLedOnly();
        } else {
            LedManager.turnOnYellowLedOnly();
        }

    }
}
