package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ChangePasswordScreen extends AppCompatActivity {

    ImageButton back;
    EditText username;
    Button submit;

    FirebaseAuth auth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        back = findViewById(R.id.backBtn);
        username = findViewById(R.id.usernameInput);
        submit = findViewById(R.id.submitButton);


        auth = FirebaseAuth.getInstance();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChangePasswordScreen.this, LandingScreen.class));
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = username.getText().toString();
                Log.d("Debug", "Email being passed inside Submit: " + email);
                if(email.isEmpty()){
                    username.setError("Email is required");
                    username.requestFocus();
                    return;
                }
                sendPasswordResetEmail(email);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void sendPasswordResetEmail(String email) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(resetTask -> {
                                if (resetTask.isSuccessful()) {
                                    Toast.makeText(this, "Password reset email sent successfully.", Toast.LENGTH_LONG).show();
                                    Log.d("ResetPassword", "Password reset email sent to: " + email);
                                    startActivity(new Intent(ChangePasswordScreen.this, ChangePasswordScreenV2.class));
                                    finish();
                                } else {
                                    Log.e("ResetPassword", "Error sending reset email: " + resetTask.getException());
                                    Toast.makeText(this, "Failed to send password reset email. Please try again.", Toast.LENGTH_LONG).show();
                                }
                            });
            } else {
                Log.e("ResetPassword", "Error fetching sign-in methods: " + task.getException());
                Toast.makeText(this, "Error occurred. Please try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }

}
