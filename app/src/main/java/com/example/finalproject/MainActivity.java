package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ImageButton add, bills, profile, home;
    String loggedUserId;
    TextView expensesCost;
    TextView expensesBillPaid;
    TextView expensesBillunsettled;

    TextView tvUsername;

    FirebaseAuth auth = FirebaseAuth.getInstance();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //bottom navigation
        add = findViewById(R.id.addButton);
        bills = findViewById(R.id.billButton);
        profile = findViewById(R.id.profileButton);
        home = findViewById(R.id.homeButton);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddBillScreen.class));
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, YourBillScreen.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileScreen.class));
            }
        });
        home.setImageResource(R.drawable.baseline_home_24);
        // ------------------ Write Here---------
        expensesCost = findViewById(R.id.tvTotalBillValue);
        expensesBillPaid= findViewById(R.id.tvPaidBillValue);
        expensesBillunsettled=findViewById(R.id.tvUnsettledBillValue);

        getLoggedInUserData();
//        getUserDataAndSetDefaults(loggedUserId);


    } // End OnCreate()
    public void getLoggedInUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView tvUsername = findViewById(R.id.username_text);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            loggedUserId = currentUser.getUid(); // Logged-in user's UID

            // Fetch user data from Firestore
            db.collection("users").document(loggedUserId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve and set the username
                                String loggedUserName = document.getString("FirstName") + " " + document.getString("LastName");
                                tvUsername.setText(loggedUserName); // Update the TextView here

                                // Safely fetch the fields, or use default values if they don't exist
                                Long totalBillCost = document.contains("total_expenses") ? document.getLong("total_expenses") : 0L;
                                Long paidBillCost = document.contains("paid_bills") ? document.getLong("paid_bills") : 0L;
                                Long unsettledBillCost = document.contains("unsettled_bills") ? document.getLong("unsettled_bills") : 0L;

// Convert to int if needed
                                int totalBillCostInt = totalBillCost.intValue();
                                int paidBillCostInt = paidBillCost.intValue();
                                int unsettledBillCostInt = unsettledBillCost.intValue();

// Update the TextViews
                                expensesCost.setText(String.valueOf(totalBillCostInt));
                                expensesBillPaid.setText(String.valueOf(paidBillCostInt));
                                expensesBillunsettled.setText(String.valueOf(unsettledBillCostInt));

                            } else {
                                Log.w("USER_DATA", "No user data found.");
                                tvUsername.setText("User not found");
                            }
                        } else {
                            Log.e("USER_DATA_ERROR", "Error getting user data: " + task.getException().getMessage());
                            tvUsername.setText("Error fetching data");
                        }
                    });
        } else {
            Log.w("USER_WARNING", "No user is currently logged in.");
            tvUsername.setText("Not logged in");
        }
    }
}