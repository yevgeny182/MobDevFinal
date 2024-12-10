package com.example.finalproject;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
    public void addNewBill(String billName, String category, double amount, String dueDate) {
        if (TextUtils.isEmpty(billName) || TextUtils.isEmpty(category) || amount <= 0 || TextUtils.isEmpty(dueDate)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the due date to check if it's in the past
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        try {
            Date parsedDueDate = dateFormat.parse(dueDate);
            Calendar currentCalendar = Calendar.getInstance();
            Calendar dueDateCalendar = Calendar.getInstance();

            if (parsedDueDate != null) {
                dueDateCalendar.setTime(parsedDueDate);

                // Reset the time to compare only the dates (ignore time components)
                currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
                currentCalendar.set(Calendar.MINUTE, 0);
                currentCalendar.set(Calendar.SECOND, 0);
                currentCalendar.set(Calendar.MILLISECOND, 0);

                dueDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                dueDateCalendar.set(Calendar.MINUTE, 0);
                dueDateCalendar.set(Calendar.SECOND, 0);
                dueDateCalendar.set(Calendar.MILLISECOND, 0);
            }

            // Set status based on date comparison
            String status = parsedDueDate != null && dueDateCalendar.before(currentCalendar) ? "unsettled" : "unpaid";

            String userID = userInfoDB.getCurrentUser().getUid();
            Map<String, Object> billInfo = new HashMap<>();
            billInfo.put("BillName", billName);
            billInfo.put("Category", category);
            billInfo.put("Amount", amount);
            billInfo.put("DueDate", dueDate);
            billInfo.put("status", status);

            // Step 1: Add the new bill to the "bills" array
            addBill.collection("users").document(userID)
                    .update("bills", FieldValue.arrayUnion(billInfo))
                    .addOnSuccessListener(aVoid -> {
                        // Step 2: Fetch the current total_expenses and update it with the new bill amount
                        addBill.collection("users").document(userID)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
//                                        // Get the current total_expenses value
//                                        Double currentTotalExpenses = documentSnapshot.getDouble("total_expenses");
//                                        if (currentTotalExpenses == null) {
//                                            currentTotalExpenses = 0.0;
//                                        }
//
//                                        // Update the total_expenses by adding the new bill amount
//                                        double newTotalExpenses = currentTotalExpenses + amount;
//
//                                        // Update the total_expenses field in Firestore
//                                        addBill.collection("users").document(userID)
//                                                .update("total_expenses", newTotalExpenses)
//                                                .addOnSuccessListener(unused ->
//                                                        Toast.makeText(AddBillScreen.this, "Bill Added and Total Expenses Updated!", Toast.LENGTH_SHORT).show())
//                                                .addOnFailureListener(e ->
//                                                        Toast.makeText(AddBillScreen.this, "Error updating total expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        Toast toast = Toast.makeText(AddBillScreen.this, "Bill Added!", Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 10); // Top position with 100px margin
                                        toast.show();

                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(AddBillScreen.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(AddBillScreen.this, "Error adding bill: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Please use MM/dd/yyyy.", Toast.LENGTH_SHORT).show();
        }
    }

}