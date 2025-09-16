package com.example.cloudkitty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SkinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);

        SharedPreferences prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        int highScore = prefs.getInt("high_score",0);

        ImageButton redBtn = findViewById(R.id.btn_red);
        ImageButton whiteBtn = findViewById(R.id.btn_white);
        ImageButton blackBtn = findViewById(R.id.btn_black);
        Button backBtn = findViewById(R.id.btn_back);

        redBtn.setOnClickListener(v -> selectSkin("cat_red"));

        if(highScore >= 50){ whiteBtn.setAlpha(1f); whiteBtn.setEnabled(true);}
        else{ whiteBtn.setAlpha(0.3f); whiteBtn.setEnabled(false);}
        whiteBtn.setOnClickListener(v -> selectSkin("cat_white"));

        if(highScore >= 3000){ blackBtn.setAlpha(1f); blackBtn.setEnabled(true);}
        else{ blackBtn.setAlpha(0.3f); blackBtn.setEnabled(false);}
        blackBtn.setOnClickListener(v -> selectSkin("cat_black"));

        backBtn.setOnClickListener(v -> finish());
    }

    private void selectSkin(String skinName){
        SharedPreferences prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("selected_skin", skinName).apply();
        finish();
    }
}
