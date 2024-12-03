package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileScreen extends AppCompatActivity {
    ImageButton home, add, bills, profile, logout;
    String loggedUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_screen);

        home = findViewById(R.id.homeButton);
        add = findViewById(R.id.addButton);
        bills = findViewById(R.id.billButton);
        profile = findViewById(R.id.profileButton);
        logout = findViewById(R.id.logoutIcon);

        mAuth = FirebaseAuth.getInstance();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileScreen.this, MainActivity.class));
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileScreen.this, AddBillScreen.class));
            }
        });
        bills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileScreen.this, YourBillScreen.class));
            }
        });
        profile.setImageResource(R.drawable.baseline_person_24);

        displayLoggedInUserData();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertLogout = new AlertDialog.Builder(ProfileScreen.this);
                alertLogout.setTitle("Logout of Spend Insight?");
                alertLogout.setMessage("Are you sure you want to logout?");
                alertLogout.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(logOutofApp()){
                            startActivity(new Intent(ProfileScreen.this, LoginScreen.class));
                            finish();
                        }
                    }
                });
                alertLogout.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertLogout.show();
            }
        });

    }

    public void displayLoggedInUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView tvUsername = findViewById(R.id.nameholder);
        TextView tvUser = findViewById(R.id.username);
        TextView contactNumber = findViewById(R.id.contact);
        TextView address = findViewById(R.id.address);
        TextView email = findViewById(R.id.email);

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
                                String loggedUser = document.getString("Username");
                                String loggedcontact = document.getString("PhoneNum");
                                String loggedaddress = document.getString("Address");



                                tvUsername.setText(loggedUserName); // Update the TextView here
                                tvUser.setText(loggedUser);
                                contactNumber.setText(loggedcontact);
                                address.setText(loggedaddress);
                                email.setText(loggedUser);


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

    boolean logOutofApp(){
        mAuth.signOut();
        return true;
    }
}