package com.example.lab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class admin extends AppCompatActivity {
Button Add, Update, Delete;
TextView TextView4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
     Add = findViewById(R.id.button);
     Update = findViewById(R.id.button3);
     Delete = findViewById(R.id.button4);

     Add.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
//             Intent intent = new Intent();
         }
     });
    }
}