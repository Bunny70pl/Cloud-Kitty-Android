package com.example.cloudkitty;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Player player;
    private ArrayList<Platform> platforms;
    private Random random;
    private int screenHeight, screenWidth;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        player = new Player(context, 500, 1000);
        platforms = new ArrayList<>();
        random = new Random();

        screenHeight = 1920;
        screenWidth = 1080;

        for (int i = 1; i < 7; i++) {
            platforms.add(new Platform(context, random.nextInt(900), 1800 - i * 400));
        }
        platforms.add(new Platform(context, 0, screenHeight - 120, screenWidth, 120));
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenHeight = getHeight();
        screenWidth = getWidth();
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.rgb(135, 206, 250));

        for (Platform p : platforms) {
            p.draw(canvas);
        }

        player.draw(canvas);

      }

    public void update() {
        player.update(platforms, screenHeight, screenWidth);

        float upperThreshold = screenHeight / 3f;
        float lowerThreshold = screenHeight * 2f / 3f;

        if (player.getY() < upperThreshold) {
            float diff = upperThreshold - player.getY();
            player.setY(upperThreshold);

            for (Platform p : platforms) {
                p.setY(p.getY() + diff);
            }

            float minY = Float.MAX_VALUE;
            for (Platform p : platforms) {
                if (p.getY() < minY) minY = p.getY();
            }

            while (minY > -500) {
                float newX = random.nextInt(screenWidth - 300);
                float newY = minY - 500;//wiekszy odstep
                platforms.add(new Platform(getContext(), newX, newY));
                minY = newY;
            }
        } else if (player.getY() > lowerThreshold) {
            float diff = player.getY() - lowerThreshold;
            player.setY(lowerThreshold);

            for (Platform p : platforms) {
                p.setY(p.getY() - diff);
            }
        }
    }

    // publiczne metody do sterowania
    public void movePlayerLeft(boolean moving) {
        player.setMovingLeft(moving);
    }

    public void movePlayerRight(boolean moving) {
        player.setMovingRight(moving);
    }
}
