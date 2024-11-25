package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LandingScreen extends AppCompatActivity {

    Button login, signup;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        mAuth = FirebaseAuth.getInstance();

        if(loggedUser()){
            startActivity(new Intent(LandingScreen.this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_landing_screen);


        login = findViewById(R.id.Login);
        signup = findViewById(R.id.SignUp);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LandingScreen.this, LoginScreen.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LandingScreen.this, SignUpScreen.class));
            }
        });

    }

    boolean loggedUser() {
        return mAuth.getCurrentUser() != null;
    }

}