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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import pl.snowdog.privparksmartblocker.camera.CameraHandler;
import pl.snowdog.privparksmartblocker.camera.ImagePreprocessor;

public class MainActivity extends Activity implements DistanceListener {

    private final String TAG = this.getClass().getName();
    private CameraHandler mCameraHandler;
    private ImagePreprocessor mImagePreprocessor;
    private boolean mIsPhotoTaken = false;
    private boolean mBlinking = true;
    private UltrasonicSensorDriver mUltrasonicSensorDriver;
    PeripheralManagerService mService;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 100;
    private static final String TRIGGER_PIN = "BCM5";
    private static final String ECHO_PIN = "BCM6";
    private static final String LED_WHITE_PIN = "BCM17";
    private static final String LED_YELLOW_PIN = "BCM27";
    private static final String LED_GREEN_PIN = "BCM22";
    private static final String LED_RED_PIN = "BCM23";
    private static final int DISTANCE_ARRAY_SIZE = 3;
    private Gpio mWhiteLED;
    private Gpio mYellowLED;
    private Gpio mGreenED;
    private Gpio mRedLED;
    private Handler mHandler = new Handler();
    private FirebaseDatabase mDatabase;
    private double DELTA = 5.0;
    private int MIN_PHOTO_DISTANCE = 20;

    private double[] mTempDistance = new double[DISTANCE_ARRAY_SIZE];
    private int mDistanceCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService = new PeripheralManagerService();
        initCamera();
        initUltraSonicSensor();
        initLED();
        initDb();
        initTempDistanceArray();
        Log.d(TAG, "Available GPIO: " + mService.getGpioList());
    }

    @Override
    public void onDistanceChange(double distanceInCm) {
        if (distanceInCm < 1000) {
            Log.d("Distance", distanceInCm + " cm");
            if (distanceInCm < MIN_PHOTO_DISTANCE && isCarPark(distanceInCm)) {
                loadPhoto();
            } else {
                if (distanceInCm > 100) {

                    mIsPhotoTaken = false;
                    try {
                        mGreenED.setValue(false);
                        mYellowLED.setValue(true);
                        mRedLED.setValue(false);
                        mWhiteLED.setValue(false);
                    } catch (IOException e) {
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
        mUltrasonicSensorDriver = new UltrasonicSensorDriver(TRIGGER_PIN,
                ECHO_PIN, this);
    }

    private void initDb() {
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference stateReference = mDatabase.getReference("state");
        stateReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value = dataSnapshot.getValue(boolean.class);
                if (value) {
                    try {
                        mGreenED.setValue(true);
                        mYellowLED.setValue(false);
                        mRedLED.setValue(false);
                        mWhiteLED.setValue(false);
                    } catch (IOException e) {
                    }
                    mBlinking = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mBlinking = false;
            }
        });
        DatabaseReference spotAvailabilityReference = mDatabase.getReference("spot_available");
        spotAvailabilityReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean value = dataSnapshot.getValue(boolean.class);
                if (!value) {
                    try {
                        mGreenED.setValue(false);
                        mYellowLED.setValue(false);
                        mRedLED.setValue(true);
                        mWhiteLED.setValue(false);
                    } catch (IOException e) {
                    }
                } else {
                    try {
                        mWhiteLED.setValue(false);
                        mGreenED.setValue(false);
                        mYellowLED.setValue(true);
                        mRedLED.setValue(false);
                    } catch (IOException e) {
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initLED() {
        try {
            mWhiteLED = mService.openGpio(LED_WHITE_PIN);
            mWhiteLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mGreenED = mService.openGpio(LED_GREEN_PIN);
            mGreenED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mYellowLED = mService.openGpio(LED_YELLOW_PIN);
            mYellowLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mYellowLED.setValue(true);
            mRedLED = mService.openGpio(LED_RED_PIN);
            mRedLED.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            Log.e(TAG, "LED not found");
        }
    }

    private void initTempDistanceArray() {
        for (int i = 0; i < mTempDistance.length; i++) {
            mTempDistance[i] = 1000.0;
        }

    }

    private void onPhotoReady(Bitmap bitmap) {
        Log.d(TAG, "Photo ready");
        try {
            mWhiteLED.setValue(false);
        } catch (IOException e) {
            Log.e(TAG, "LED not found");
        }
        mBlinking = true;
        mHandler.removeCallbacks(mBlinkRunnable);
        mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        startRecognition(bitmap);
    }

    private void loadPhoto() {
        if (!mIsPhotoTaken) {
            Log.d(TAG, "Take a photo");
            try {
                mWhiteLED.setValue(true);
            } catch (IOException e) {
                Log.e(TAG, "LED not found");
            }
            mCameraHandler.takePicture();
            mIsPhotoTaken = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mUltrasonicSensorDriver.close();
            mWhiteLED.close();
            mYellowLED.close();
            mRedLED.close();
            mGreenED.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.removeCallbacks(mBlinkRunnable);
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {

            if (mYellowLED == null) {
                return;
            }
            try {
                mYellowLED.setValue(!mYellowLED.getValue());
                Log.d(TAG, "blinking: " + mBlinking);
                if (mBlinking) {
                    mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
    private void startRecognition(Bitmap bitmap) {
        DatabaseReference myRef = mDatabase.getReference("car_plate");
        myRef.setValue("ZZ 1235556");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mBlinking = false;
                    mHandler.removeCallbacks(mBlinkRunnable);
                    mYellowLED.setValue(false);
                    mGreenED.setValue(true);
                } catch (IOException e) {
                }
            }
        }, 2000);
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
        Log.d(TAG, "sum: " + sum);
        double average = sum / DISTANCE_ARRAY_SIZE;
        Log.d(TAG, "average: " + average);
        if (Math.abs(average) < DELTA) {
            return true;
        } else {
            return false;
        }
    }
}
