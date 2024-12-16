package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditBillScreen extends AppCompatActivity {
    ImageButton home, bills, profile, add;
    EditText editBillName, editAmount, editDueDate;
    Spinner editCategorySpinner;
    Button saveButton, backButton;

    // Firebase Firestore instance
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();


        //bottom navigation
        add = findViewById(R.id.addButton);
        bills = findViewById(R.id.billButton);
        profile = findViewById(R.id.profileButton);
        home = findViewById(R.id.homeButton);

        // Initialize views
        editBillName = findViewById(R.id.editBillName);
        editCategorySpinner = findViewById(R.id.editCategorySpinner);
        editAmount = findViewById(R.id.editAmount);
        editDueDate = findViewById(R.id.editDueDate);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.cancelButton);

        // Retrieve data from Intent
        Intent retrieveData = getIntent();
        String billId1 = retrieveData.getStringExtra("bill_id");
        String billName1 = retrieveData.getStringExtra("bill_name");
        double billAmount1 = retrieveData.getDoubleExtra("bill_amount", 0.0);
        String billDueDate1 = retrieveData.getStringExtra("bill_due_date");
        String billCategory1 = retrieveData.getStringExtra("bill_category");

        editBillName.setText(billName1);
        editAmount.setText(String.valueOf(billAmount1));
        editDueDate.setText(billDueDate1);

        if (billCategory1 != null) {
            setSpinnerValue(editCategorySpinner, billCategory1);
        }

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditBillScreen.this, MainActivity.class));
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditBillScreen.this, YourBillScreen.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditBillScreen.this, ProfileScreen.class));
            }
        });

        // Enable up navigation in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Bill");
        }

        // Date picker for due date field
        editDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                        editDueDate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {
            String updatedName = editBillName.getText().toString().trim();
            String updatedCategory = editCategorySpinner.getSelectedItem().toString().trim();
            String updatedAmount = editAmount.getText().toString().trim();
            String updatedDueDate = editDueDate.getText().toString().trim();

            String billId = getIntent().getStringExtra("bill_id");
            if (billId == null) {
                Log.e("EditBillScreen", "bill_id is null");
                return;
            }

            try {
                int index = Integer.parseInt(billId);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference userDocRef = db.collection("users").document(userId);

                userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> billsArray = (List<Map<String, Object>>) documentSnapshot.get("bills");
                        if (billsArray != null && index >= 0 && index < billsArray.size()) {
                            Map<String, Object> billToUpdate = billsArray.get(index);

                            // Update bill fields
                            billToUpdate.put("BillName", updatedName);
                            billToUpdate.put("Category", updatedCategory);
                            billToUpdate.put("Amount", Double.parseDouble(updatedAmount));
                            billToUpdate.put("DueDate", updatedDueDate);

                            // Check if the updated due date is in the past
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                            try {
                                Date parsedDueDate = dateFormat.parse(updatedDueDate);
                                Date currentDate = new Date(); // Current date

                                if (parsedDueDate != null && parsedDueDate.before(currentDate)) {
                                    billToUpdate.put("status", "unsettled"); // Set status to overdue
                                } else {
                                    billToUpdate.put("status", "unpaid"); // Reset to unpaid if not past due
                                }
                            } catch (ParseException e) {
                                Log.e("EditBillScreen", "Error parsing DueDate: " + updatedDueDate + e);
                            }

                            // Update Firestore
                            userDocRef.update("bills", billsArray)
                                    .addOnSuccessListener(aVoid -> {
                                        Intent intent = new Intent(EditBillScreen.this, YourBillScreen.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish(); // Finish the current activity
                                    })
                                    .addOnFailureListener(e -> Log.e("FirestoreError", "Error updating bill: ", e));
                        } else {
                            Log.e("EditBillScreen", "Invalid index: " + index);
                        }
                    }
                }).addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching document: ", e));
            } catch (NumberFormatException e) {
                Log.e("EditBillScreen", "Invalid bill_id: " + billId, e);
            }
        });




        backButton.setOnClickListener(v -> {
            // Navigate back to YourBillScreen
            Intent intent = new Intent(EditBillScreen.this, YourBillScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Ensure it clears the stack
            startActivity(intent);
            finish(); // Finish the current activity
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    // Handle toolbar back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and return to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Helper method to set Spinner value
    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
