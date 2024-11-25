package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpScreen extends AppCompatActivity {
    ImageButton back;
    Button submit;
    EditText fName, lName, phNumber, homeAdd, email, password;
    private FirebaseAuth signUpAuth;
    FirebaseFirestore userInfoDB;
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

        signUpAuth = FirebaseAuth.getInstance();
        userInfoDB = FirebaseFirestore.getInstance();



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String FName = fName.getText().toString();
                String LName = lName.getText().toString();
                String phone = phNumber.getText().toString();
                String home = homeAdd.getText().toString();
                String user = email.getText().toString();
                String pass = password.getText().toString();
                registerNewUser(FName, LName, phone, home, user, pass);
            }
        });

    }

    public void registerNewUser(String FName, String LName, String phone, String home, String username, String password){
        if(TextUtils.isEmpty(FName)
            || TextUtils.isEmpty(LName)
            || TextUtils.isEmpty(phone)
            || TextUtils.isEmpty(home)
            || TextUtils.isEmpty(username)
            || TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        signUpAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String userID = signUpAuth.getCurrentUser().getUid();
                            Map<String,Object> userInfo = new HashMap<>();
                            userInfo.put("FirstName", FName);
                            userInfo.put("LastName", LName);
                            userInfo.put("PhoneNum", phone);
                            userInfo.put("Username", username);
                            userInfo.put("Password", password);
                            userInfoDB.collection("users").document(userID)
                                .set(userInfo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(SignUpScreen.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignUpScreen.this, LoginScreen.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUpScreen.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                        }else{
                            Toast.makeText(SignUpScreen.this, "Registration Error", Toast.LENGTH_SHORT).show();
                        }
                    }
            });
    }
}