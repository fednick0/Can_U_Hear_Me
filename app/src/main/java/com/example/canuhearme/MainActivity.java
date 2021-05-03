package com.example.canuhearme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // API level 8
    // https://developer.android.com/reference/android/speech/SpeechRecognizer

    private TextView tx;
    private FloatingActionButton fab;
    private Vibrator vibrator;

    final static float move = 200;
    float ratio = 45.0f;
    int baseDist;
    float baseRatio;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            checkPermission();

        tx = findViewById(R.id.textView);
        tx.setTextSize(ratio + 15);
        fab = findViewById(R.id.floatingActionButton);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                vibrator.vibrate(VibrationEffect.EFFECT_HEAVY_CLICK);
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                vibrator.vibrate(VibrationEffect.EFFECT_HEAVY_CLICK);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String result = data.get(0);

                tx.setText(result.substring(0, 1).toUpperCase() + result.substring(1));
                vibrator.vibrate(VibrationEffect.EFFECT_HEAVY_CLICK);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    speechRecognizer.stopListening();
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    speechRecognizer.startListening(speechRecognizerIntent);

                return false;
            }
        });

        tx.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 2) {
                    int action = event.getAction();
                    int mainaction = action & MotionEvent.ACTION_MASK;

                    if (mainaction == MotionEvent.ACTION_POINTER_DOWN) {
                        baseDist = getDistance(event);
                        baseRatio = ratio;
                    } else {
                        float scale = (getDistance(event) - baseDist) / move;
                        float factor = (float) Math.pow(2, scale);
                        ratio = Math.min(1024.0f, Math.max(0.1f, baseRatio * factor));

                        tx.setTextSize(ratio + 15);
                    }
                }
                return true;
            }
        });
    }

    private int getDistance(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));

        return (int) (Math.sqrt(dx * dx + dy * dy));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 )
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, getString(R.string.permisos), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("text", tx.getText().toString());
        outState.putFloat("ratio", ratio);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tx.setText(savedInstanceState.getString("text"));
        ratio = savedInstanceState.getFloat("ratio");
        tx.setTextSize(ratio + 15);
    }
}