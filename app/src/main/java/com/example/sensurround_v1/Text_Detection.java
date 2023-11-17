//**************************************************************************************************
//SENSURROUND : A DIFFERENT WAY OF LIFE
//Sensurround is an assistive app that helps blind and visually impaired users to find objects
//independently. It provides a set of digital eyes to make the physical world more accessible for
//the blind and low vision community.

//CURRENT VERSION 1.10

//APIs USED GOOGLE'S SPEECH RECOGNITION, ML TOOLKIT CUSTOM MODEL(model.tflite) FOR OBJECT DETECTION,
//DEFAULT ML TOOLKIT FOR TEXT RECOGNITION AND INBUILT TEXT-TO-SPEECH.

//CREATED BY : MEET PATEL(18CE079)
//             MEET PATEL(18CE080)
//             NIRMIT PATEL(18CE083)
//             KARAN PRADHAN(18CE092)

//GUIDED BY : PROF. RONAK PATEL
//**************************************************************************************************

//THIS IS THE TEXT DETECTION ACTIVITY IN WHICH USER POINTS THE CAMERA TOWARDS A TEXT AND THIS
//APPLICATION WILL START SAYING THE PARTICULAR TEXT.
package com.example.sensurround_v1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

public class Text_Detection extends AppCompatActivity {
    Preview preview = new Preview.Builder()                                                         //Here we initialize camera preview object
            .build();

