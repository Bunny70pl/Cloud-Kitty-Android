package com.example.cloudkitty;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private boolean running;
    private final SurfaceHolder holder;
    private final GameView gameView;

    public GameThread(SurfaceHolder holder, GameView view) {
        this.holder = holder; this.gameView = view;
    }

    public void setRunning(boolean running) { this.running = running; }

    @Override
    public void run() {
        while(running) {
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    synchronized (holder) {
                        gameView.update();
                        gameView.draw(canvas);
                    }
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            try { sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}
