package com.example.lab;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class register extends AppCompatActivity {

    TextView textView3;
    EditText email;
    EditText password;
    Button register;
    FirebaseAuth Bauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        Bauth= FirebaseAuth.getInstance();
        textView3=findViewById(R.id.textView3);
        email= findViewById(R.id.email);
        password=findViewById(R.id.password);
        register=findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();

            }
        });

    }
    public void registerNewUser(){
        String e,p;
        e=email.getText().toString();
        p=password.getText().toString();
        if (TextUtils.isEmpty(e)) {
        Toast.makeText(getApplicationContext(), "Cannot Leave Field Empty",Toast.LENGTH_LONG).show();
        return;
        }
        if (TextUtils.isEmpty(p)){
            Toast.makeText(getApplicationContext(),"Cannot Leave Field Empty", Toast.LENGTH_LONG).show();
            return;
        }
        Bauth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Registration Completed",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(register.this, login.class);
                    startActivity(intent);
                }
            }
        });
    }
}
