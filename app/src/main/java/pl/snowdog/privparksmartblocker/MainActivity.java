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

public class MainActivity extends Activity implements DistanceListener {

    private final String TAG = this.getClass().getName();
    private CameraHandler mCameraHandler;
    private ImagePreprocessor mImagePreprocessor;
    private UltrasonicSensorDriver mUltrasonicSensorDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        initCamera();
        initUltraSonicSensor();


        Log.d(TAG, "Available GPIO: " + service.getGpioList());
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
        mUltrasonicSensorDriver = new UltrasonicSensorDriver("BCM5",
                "BCM6", this);
    }

    private void onPhotoReady(Bitmap bitmap) {
        //sync to firebase
        Log.d(TAG, "Photo ready");
    }

    private void loadPhoto() {
        Log.d(TAG,"Take a photo");
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
