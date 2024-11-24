package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileScreen extends AppCompatActivity {
    ImageButton home, add, bills, profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_screen);

        home = findViewById(R.id.homeButton);
        add = findViewById(R.id.addButton);
        bills = findViewById(R.id.billButton);
        profile = findViewById(R.id.profileButton);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileScreen.this, MainActivity.class));
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileScreen.this, AddBillScreen.class));
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileScreen.this, YourBillScreen.class));
            }
        });
        profile.setImageResource(R.drawable.baseline_person_24);

    }
}