    CameraSelector cameraSelector = new CameraSelector.Builder()                                    //Here we initialize camera selector object which will help
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)                                     // us in opening camera with its lens facing backward
            .build();

    ImageAnalysis imageAnalysis =                                                                   //Here we initialize image analysis object with a target
            new ImageAnalysis.Builder()                                                             //resolution as 1280 x 720 and back pressure strategy as
                    .setTargetResolution(new Size(1280, 720))                          //KEEP_ONLY_LATEST so that it won't flood our model with
                    .build();                                                                       //large number of frame which will in turn result in lag.

    private class YourAnalyzer implements ImageAnalysis.Analyzer {                                  //Here we created a class for our analyser implementing image
        //analyser


        @Override
        public void analyze(ImageProxy imageProxy) {

            @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image =                                                                  //Here we created input image which we contain image frame,
                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());//image info and its rotational degrees.
                TextRecognizer recognizer = TextRecognition.getClient();
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {                       //Here on success the detected text in image is stored in labels
                            @Override
                            //which is a string, which then is passed to tts with a pause of
                            public void onSuccess(Text visionText) {
                                int size = visionText.getTextBlocks().size();
                                int iter = size;                                                    //0.2s.
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String text = block.getText();
                                    String textt = text;
                                    Log.d("block", textt);
                                    Log.d("size", String.valueOf(size));
                                    Log.d("iter ", String.valueOf(iter));
                                    if (iter > 0) {
                                        finals = finals.concat(text).concat(" ");
                                        iter -= 1;
                                    }

                                    if (iter == 0) {
                                        if (!textToSpeech.isSpeaking()) {
                                            speak(finals);
                                        }
                                        Log.d("final", finals);
                                        checker = true;
                                        previous = finals;

                                    }

                                    if (checker) {
                                        finals = "";
                                        checker = false;
                                    }
                                }
                                imageProxy.close();                                                 //The above process is repeated as many times as camera frame
                            }                                                                       //changes and atlast we close image proxy after work is done.

                        })
                        .addOnFailureListener(
                                new OnFailureListener() {                                           //We close image proxy on failure resulting in the
                                    @Override
                                    //termination of the app.
                                    public void onFailure(@NonNull Exception e) {
                                        imageProxy.close();
                                    }
                                });
            }
        }
    }

    private void speak(String finals) {
        textToSpeech.speak(finals, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    TextView labels, confidences;
    TextToSpeech textToSpeech;
    Button object_detection;
    String finals = "", previous = "";
    boolean flash_on = false, flash_off = false, checker = false;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    androidx.camera.view.PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text__detection);
        getSupportActionBar().hide();                                                               //Hiding action bar
        previewView = findViewById(R.id.previewView);                                               //Here we connect all our containers/components of UI with
        labels = findViewById(R.id.answer);                                                         //backend.
        confidences = findViewById(R.id.answer1);
        object_detection = findViewById(R.id.object_button);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {//Here we initialize the object for Text-To-Speech
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);                                            //Setting language as Uk
                }
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {                                                                     //welcome audio when activity starts
                textToSpeech.speak("Looking for text please give us a second", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, 1000);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));


        object_detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                        //On click listener to navigate back to object detection.
                Intent intent = new Intent(Text_Detection.this, homeactivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {                               //This function helps us to bind preview with camera provider.
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new YourAnalyzer());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
    }


    void flashOn(@NonNull ProcessCameraProvider cameraProvider) {                                   //This function will be invoked whenever flash needs to be
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new YourAnalyzer()); //activated.
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
        camera.getCameraControl().enableTorch(true);
    }

    void flashOff(@NonNull ProcessCameraProvider cameraProvider) {                                  //This function will be invoked whenever flash needs to be
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new YourAnalyzer()); //disabled.
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
        camera.getCameraControl().enableTorch(false);
    }

    public void getSpeechInput(View view) {                                                         //This function will be called whenever the user clicks on
        //mic icon to give his/her speech input.
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {       //From here the below code is how the app will react
        super.onActivityResult(requestCode, resultCode, data);                                      //according to different speech input given to it.
        switch (requestCode) {

            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> audioInputArray = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String audioInput = audioInputArray.get(0);

                    String flashOn = "flash on";
                    if (audioInput.toLowerCase().indexOf(flashOn.toLowerCase()) > -1) {
                        if (flash_on) {
                            textToSpeech.speak("Flash is already on", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                            break;
                        }
                        flash_on = true;
                        textToSpeech.speak("Flash turned on", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                        cameraProviderFuture.addListener(() -> {
                            try {
                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                                flashOn(cameraProvider);


                            } catch (ExecutionException | InterruptedException e) {
                                // No errors need to be handled for this Future.
                                // This should never be reached.
                            }
                        }, ContextCompat.getMainExecutor(this));
                    }

                    String onFlash = "on flash";
                    if (audioInput.toLowerCase().indexOf(onFlash.toLowerCase()) > -1) {
                        if (flash_on) {
                            textToSpeech.speak("Flash is already on", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                            break;
                        }
                        flash_on = true;
                        textToSpeech.speak("Flash turned on", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                        cameraProviderFuture.addListener(() -> {
                            try {
                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                                flashOn(cameraProvider);


                            } catch (ExecutionException | InterruptedException e) {
                                // No errors need to be handled for this Future.
                                // This should never be reached.
                            }
                        }, ContextCompat.getMainExecutor(this));
                    }

                    String flashOff = "flash off";
                    if (audioInput.toLowerCase().indexOf(flashOff.toLowerCase()) > -1) {
                        if (flash_off) {
                            textToSpeech.speak("Flash is already off", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                            break;
                        }
                        flash_off = true;
                        textToSpeech.speak("Flash turned off", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);

                        cameraProviderFuture.addListener(() -> {
                            try {
                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                                flashOff(cameraProvider);


                            } catch (ExecutionException | InterruptedException e) {
                                // No errors need to be handled for this Future.
                                // This should never be reached.
                            }
                        }, ContextCompat.getMainExecutor(this));
                    }

                    String offFlash = "off flash";
                    if (audioInput.toLowerCase().indexOf(offFlash.toLowerCase()) > -1) {
                        if (flash_off) {
                            textToSpeech.speak("Flash is already off", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                            break;
                        }
                        flash_off = true;
                        textToSpeech.speak("Flash turned off", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                        cameraProviderFuture.addListener(() -> {
                            try {
                                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                                flashOff(cameraProvider);
                            } catch (ExecutionException | InterruptedException e) {
                                // No errors need to be handled for this Future.
                                // This should never be reached.
                            }
                        }, ContextCompat.getMainExecutor(this));
                    }

                }
                break;
        }
    }
}