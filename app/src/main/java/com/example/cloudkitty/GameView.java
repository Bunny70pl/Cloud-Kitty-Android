package com.example.cloudkitty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.ConsoleHandler;

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
    private static final long KILLER_SPAWN_INTERVAL = 11000;

    // hamburger menu
    private Rect menuButtonRect;
    private boolean menuOpen = false;
    private Paint menuPaint;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        platforms = new ArrayList<>();
        random = new Random();
        setFocusable(true);
    }

    private void initGameObjects() {
        screenHeight = getHeight();
        screenWidth = getWidth();
        player = new Player(getContext(), screenWidth / 2f, screenHeight - 220);

        platforms.clear();
        int baseY = screenHeight - 120;
        int spacing = 300; // większy odstęp początkowych platform

        for (int i = 1; i < 7; i++) {
            int type = random.nextInt(4);
            float x = random.nextInt(screenWidth - 200);
            float y = baseY - i * spacing;
            platforms.add(new Platform(getContext(), x, y, 250, 150, type, false));
        }

        platforms.add(new Platform(getContext(), 0, screenHeight - 120, screenWidth, 150, Platform.NORMAL, true));
        spawnKillerCloud();
    }

    private void spawnKillerCloud() {
        float x = random.nextInt(screenWidth - 200);
        float minPlatformY = Float.MAX_VALUE;
        for (Platform p : platforms) if (p.getY() < minPlatformY) minPlatformY = p.getY();
        float y = minPlatformY - (random.nextInt(200) + 200);
        killerCloud = new Platform(getContext(), x, y, 250, 150, Platform.KILLER, false);
        lastKillerSpawn = System.currentTimeMillis();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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

        // tło
        canvas.drawColor(Color.rgb(138, 235, 241));

        // rysowanie platform
        for (Platform p : platforms) p.draw(canvas);

        // killer cloud
        if (killerCloud != null) killerCloud.draw(canvas);

        // gracz
        player.draw(canvas);

        // tekst wyniku
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(64);
        textPaint.setAntiAlias(true);
        canvas.drawText("Score: " + score, 50, 100, textPaint);
        canvas.drawText("High Score: " + getHighScore(), 50, 200, textPaint);

        // hamburger menu
        if(menuPaint == null){
            menuPaint = new Paint();
            menuPaint.setColor(Color.BLACK);
            menuPaint.setStrokeWidth(10);
        }
        int left = screenWidth - 150;
        int top = 50;
        int width = 100;
        int height = 60;
        menuButtonRect = new Rect(left, top, left + width, top + height);
        canvas.drawLine(left, top, left + width, top, menuPaint);
        canvas.drawLine(left, top + 20, left + width, top + 20, menuPaint);
        canvas.drawLine(left, top + 40, left + width, top + 40, menuPaint);

        // menu opcje
        if(menuOpen){
            Paint bg = new Paint();
            bg.setColor(Color.WHITE);
            bg.setAlpha(230);
            canvas.drawRect(screenWidth/4f, screenHeight/4f, screenWidth*3/4f, screenHeight*3/4f, bg);

            Paint option = new Paint();
            option.setColor(Color.BLACK);
            option.setTextSize(64);
            canvas.drawText("Wybierz skina", screenWidth/4f + 50, screenHeight/4f + 100, option);
            canvas.drawText("Wróć do gry", screenWidth/4f + 50, screenHeight/4f + 200, option);
            canvas.drawText("Wyjdź", screenWidth/4f + 50, screenHeight/4f + 300, option);
        }

        // Game Over - wyśrodkowane
        if(isGameOver){
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(120);
            paint.setAntiAlias(true);

            String text = "GAME OVER";
            float textWidth = paint.measureText(text);
            float x = (screenWidth - textWidth) / 2f;

            Paint.FontMetrics fm = paint.getFontMetrics();
            float y = (screenHeight - (fm.ascent + fm.descent)) / 2f;

            canvas.drawText(text, x, y, paint);
        }
    }


    public void update() {
        if (isGameOver || menuOpen) return;

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

        float minY = Float.MAX_VALUE;
        for (Platform p : platforms) if (p.getY() < minY) minY = p.getY();
        while(minY > -500){
            int type = random.nextInt(4);
            float newX = random.nextInt(screenWidth - 200);
            float newY = minY - (random.nextInt(700) + 300);
            platforms.add(new Platform(getContext(), newX, newY, 250, 150, type, false));
            minY = newY;
        }

        for(Platform p : platforms){
            if(!p.isPassed() && player.getY() + 100 < p.getY()){
                score += 10;
                p.setPassed(true);
            }
        }

        boolean onPlatform = false;
        for(Platform p : platforms){
            if(player.isLandingOn(p)){
                player.setY(p.getY() - 100);
                player.setVelocityY(0);
                onPlatform = true;
                break;
            }
        }

        float groundY = screenHeight - 120 - 100;
        if(!onPlatform && player.getY() >= groundY){
            player.setY(groundY);
            player.setVelocityY(0);
        }

        if(killerCloud != null && player.isLandingOn(killerCloud)) isGameOver = true;
        saveHighScore();

        if(System.currentTimeMillis() - lastKillerSpawn > KILLER_SPAWN_INTERVAL){
            spawnKillerCloud();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                // jeśli game over
                if(isGameOver){
                    if(x < screenWidth / 2f){
                        // lewa połowa ekranu -> restart gry
                        restartGame();
                    } else {
                        // prawa połowa ekranu -> otwórz menu
                        menuOpen = true;
                    }
                    return true;
                }

                // obsługa hamburger menu
                if(menuButtonRect.contains((int)x, (int)y)){
                    menuOpen = !menuOpen;
                    return true;
                }

                if(menuOpen){
                    if(y > screenHeight/4f + 50 && y < screenHeight/4f + 150){
                        Context context = getContext();
                        Intent intent = new Intent(context, SkinActivity.class);
                        context.startActivity(intent);
                        if(context instanceof Activity) ((Activity)context).finish();
                    } else if(y > screenHeight/4f + 150 && y < screenHeight/4f + 250){
                        menuOpen = false;
                    } else if(y > screenHeight/4f + 250 && y < screenHeight/4f + 350){
                        if(getContext() instanceof Activity) ((Activity)getContext()).finish();
                    }
                    return true;
                }

                // sterowanie lewo/prawo
                if(x < screenWidth/2f){
                    movePlayerLeft(true);
                    movePlayerRight(false);
                } else {
                    movePlayerRight(true);
                    movePlayerLeft(false);
                }
                break;

            case MotionEvent.ACTION_UP:
                movePlayerLeft(false);
                movePlayerRight(false);
                break;
        }
        return true;
    }


    public void movePlayerLeft(boolean moving){ player.setMovingLeft(moving); }
    public void movePlayerRight(boolean moving){ player.setMovingRight(moving); }

    private void saveHighScore(){
        int highScore = getHighScore();
        if(score > highScore){
            getContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("high_score", score)
                    .apply();
        }
    }

    private int getHighScore(){
        return getContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .getInt("high_score", 0);
    }

    public void restartGame(){
        score = 0;
        isGameOver = false;
        initGameObjects();
    }

    public void reloadPlayerSkin(){
        if(player != null){
            float px = player.getX();
            float py = player.getY();
            player = new Player(getContext(), px, py);
        }
    }

    public boolean isGameOver(){ return isGameOver; }
}
