package pl.snowdog.privparksmartblocker.recognition;


import android.graphics.Bitmap;
import android.util.Log;

import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CarPlateRecogniser {

    private RecognitionListener mListener;
    private static final String TAG = "CarPlateRecogniser";
    private static final String FEATURE_LABEL = "TEXT_DETECTION";

    public CarPlateRecogniser(RecognitionListener listener) {
        this.mListener = listener;
    }

    public void recognise(Bitmap bitmap, final Vision vision) {

        Feature desiredFeature = new Feature();
        desiredFeature.setType(FEATURE_LABEL);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        Image inputImage = new Image();
        inputImage.encodeContent(imageBytes);


        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));


        final BatchAnnotateImagesRequest batchRequest =
                new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Waiting for recognition results... ");
                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();
                    final TextAnnotation text = batchResponse.getResponses()
                            .get(0).getFullTextAnnotation();
                    if (text != null) {
                        Log.d(TAG, "Found text: " + text.getText());
                        mListener.onFinishRecognition(filterResults(text));
                    } else {
                        Log.e(TAG, "Text not found");
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }).start();
    }

    private String filterResults(TextAnnotation textAnnotation) {
        String pattern = "[A-Z0-9]{2}[A-Z 0-9]{2}[A-Z0-9]{4}";
        Pattern p = Pattern.compile(pattern);
        String text ="";
        Matcher m = p.matcher(textAnnotation.getText());
        while(m.find()){
            text = m.group();
        }
        Log.d(TAG, "REGEX: " + text);
        return text;
    }
}
