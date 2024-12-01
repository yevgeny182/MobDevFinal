package com.example.finalproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddBillScreen extends AppCompatActivity {
    ImageButton home, bills, profile, add;
    EditText billname, amount, duedate;
    Spinner category;
    Button createbill;
    FirebaseFirestore addBill;
    private FirebaseAuth userInfoDB;

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
        category = findViewById(R.id.categorySpinner);
        amount = findViewById(R.id.Amount);
        duedate = findViewById(R.id.dueDate);
        createbill = findViewById(R.id.createBillButton);

        addBill = FirebaseFirestore.getInstance();
        userInfoDB = FirebaseAuth.getInstance();

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

        /* category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCategory = adapterView.getItemAtPosition(i).toString();
                if(selectedCategory.equals("Category")){
                    Toast.makeText(AddBillScreen.this, "Please select a category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
         */

        createbill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String billName = billname.getText().toString().trim();
                String selectedCategory = category.getSelectedItem().toString();
                String billAmountText = amount.getText().toString().trim();
                String dueDate = duedate.getText().toString().trim();

                if (selectedCategory.equals("Category")) {
                    Toast.makeText(AddBillScreen.this, "Please select a valid category", Toast.LENGTH_SHORT).show();
                    return;
                }

                double billAmount = 0;
                try {
                    billAmount = Double.parseDouble(billAmountText);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddBillScreen.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                addNewBill(billName, selectedCategory, billAmount, dueDate);
                    billname.setText("");
                    amount.setText("");
                    duedate.setText("");
                    category.setSelection(0);
            }

        });




    }
    public void addNewBill(String billName, String category, double amount, String dueDate){
        if (TextUtils.isEmpty(billName) || TextUtils.isEmpty(category) || amount <= 0 || TextUtils.isEmpty(dueDate)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String userID = userInfoDB.getCurrentUser().getUid();
        Map<String, Object> billInfo = new HashMap<>();
        billInfo.put("BillName", billName);
        billInfo.put("Category", category);
        billInfo.put("Amount", amount);
        billInfo.put("DueDate", dueDate);
        billInfo.put("status", "unpaid");

        addBill.collection("users").document(userID)
                .update("bills", FieldValue.arrayUnion(billInfo))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddBillScreen.this, "Bill Added Successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddBillScreen.this, "Error adding bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}