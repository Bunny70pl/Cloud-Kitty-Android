package com.example.cloudkitty;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Platform {
    private Bitmap bitmap;
    private float x, y;

    public Platform(Context context, float x, float y) {
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud);
        this.x = x;
        this.y = y;
    }
    public Platform(Context context, float x, float y, int width, int height) {
        this.x = x;
        this.y = y;

        // Tworzymy prostokątną bitmapę na podaną szerokość i wysokość
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud);
        this.bitmap = Bitmap.createScaledBitmap(original, width, height, false);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    public Rect getRect() {
        return new Rect((int)x, (int)y, (int)x + bitmap.getWidth(), (int)y + bitmap.getHeight());
    }

    public float getY() {
        return y;
    }

    public void setY(float newY) {
        this.y = newY;
    }

    public float getX() {
        return x;
    }
}
