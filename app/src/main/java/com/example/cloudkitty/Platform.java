package com.example.cloudkitty;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Platform {
    public static final int NORMAL = 0, MOVING = 1, SPRING = 2, BREAKABLE = 3, KILLER = 4;

    private Bitmap bitmap;
    private float x, y;
    private int type;
    private int width, height;
    private boolean visible = true;
    private float direction = 1;
    private boolean passed = false;

    private long destroyedAt = 0;
    public static final long RESPAWN_TIME = 10000;

    public Platform(Context context, float x, float y, int width, int height, int type, boolean isGround){
        this.x = x; this.y = y; this.width = width; this.height = height; this.type = type;

        int resId;
        switch(type){
            case MOVING: resId = R.drawable.cloud_moving; break;
            case SPRING: resId = R.drawable.cloud_spring; break;
            case BREAKABLE: resId = R.drawable.cloud_breakable; break;
            case KILLER: resId = R.drawable.cloud_kill; break;
            default: resId = R.drawable.cloud; break;
        }

        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resId);
        this.bitmap = Bitmap.createScaledBitmap(original, width, height, false);
    }

    public void update(int screenWidth){
        if(type == MOVING && visible){
            x += 5*direction;
            if(x < 0 || x + width > screenWidth) direction *= -1;
        }

        if(!visible && type == BREAKABLE){
            if(System.currentTimeMillis() - destroyedAt >= RESPAWN_TIME) visible = true;
        }
    }

    public void draw(Canvas canvas){
        if(visible) canvas.drawBitmap(bitmap, x, y, null);
    }

    public Rect getRect(){ return new Rect((int)x,(int)y,(int)x+width,(int)y+height); }

    public void destroy(){ visible = false; destroyedAt = System.currentTimeMillis(); }

    public boolean isVisible(){ return visible; }
    public float getY(){ return y; }
    public void setY(float y){ this.y = y; }
    public int getType(){ return type; }
    public boolean isPassed(){ return passed; }
    public void setPassed(boolean passed){ this.passed = passed; }
}
