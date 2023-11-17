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

//THIS IS THE OBJECT DETECTION ACTIVITY WHICH IS REFERRED AS HOMEACTIVITY BECAUSE OBJECT DETECTION
//IS THE COMPONENT WHICH WILL BE RUNNING FIRST BY DEFAULT.
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
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import android.os.Handler;
import android.speech.tts.TextToSpeech;

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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
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
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

public class homeactivity extends AppCompatActivity {
    Preview preview = new Preview.Builder()                                                         //Here we initialize camera preview object
            .build();

    CameraSelector cameraSelector = new CameraSelector.Builder()                                    //Here we initialize camera selector object which will help
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)                                     // us in opening camera with its lens facing backward
            .build();

    ImageAnalysis imageAnalysis =                                                                   //Here we initialize image analysis object with a target
            new ImageAnalysis.Builder()                                                             //resolution as 1280 x 720 and back pressure strategy as
                    .setTargetResolution(new Size(1280, 720))                          //KEEP_ONLY_LATEST so that it won't flood our model with
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)               //large number of frame which will in turn result in lag.
                    .build();


    private class YourAnalyzer implements ImageAnalysis.Analyzer {                                  //Here we created a class for our analyser implementing image
                                                                                                    //analyser
        @Override
        public void analyze(ImageProxy imageProxy) {
            @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image =                                                                  //Here we created input image which we contain image frame,
                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());//image info and its rotational degrees.

                LocalModel localModel =                                                             //Here we create a custom local model is created using
                        new LocalModel.Builder()                                                    //pre-trained tflite model which has been trained on cocomo
                                .setAssetFilePath("model.tflite")                                   //training set.
                                .build();

                CustomObjectDetectorOptions customObjectDetectorOptions =                           //Here we created a our customObjectDetectorOptions in which
                        new CustomObjectDetectorOptions.Builder(localModel)                         //we pass our machine learning model, set detector mode in
                                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)           //STREAM_MODE as we are detecting object in real-time, enable
                                .enableClassification()                                             //classification, set threshold confidence at 80% any
                                .setClassificationConfidenceThreshold(0.8f)                         //confidence % below 80 would not be taken into account
                                .setMaxPerObjectLabelCount(1)                                       //and at last set max per object label count as 1.
                                .build();

                ObjectDetector objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
                objectDetector                                                                      //Here we created our final object detector and then add
                        .process(image)                                                             //respective listener to them.
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {                           //Here onfailure the imageproxy is closed resulting in
                                imageProxy.close();                                                 //termination of the application.
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                            @Override
                            public void onSuccess(List<DetectedObject> results) {                   //Here on success the labels and its confidence % are stored
                                for (DetectedObject detectedObject : results) {                     //in labelText(string) and confidence(float) respectively.
                                    for (DetectedObject.Label label : detectedObject.getLabels()) { //After that we create a vibrator object which vibrates the
                                        String labelText = label.getText();                         //phone after successful detection of object
                                        float confidence = label.getConfidence();                   //At last the label are passes in tts as a audio response

                                        labels.setText(labelText + (float) confidence);
                                        if (labelText.toLowerCase().indexOf(toFind.toLowerCase()) > -1 && toFind != "Explore") {//to user
                                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                            vibrator.vibrate(100);
                                            textToSpeech.speak(labelText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                                            try {
                                                sleep(400);                                   //Here sleep is added to halt the for loop for 0.4s for
                                            } catch (InterruptedException e) {                      //uninterrupted audio response.
                                                e.printStackTrace();
                                            }
                                        }

                                        if (toFind == "Explore") {//to user
                                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                            vibrator.vibrate(100);
                                            textToSpeech.speak(labelText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                                            try {
                                                sleep(400);                                   //Here sleep is added to halt the for loop for 0.4s for
                                            } catch (InterruptedException e) {                      //uninterrupted audio response.
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                imageProxy.close();                                                 //The above process is repeated as many times as camera frame
                            }                                                                       //changes and atlast we close image proxy after work is done.
                        });

            }
        }
    }

    TextView labels, confidences;
    TextToSpeech textToSpeech;
    Button textDetection;
    boolean flash_on = false, flash_off = false;
    String toFind = "";

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeactivity);
        getSupportActionBar().hide();                                                               //Hiding action bar
        previewView = findViewById(R.id.previewView);                                               //Here we connect all our containers/components of UI with
        labels = findViewById(R.id.answer);                                                         //backend
        confidences = findViewById(R.id.answer1);
        textDetection = findViewById(R.id.text_button);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {//Here we initialize the object for Text-To-Speech
            @Override
            public void onInit(int status) {
                if (status != android.speech.tts.TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);                                            //Setting language as Uk
                }
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {                                                                     //welcome audio when activity starts
                textToSpeech.speak("Looking for object please give us a second", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, 1000);


        textDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                        //On click listener to navigate to text detection activity
                Intent intent = new Intent(homeactivity.this, Text_Detection.class);
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

    void flashOff(@NonNull ProcessCameraProvider cameraProvider) {                                  //This function will be invoked  whenever flash needs to be
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

                    String pillow = "pillow";
                    if (audioInput.toLowerCase().indexOf(pillow.toLowerCase()) > -1) {
                        textToSpeech.speak("Looking for pillow", TextToSpeech.QUEUE_FLUSH, null, null);
                        toFind = pillow;
                    }

                    String door = "door";
                    if (audioInput.toLowerCase().indexOf(door.toLowerCase()) > -1) {
                        textToSpeech.speak("Looking for door", TextToSpeech.QUEUE_FLUSH, null, null);
                        toFind = door;
                    }

                    String bags = "luggage and bags";
                    if (audioInput.toLowerCase().indexOf(bags.toLowerCase()) > -1) {
                        textToSpeech.speak("Looking for bags", TextToSpeech.QUEUE_FLUSH, null, null);
                        toFind = bags;
                    }

                    String headphones = "headphone";
                    if (audioInput.toLowerCase().indexOf(headphones.toLowerCase()) > -1) {
                        textToSpeech.speak("Looking for headphone", TextToSpeech.QUEUE_FLUSH, null, null);
                        toFind = headphones;
                    }

                    String explore = "Explore";
                    if (audioInput.toLowerCase().indexOf(explore.toLowerCase()) > -1) {
                        textToSpeech.speak("Exploring the surrounding", TextToSpeech.QUEUE_FLUSH, null, null);
                        toFind = explore;
                    }
                }
                break;
        }
    }
}