package com.example.cloudkitty;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;

public class Player {
    private Bitmap bitmap;
    private float x, y, velocityY;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean facingRight = false; // kierunek kota

    public Player(Context context, float x, float y){
        SharedPreferences prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        String skin = prefs.getString("selected_skin","cat_red");

        int resId;
        switch(skin){
            case "cat_white": resId = R.drawable.cat_white; break;
            case "cat_black": resId = R.drawable.cat_black; break;
            default: resId = R.drawable.cat_red; break;
        }

        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resId);
        this.bitmap = Bitmap.createScaledBitmap(original, 300, 250, false);
        this.x = x;
        this.y = y;
        this.velocityY = 0;
    }

    public void update(ArrayList<Platform> platforms, int screenHeight, int screenWidth){
        // grawitacja
        velocityY += 1.5f;
        y += velocityY;

        // ruch w poziomie + kierunek kota
        if(movingLeft){
            x -= 15;
            facingRight = false;
        }
        if(movingRight){
            x += 15;
            facingRight = true;
        }

        // ograniczenia ekranu
        if(x < 0) x = 0;
        if(x > screenWidth - bitmap.getWidth()) x = screenWidth - bitmap.getWidth();

        boolean landed = false;
        for(Platform p : platforms){
            if(isLandingOn(p)){
                velocityY = -57;
                y = p.getY() - bitmap.getHeight();
                landed = true;

                if(p.getType() == Platform.SPRING) velocityY = -100;
                if(p.getType() == Platform.BREAKABLE) p.destroy();
                break;
            }
        }

        float groundY = screenHeight - 120 - bitmap.getHeight();
        if(!landed && y > groundY){
            y = groundY;
            velocityY = 0;
        }
    }

    private Rect getRect(){
        int paddingX = (int)(bitmap.getWidth() * 0.1f);   // 10% szerokości
        int paddingTop = (int)(bitmap.getHeight() * 0.05f);
        int paddingBottom = (int)(bitmap.getHeight() * 0.1f);

        return new Rect(
                (int)x + paddingX,
                (int)y + paddingTop,
                (int)x + bitmap.getWidth() - paddingX,
                (int)y + bitmap.getHeight() - paddingBottom
        );
    }

    public boolean isLandingOn(Platform p){
        Rect playerRect = getRect();
        Rect platRect = p.getRect();

        boolean falling = velocityY > 0;

        // hitbox gracza przesunięty o velocityY
        Rect nextRect = new Rect(playerRect);
        nextRect.offset(0, (int)velocityY);

        boolean horizontal = nextRect.right > platRect.left && nextRect.left < platRect.right;
        boolean vertical = playerRect.bottom <= platRect.top && nextRect.bottom >= platRect.top;

        return falling && horizontal && vertical && p.isVisible();
    }

    public void draw(Canvas canvas){
        if(facingRight){
            canvas.drawBitmap(bitmap, x, y, null);
        } else {
            canvas.save();
            canvas.scale(-1, 1, x + bitmap.getWidth()/2f, y + bitmap.getHeight()/2f);
            canvas.drawBitmap(bitmap, x, y, null);
            canvas.restore();
        }
    }

    public void setMovingLeft(boolean moving){ this.movingLeft = moving; }
    public void setMovingRight(boolean moving){ this.movingRight = moving; }

    public float getY(){ return y; }
    public float getX(){ return x; }
    public void setY(float y){ this.y = y; }
    public void setVelocityY(float velocityY){ this.velocityY = velocityY; }
}
