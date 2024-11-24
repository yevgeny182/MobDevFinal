package com.example.finalproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginScreen extends AppCompatActivity {
    ImageButton back, togglePass;
    EditText user, pass;
    Button login;
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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        login.setEnabled(false);
        login.setAlpha(0.5f);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    String username = user.getText().toString().trim();
                    String password = pass.getText().toString().trim();
                    if(!username.isEmpty() && !password.isEmpty()){
                        login.setEnabled(true);
                        login.setAlpha(1.0f);
                    }else{
                        login.setEnabled(false);
                        login.setAlpha(0.5f);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        togglePass.setOnClickListener(new View.OnClickListener() {
            boolean passwordSeen = false;
            @Override
            public void onClick(View view) {
                if(passwordSeen){
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePass.setImageResource(R.drawable.baseline_visibility_off_24);
                    pass.setSelection(pass.getText().length());
                    passwordSeen = false;
                }else{
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePass.setImageResource(R.drawable.baseline_visibility_24);
                    pass.setSelection(pass.getText().length());
                    passwordSeen = true;
                }
            }
        });


    }
}