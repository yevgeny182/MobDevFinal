package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginScreen extends AppCompatActivity {
    ImageButton back, togglePass;
    EditText user, pass;
    Button login;
    TextView forgotPass;
    private FirebaseAuth loginAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        back = findViewById(R.id.imageButtonBack);
        togglePass = findViewById(R.id.imageButtonEye);
        user = findViewById(R.id.usernameInput);
        pass = findViewById(R.id.passwordInput);
        login = findViewById(R.id.LoginUserButton);
        forgotPass = findViewById(R.id.passwordText);


        loginAuth = FirebaseAuth.getInstance();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, LandingScreen.class));
            }
        });

        login.setEnabled(false);
        login.setAlpha(0.5f);

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, ChangePasswordScreen.class));
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String username = user.getText().toString().trim();
                String password = pass.getText().toString().trim();
                if (!username.isEmpty() && !password.isEmpty()) {
                    login.setEnabled(true);
                    login.setAlpha(1.0f);
                } else {
                    login.setEnabled(false);
                    login.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        user.addTextChangedListener(watcher);
        pass.addTextChangedListener(watcher);

        boolean passwordUpdated = getIntent().getBooleanExtra("passwordUpdated", false);
        if (passwordUpdated) {
            String newPassword = getIntent().getStringExtra("newPassword");
            if (newPassword != null) {
                updatePassword(newPassword);
            }
        }
        login.setOnClickListener(view -> {
            String username = user.getText().toString().trim();
            String password = pass.getText().toString().trim();
            // Proceed to MainActivity
            if (!username.isEmpty() && !password.isEmpty()) {
                loginAuth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(LoginScreen.this, MainActivity.class));
                                } else {
                                    Snackbar.make(view, "Invalid username or password, please try again.", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        togglePass.setOnClickListener(new View.OnClickListener() {
            boolean passwordSeen = false;

            @Override
            public void onClick(View view) {
                if (passwordSeen) {
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePass.setImageResource(R.drawable.baseline_visibility_off_24);
                    pass.setSelection(pass.getText().length());
                    passwordSeen = false;
                } else {
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePass.setImageResource(R.drawable.baseline_visibility_24);
                    pass.setSelection(pass.getText().length());
                    passwordSeen = true;
                }
            }
        });


    }

    private void updatePassword(String password) {
        // Assuming the user is logged in; you may need to handle cases where they are not
        FirebaseUser user = loginAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(password)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Optionally update Firestore or handle success
                                Toast.makeText(LoginScreen.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginScreen.this, "Error updating password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(LoginScreen.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}