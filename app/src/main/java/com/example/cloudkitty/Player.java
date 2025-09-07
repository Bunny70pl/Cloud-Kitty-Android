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
        velocityY += 1.5f; // grawitacja
        y += velocityY;

        // Ruch poziomy
        if (movingLeft) x -= 10;
        if (movingRight) x += 10;

        // Granice ekranu
        if (x < 0) x = 0;
        if (x > screenWidth - bitmap.getWidth()) x = screenWidth - bitmap.getWidth();

        // Kolizje z platformami
        for (Platform p : platforms) {
            if (isLandingOn(p)) {
                velocityY = -45; // skok
                y = p.getY() - bitmap.getHeight(); // ustaw dokładnie na platformie
                break;
            }
        }

        // Nie spadaj poniżej ziemi
        if (y > screenHeight - bitmap.getHeight()) {
            y = screenHeight - bitmap.getHeight();
            velocityY = 0;
        }
    }
    private boolean isLandingOn(Platform p) {
        Rect playerRect = getRect();
        Rect platRect = p.getRect();

        // kot musi być **nad platformą w poprzednim kroku**
        boolean falling = velocityY > 0;

        // poziomy zasięg
        boolean horizontallyOverlaps = playerRect.right > platRect.left && playerRect.left < platRect.right;

        // pionowy kontakt od góry platformy
        boolean verticallyTouching = playerRect.bottom >= platRect.top && playerRect.bottom - velocityY < platRect.top;

        return falling && horizontallyOverlaps && verticallyTouching;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    private boolean collidesWith(Platform p) {
        return Rect.intersects(getRect(), p.getRect());
    }

    private Rect getRect() {
        return new Rect((int)x, (int)y, (int)x + bitmap.getWidth(), (int)y + bitmap.getHeight());
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setMovingLeft(boolean left) {
        this.movingLeft = left;
    }

    public void setMovingRight(boolean right) {
        this.movingRight = right;
    }
}
