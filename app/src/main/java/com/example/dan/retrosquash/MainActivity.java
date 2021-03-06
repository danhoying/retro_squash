package com.example.dan.retrosquash;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Sets DayNight theme to always Night Mode
    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
    }

    private static final String TAG = "MainActivity";

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
    Point blockPosition;

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

        // Get screen size in pixels
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        // Set size of game objects
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 200;
        racketWidth = screenWidth / 8;
        racketHeight = 10;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;

        lives = 3;

        Block.setNumBlocks(32);
        int blockWidth = screenWidth / 8;
        int blockHeight = screenHeight / 16;
        blockPosition = new Point(blockWidth / 2, (blockHeight / 2) + 100);

        Block.blockList = new ArrayList<>();
        Block.setBlockList(Block.blockList, Block.getNumBlocks(),
                blockWidth, blockHeight, blockPosition);

        Block block = Block.getBlockList().get(0);
        Block block2 = Block.getBlockList().get(1);
        Log.i(TAG, "onCreate: " + block.position);
        Log.i(TAG, "onCreate: " + block2.position);
    }

    class SquashCourtView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            ourHolder = getHolder();
            ballIsMovingDown = true;

            // Send the ball in random direction
            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);
            switch (ballDirection) {
                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1:
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                    break;
                case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
            }
        }

        @Override
        public void run() {
            while (playingSquash) {
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }

        public void updateCourt() {
            if (racketIsMovingRight) {
                if (racketPosition.x + (racketWidth / 2) > screenWidth) {
                    racketIsMovingRight = false;
                } else {
                    racketPosition.x = racketPosition.x + 20;
                }
            }
            if (racketIsMovingLeft) {
                if (racketPosition.x - (racketWidth / 2) < 0) {
                    racketIsMovingLeft = false;
                } else {
                    racketPosition.x = racketPosition.x - 20;
                }
            }
            // Detect collisions
            // Hit right of screen
            if (ballPosition.x + ballWidth > screenWidth) {
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            // Hit left of screen
            if (ballPosition.x < 0) {
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }

            // Edge of ball has hit bottom of screen
            if (ballPosition.y > screenHeight - ballWidth) {
                lives = lives - 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4, 1, 1, 0, 0, 1);
                }
                ballPosition.y = 1 + ballWidth; // Back to top of screen

                // Set horizontal direction for next falling ball
                Random randomNumber = new Random();
                int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;
                ballPosition.x = startX + ballWidth;
                int ballDirection = randomNumber.nextInt(3);

                switch (ballDirection) {
                    case 0:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;
                    case 1:
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                        break;
                    case 2:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = false;
                        break;
                }
            }
            // Ball hits top of screen
            if (ballPosition.y <= 0) {
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
                soundPool.play(sample2, 1, 1, 0, 0, 1);
            }
            // Adjust x position based on ball moving up or down
            if (ballIsMovingDown) {
                ballPosition.y += 6;
            }
            if (ballIsMovingUp) {
                ballPosition.y -= 10;
            }
            if (ballIsMovingLeft) {
                ballPosition.x -= 12;
            }
            if (ballIsMovingRight) {
                ballPosition.x += 12;
            }

            // Has ball hit racket?
            if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2)) {
                int halfRacket = racketWidth / 2;
                if (ballPosition.x + ballWidth > (racketPosition.x - halfRacket) &&
                        ballPosition.x - ballWidth < (racketPosition.x + halfRacket)) {
                    // Rebound ball vertically and play a sound
                    soundPool.play(sample3, 1, 1, 0, 0, 1);
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;
                    // Determine how to rebound ball horizontally
                    if (ballPosition.x > racketPosition.x) {
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    } else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }
        }

        public void drawCourt() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                paint = new Paint();
                canvas.drawColor(Color.BLACK); // Background color
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(45);
                canvas.drawText("Score:" + score + " Lives:" + lives + " fps:" + fps, 20, 40, paint);

                // Draw the squash racket
                canvas.drawRect(racketPosition.x - (racketWidth / 2),
                        racketPosition.y - (racketHeight / 2), racketPosition.x + (racketWidth / 2),
                        racketPosition.y + racketHeight, paint);

                // Draw the ball
                canvas.drawRect(ballPosition.x, ballPosition.y,
                        ballPosition.x + ballWidth, ballPosition.y + ballWidth, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void controlFPS() {
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            long timeToSleep = 15 - timeThisFrame;
            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }
            if (timeToSleep > 0) {
                try {
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    // Catch exceptions
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }

        public void pause() {
            playingSquash = false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
                // Catch exceptions
            }
        }

        public void resume() {
            playingSquash = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getX() >= screenWidth / 2) {
                        racketIsMovingRight = true;
                        racketIsMovingLeft = false;
                    } else {
                        racketIsMovingLeft = true;
                        racketIsMovingRight = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    racketIsMovingRight = false;
                    racketIsMovingLeft = false;
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        while (true) {
            squashCourtView.pause();
            break;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        squashCourtView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;
    }
}
