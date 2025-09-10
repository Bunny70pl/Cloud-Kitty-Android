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

    private float worldOffset = 0;       // aktualna wysokość
    private float maxWorldOffset = 0;    // najwyższa osiągnięta wysokość
    private float checkpointOffset = 0;  // ostatni checkpoint (przy spadku)
    private int score = 0;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        player = new Player(context, 500, 1000);
        platforms = new ArrayList<>();
        random = new Random();

        screenHeight = 1920;
        screenWidth = 1080;

        // Początkowe platformy
        for (int i = 1; i < 7; i++) {
            int type = random.nextInt(4); // NORMAL, MOVING, SPRING, BREAKABLE
            platforms.add(new Platform(context,
                    random.nextInt(screenWidth - 200),
                    1800 - i * 400,
                    200, 60, type, false));
        }

        // Stała ziemia
        platforms.add(new Platform(context, 0, screenHeight - 120, screenWidth, 120));
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenHeight = getHeight();
        screenWidth = getWidth();
        worldOffset = 0;
        maxWorldOffset = 0;
        checkpointOffset = 0;
        score = 0;
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

        for (Platform p : platforms) p.draw(canvas);
        player.draw(canvas);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(64);
        textPaint.setAntiAlias(true);
        canvas.drawText("Score: " + score, 50, 100, textPaint);
    }

    public void update() {
        player.update(platforms, screenHeight, screenWidth);

        float upperThreshold = screenHeight / 3f;
        float lowerThreshold = screenHeight * 2f / 3f;
        float diff;

        // Aktualizacja platform
        for (Platform p : platforms) p.update(screenWidth);

        if (player.getY() < upperThreshold) {
            diff = upperThreshold - player.getY();
            player.setY(upperThreshold);

            // Przesuwamy tylko platformy, które nie są ziemią
            for (Platform p : platforms) {
                if (!p.isGround()) {
                    p.setY(p.getY() + diff);
                }
            }

            // Dodajemy do worldOffset tylko przy wznoszeniu się
            worldOffset += diff;

            // Aktualizacja platform w górę
            float minY = Float.MAX_VALUE;
            for (Platform p : platforms) {
                if (!p.isGround() && p.getY() < minY) minY = p.getY();
            }

            while (minY > -500) {
                int type = random.nextInt(4);
                float newX = random.nextInt(screenWidth - 200);
                float newY = minY - (random.nextInt(700) + 300);
                platforms.add(new Platform(getContext(), newX, newY, 100, 60, type, false));
                minY = newY;
            }

            // --- NOWA LOGIKA PUNKTÓW ---
            // Dodajemy punkty tylko jeśli idziemy wyżej niż maxWorldOffset
            if (worldOffset > maxWorldOffset) {
                score += (int) ((worldOffset - maxWorldOffset) / 10); // tylko przy wznoszeniu
                maxWorldOffset = worldOffset;
            }

        } else if (player.getY() > lowerThreshold) {
            diff = player.getY() - lowerThreshold;
            player.setY(lowerThreshold);

            for (Platform p : platforms) {
                if (!p.isGround()) {
                    p.setY(p.getY() - diff);
                }
            }

            // Przy spadku nie zmieniamy punktów ani maxWorldOffset
        }
    }



    public void movePlayerLeft(boolean moving) { player.setMovingLeft(moving); }
    public void movePlayerRight(boolean moving) { player.setMovingRight(moving); }
}
