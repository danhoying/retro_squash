package com.example.dan.retrosquash;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Display;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Sets DayNight theme to always Night Mode
    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }

    Canvas canvas;
    SquashCourtView squashCourtView;

    // Sound variables
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    // Get display details
    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    // Game objects
    int racketWidth;
    int racketHeight;
    Point racketPosition;
    int ballWidth;
    Point ballPosition;

    // For ball and racket movement
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;
    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    // Stats
    long lastFrameTime;
    int fps;
    int score;
    int lives;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);

        // Sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // Create objects for 2 required classes
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            //Create FX in memory ready to use
            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);
        } catch (IOException e) {
            // Catch exceptions
        }
    }
}
