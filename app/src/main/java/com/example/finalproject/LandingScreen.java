package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import com.google.firebase.auth.FirebaseAuth;

public class LandingScreen extends AppCompatActivity {

    Button login, signup;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_screen);

        ArrayList<SlideModel> imageList = new ArrayList<>();

        imageList.add(new SlideModel(R.drawable.img1, ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.img2, ScaleTypes.CENTER_CROP));

        ImageSlider imageSlider = findViewById(R.id.image_slider);
        imageSlider.setImageList(imageList);

        mAuth = FirebaseAuth.getInstance();

        if(loggedUser()){
            startActivity(new Intent(LandingScreen.this, MainActivity.class));
            finish();
            return;
        }



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