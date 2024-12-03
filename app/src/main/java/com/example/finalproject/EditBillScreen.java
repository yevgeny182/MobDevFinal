package com.example.finalproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class EditBillScreen extends AppCompatActivity {
    ImageButton home, bills, profile, add;
    EditText editBillName, editAmount, editDueDate;
    Spinner editCategorySpinner;
    Button saveButton, paidButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill_screen);

        // Initialize views
        editBillName = findViewById(R.id.editBillName);
        editCategorySpinner = findViewById(R.id.editCategorySpinner);
        editAmount = findViewById(R.id.editAmount);
        editDueDate = findViewById(R.id.editDueDate);
        saveButton = findViewById(R.id.saveButton);
        paidButton = findViewById(R.id.paidButton);

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

        // Save button functionality
        saveButton.setOnClickListener(v -> {
            String billName = editBillName.getText().toString().trim();
            String category = editCategorySpinner.getSelectedItem().toString().trim();
            String amount = editAmount.getText().toString().trim();
            String dueDate = editDueDate.getText().toString().trim();

            // Here, you would typically save the data to a database or shared preferences
            // Example: DatabaseHelper.updateBill(billId, billName, category, amount, dueDate);

            // Show success message or navigate back
            Intent intent = new Intent();
            intent.putExtra("result", "Bill Updated Successfully");
            setResult(RESULT_OK, intent);
            finish(); // Go back to the previous screen
        });

        // Paid button functionality
        paidButton.setOnClickListener(v -> {
            // Mark the bill as paid
            // Example: DatabaseHelper.markBillAsPaid(billId);

            // Optionally navigate or show a success message
            Intent intent = new Intent();
            intent.putExtra("result", "Bill Marked as Paid");
            setResult(RESULT_OK, intent);
            finish();
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
}
