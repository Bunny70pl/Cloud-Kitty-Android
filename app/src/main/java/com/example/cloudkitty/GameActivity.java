package com.example.cloudkitty;

import android.os.Bundle;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float touchX = event.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (touchX < gameView.getWidth() / 2) {
                    gameView.movePlayerLeft(true);
                    gameView.movePlayerRight(false);
                } else {
                    gameView.movePlayerRight(true);
                    gameView.movePlayerLeft(false);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                gameView.movePlayerLeft(false);
                gameView.movePlayerRight(false);
                break;
        }

        if (gameView.isGameOver()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (touchX < gameView.getWidth() / 2) {
                    // restart gry
                    gameView.restartGame();
                } else {
                    // powrót do menu
                    finish(); // zamyka GameActivity → wraca do MainActivity (menu główne)
                }
            }
            return true;
        }


        return true;
    }
}