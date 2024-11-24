package com.example.finalproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpScreen extends AppCompatActivity {
    ImageButton back;
    Button submit;
    EditText fName, lName, phNumber, homeAdd, email, password;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_screen);

        back = findViewById(R.id.imageButtonBack);
        submit = findViewById(R.id.submitButton);
        fName = findViewById(R.id.firstNameInput);
        lName = findViewById(R.id.lastNameInput);
        phNumber = findViewById(R.id.phoneNumberInput);
        homeAdd = findViewById(R.id.homeAddress);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);




        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });





    }
}