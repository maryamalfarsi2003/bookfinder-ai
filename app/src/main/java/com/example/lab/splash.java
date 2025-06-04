package com.example.lab;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class splash extends AppCompatActivity {
Handler splash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        splash= new Handler();
        splash.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splash
                        =new Intent(splash.this, login.class);
                startActivity(splash);
                finish();
            }
        },4000);
    }
}