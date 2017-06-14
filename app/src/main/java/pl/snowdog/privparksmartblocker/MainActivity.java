package pl.snowdog.privparksmartblocker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.ultrasonicsensor.DistanceListener;
import com.google.android.things.pio.PeripheralManagerService;

public class MainActivity extends Activity implements DistanceListener {

    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());

    }

    @Override
    public void onDistanceChange(double distanceInCm) {

    }
}
