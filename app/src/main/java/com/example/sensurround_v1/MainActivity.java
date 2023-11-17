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


//THIS IS SPLASH SCREEN ACTIVITY WHICH WILL BE DISPLAYED FOR 4 SECONDS ALONG WITH AUDIO WHICH WILL
//BE PLAYED WTIH AFTER A DELAY OF 1 SECOND
package com.example.sensurround_v1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    TextToSpeech textToSpeech;
    String welcomeText="Welcome to sensurround, A different way of life";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();                                                                //Hiding our action bar as it does'nt look good for app having
                                                                                                     // camera functionality.

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {                                                                      //Here we shift from our splash screen to home activity(Object )
                Intent intent = new Intent(MainActivity.this, homeactivity.class);     //Detection) After a delay of 4s
                startActivity(intent);
                finish();
            }
        }, 4000);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() { //Here we initialize the object for Text-To-Speech
            @Override
            public void onInit(int status) {
                if (status != android.speech.tts.TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);                                             //Setting language as Uk
                }
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {                                                                      //Audio Response from app 1s after the activity starts
                textToSpeech.speak(welcomeText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, 1000);
    }
}