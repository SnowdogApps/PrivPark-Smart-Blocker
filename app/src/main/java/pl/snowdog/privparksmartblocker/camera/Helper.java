package pl.snowdog.privparksmartblocker.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class Helper {
    private static final String TAG = "ImageHelper";


    public static void saveBitmap(final Bitmap bitmap) {
        final File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "preview.jpg");

        Log.d(TAG, String.format("Saving %dx%d bitmap to %s.",
                bitmap.getWidth(), bitmap.getHeight(), file.getAbsolutePath()));

        if (file.exists()) {
            file.delete();
        }
        try (FileOutputStream fs = new FileOutputStream(file);
             BufferedOutputStream out = new BufferedOutputStream(fs)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, out);
        } catch (final Exception e) {
            Log.w(TAG, "Could not save image for debugging. " + e.getMessage());
        }
    }

    public static void cropAndRescaleBitmap(final Bitmap src, final Bitmap dst, int sensorOrientation) {

        final Matrix matrix = new Matrix();
        if (sensorOrientation != 0) {
            matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
            matrix.postRotate(sensorOrientation);
            matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
        }
        final Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, matrix, null);
    }
}