package com.example.finalproject;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import java.util.Calendar;

public class AddBillScreen extends AppCompatActivity {
    ImageButton home, bills, profile, add;
    EditText billname, category, amount, duedate;
    Button createbill;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_bill_screen);

        home = findViewById(R.id.homeButton);
        bills = findViewById(R.id.billButton);
        profile = findViewById(R.id.profileButton);
        add = findViewById(R.id.addButton);
        billname = findViewById(R.id.billName);
        category = findViewById(R.id.Category);
        amount = findViewById(R.id.Amount);
        duedate = findViewById(R.id.dueDate);
        createbill = findViewById(R.id.createBillButton);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddBillScreen.this, MainActivity.class));
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddBillScreen.this, YourBillScreen.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddBillScreen.this, ProfileScreen.class));
            }
        });


        duedate.setOnClickListener(v ->{
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day  = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = (selectedMonth + 1) + "/" +  selectedDay + "/" + selectedYear;
                        duedate.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });

        add.setImageResource(R.drawable.baseline_add_circle_24);



    }
}