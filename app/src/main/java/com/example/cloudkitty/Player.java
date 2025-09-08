package com.example.cloudkitty;

import android.content.Context;
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

    public Player(Context context, float x, float y) {
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat);
        this.x = x;
        this.y = y;
        this.velocityY = 0;
    }

    public void update(ArrayList<Platform> platforms, int screenHeight, int screenWidth) {
        velocityY += 1.5f;
        y += velocityY;

        if (movingLeft) x -= 15;
        if (movingRight) x += 15;

        if (x < 0) x = 0;
        if (x > screenWidth - bitmap.getWidth()) x = screenWidth - bitmap.getWidth();

        for (Platform p : platforms) {
            if (isLandingOn(p)) {
                velocityY = -57;
                y = p.getY() - bitmap.getHeight();

                if (p.getType() == Platform.SPRING) velocityY = -100;
                if (p.getType() == Platform.BREAKABLE) p.destroy();

                break;
            }
        }

        if (y > screenHeight - bitmap.getHeight()) {
            y = screenHeight - bitmap.getHeight();
            velocityY = 0;
        }
    }

    public boolean isLandingOn(Platform p) {
        Rect playerRect = getRect();
        Rect platRect = p.getRect();
        boolean falling = velocityY > 0;
        boolean horizontallyOverlaps = playerRect.right > platRect.left && playerRect.left < platRect.right;
        boolean verticallyTouching = playerRect.bottom >= platRect.top && playerRect.bottom - velocityY < platRect.top;
        return falling && horizontallyOverlaps && verticallyTouching && p.isVisible();
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    private Rect getRect() {
        return new Rect((int)x, (int)y, (int)x + bitmap.getWidth(), (int)y + bitmap.getHeight());
    }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
}
