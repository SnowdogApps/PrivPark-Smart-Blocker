package pl.snowdog.privparksmartblocker.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ImagePreprocessor {
    private static final boolean SAVE_PREVIEW_BITMAP = true;

    private Bitmap rgbFrameBitmap;
    private Bitmap croppedBitmap;

    public ImagePreprocessor() {
        this.croppedBitmap = Bitmap.createBitmap(CameraHandler.WIDTH, CameraHandler.HEIGHT,
                Bitmap.Config.ARGB_8888);
        this.rgbFrameBitmap = Bitmap.createBitmap(CameraHandler.WIDTH,
                CameraHandler.HEIGHT, Bitmap.Config.ARGB_8888);
    }

    public Bitmap preprocessImage(final Image image) {
        if (image == null) {
            return null;
        }
        if (croppedBitmap != null && rgbFrameBitmap != null) {
            ByteBuffer bb = image.getPlanes()[0].getBuffer();
            rgbFrameBitmap = BitmapFactory.decodeStream(new ByteBufferBackedInputStream(bb));
            Helper.cropAndRescaleBitmap(rgbFrameBitmap, croppedBitmap, 180);
        }

        image.close();

            if (SAVE_PREVIEW_BITMAP) {
                Helper.saveBitmap(croppedBitmap);
            }

        return croppedBitmap;
    }

    private static class ByteBufferBackedInputStream extends InputStream {

        ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        public int read() throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get() & 0xFF;
        }

        public int read(byte[] bytes, int off, int len)
                throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }

            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }
    }

}
