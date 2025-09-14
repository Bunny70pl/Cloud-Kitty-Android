package com.example.cloudkitty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
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
    private int score = 0;

    private boolean isGameOver = false;

    private Platform killerCloud;
    private long lastKillerSpawn = 0;
    private static final long KILLER_SPAWN_INTERVAL = 11000; // co 11 sek.

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        platforms = new ArrayList<>();
        random = new Random();

        screenHeight = 1920;
        screenWidth = 1080;

        initGameObjects();
        setFocusable(true);
    }

    private void initGameObjects() {
        // Gracz startuje tuż nad ziemią
        player = new Player(getContext(), screenWidth / 2f, screenHeight - 220);
        player.setVelocityY(0);

        // Platformy
        platforms.clear();
        int baseY = screenHeight - 120;
        int spacing = 250;

        for (int i = 1; i < 7; i++) {
            int type = random.nextInt(4);
            float x = random.nextInt(screenWidth - 200);
            float y = baseY - i * spacing;
            platforms.add(new Platform(getContext(), x, y, 250, 150, type, false));
        }

        // Ziemia
        platforms.add(new Platform(getContext(), 0, screenHeight - 120, screenWidth, 150, 0, true));

        // Zabójcza chmura
        spawnKillerCloud();
    }

    private void spawnKillerCloud() {
        float x = random.nextInt(screenWidth - 200);
        float minPlatformY = Float.MAX_VALUE;
        for (Platform p : platforms) {
            if (p.getY() < minPlatformY) minPlatformY = p.getY();
        }
        float y = minPlatformY - (random.nextInt(200) + 200);
        killerCloud = new Platform(getContext(), x, y, 250, 150, Platform.KILLER, false);
        lastKillerSpawn = System.currentTimeMillis();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenHeight = getHeight();
        screenWidth = getWidth();
        score = 0;
        isGameOver = false;
        initGameObjects();
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
            try { thread.join(); retry = false; }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.rgb(138, 235, 241));

        for (Platform p : platforms) p.draw(canvas);
        if (killerCloud != null) killerCloud.draw(canvas);
        player.draw(canvas);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(64);
        textPaint.setAntiAlias(true);
        canvas.drawText("Score: " + score, 50, 100, textPaint);
        canvas.drawText("High Score: " + getHighScore(), 50, 200, textPaint);

        if (isGameOver) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(120);
            paint.setAntiAlias(true);
            canvas.drawText("GAME OVER", screenWidth / 4f, screenHeight / 2f, paint);

            Paint small = new Paint();
            small.setColor(Color.BLACK);
            small.setTextSize(64);
            canvas.drawText("Tap LEFT = Restart", screenWidth / 6f, screenHeight / 2f + 150, small);
            canvas.drawText("Tap RIGHT = Menu", screenWidth / 6f, screenHeight / 2f + 250, small);
        }
    }

    public void update() {
        if (isGameOver) return;

        player.update(platforms, screenHeight, screenWidth);

        float upperThreshold = screenHeight / 3f;
        float lowerThreshold = screenHeight * 2f / 3f;
        float diff;

        for (Platform p : platforms) p.update(screenWidth);

        if (player.getY() < upperThreshold) {
            diff = upperThreshold - player.getY();
            player.setY(upperThreshold);
            for (Platform p : platforms) p.setY(p.getY() + diff);
            if (killerCloud != null) killerCloud.setY(killerCloud.getY() + diff);
        } else if (player.getY() > lowerThreshold) {
            diff = player.getY() - lowerThreshold;
            player.setY(lowerThreshold);
            for (Platform p : platforms) p.setY(p.getY() - diff);
            if (killerCloud != null) killerCloud.setY(killerCloud.getY() - diff);
        }

        // Generowanie nowych platform w górę
        float minY = Float.MAX_VALUE;
        for (Platform p : platforms) if (p.getY() < minY) minY = p.getY();
        while (minY > -500) {
            int type = random.nextInt(4);
            float newX = random.nextInt(screenWidth - 200);
            float newY = minY - (random.nextInt(700) + 300);
            platforms.add(new Platform(getContext(), newX, newY, 250, 150, type, false));
            minY = newY;
        }

        // Punktacja
        for (Platform p : platforms) {
            if (!p.isPassed() && player.getY() + 100 < p.getY()) {
                score += 10;
                p.setPassed(true);
            }
        }

        // Kolizja z platformami
        boolean onPlatform = false;
        for (Platform p : platforms) {
            if (player.isLandingOn(p)) {
                player.setY(p.getY() - getPlayerHeight());
                player.setVelocityY(0);
                onPlatform = true;
                break;
            }
        }

        float groundY = screenHeight - 120 - getPlayerHeight();
        if (!onPlatform && player.getY() >= groundY) {
            player.setY(groundY);
            player.setVelocityY(0);
        }

        // Kolizja z zabójczą chmurą
        if (killerCloud != null && player.isLandingOn(killerCloud)) {
            isGameOver = true;
        }

        saveHighScore();

        if (System.currentTimeMillis() - lastKillerSpawn > KILLER_SPAWN_INTERVAL) {
            spawnKillerCloud();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xTouch = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (isGameOver) {
                    if (xTouch < screenWidth / 2f) {
                        // reset gry
                        restartGame();
                        return true;
                    } else {
                        // menu
                        Context context = getContext();
                        Intent intent = new Intent(context, MenuActivity.class);
                        context.startActivity(intent);
                        if (context instanceof Activity) ((Activity) context).finish();
                        return true;
                    }
                }

                if (xTouch < screenWidth / 2f) player.setMovingLeft(true);
                else player.setMovingRight(true);
                break;

            case MotionEvent.ACTION_UP:
                player.setMovingLeft(false);
                player.setMovingRight(false);
                break;
        }
        return true;
    }

    public void movePlayerLeft(boolean moving) { player.setMovingLeft(moving); }
    public void movePlayerRight(boolean moving) { player.setMovingRight(moving); }

    private void saveHighScore() {
        int highScore = getHighScore();
        if (score > highScore) {
            getContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("high_score", score)
                    .apply();
        }
    }

    private int getHighScore() {
        return getContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .getInt("high_score", 0);
    }

    private int getPlayerHeight() { return 100; }

    public boolean isGameOver() { return isGameOver; }

    public void restartGame() {
        score = 0;
        isGameOver = false;
        initGameObjects();
    }
}
