package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YourBillScreen extends AppCompatActivity {
    ImageButton home, add, profile;
    SearchView searchBox;

    Button createBill;

    private RecyclerView recyclerView;
    private TextView noBillsText;
    private BillAdapter_billpage billAdapter;
    private List<Bill_model_billpage> billList;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_bill_screen);

        home = findViewById(R.id.homeButton);
        add = findViewById(R.id.addButton);
        profile = findViewById(R.id.profileButton);


        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(YourBillScreen.this, MainActivity.class));
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(YourBillScreen.this, AddBillScreen.class));
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(YourBillScreen.this, ProfileScreen.class));
            }
        });

//          Code here
        recyclerView = findViewById(R.id.bill_List_recycler);
        noBillsText = findViewById(R.id.no_bills_text);
        searchBox = findViewById(R.id.searchView);

//        billList = new ArrayList<>();
//        billList.add(new Bill_model_billpage("1", "Electricity Bill", "Utilities", 500.65, "12/5/2024", "Unpaid"));
//        billList.add(new Bill_model_billpage("2", "Water Bill", "Utilities", 200.0, "12/10/2024", "Paid"));
//        billList.add(new Bill_model_billpage("3", "Shopping Bill", "Shopping", 100.0, "12/15/2024", "Unpaid"));
//
//        billAdapter = new BillAdapter_billpage(billList, YourBillScreen.this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(billAdapter);



        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Load bills from Firestore
        billList = new ArrayList<>();

        // Set up RecyclerView
        billAdapter = new BillAdapter_billpage(billList,YourBillScreen.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(billAdapter);
        loadBillsFromFirestore();

        Log.d("BillPage", "Logging BillList:" + billList.size() + " Bills");


        searchBox.setIconifiedByDefault(false); // Ensure the search view is expanded by default
        searchBox.setFocusable(true);          // Make it focusable
        searchBox.setFocusableInTouchMode(true); // Ensure it reacts to touch

        Log.d("ForFilter", "onCreate: "+ billAdapter.getItemCount());
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                billAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                billAdapter.getFilter().filter(query);
                return false;
            }
        });


    }
    public void loadBillsFromFirestore() {
        // Reference to the user's document in Firestore
        DocumentReference billsDocRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        billsDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    // Clear the current bill list
                    billList.clear();

                    // Log the raw document data
                    Log.d("FirestoreData", "Raw Document: " + document.getData());

                    // Get the "bills" array field
                    List<Map<String, Object>> billsArray = (List<Map<String, Object>>) document.get("bills");

                    if (billsArray != null && !billsArray.isEmpty()) {
                        // Process each bill in the array
                        for (Map<String, Object> billMap : billsArray) {
                            try {
                                // Safely retrieve each field with default values
                                String id = billMap.containsKey("id") ? billMap.get("id").toString() : "Unknown";
                                String billName = billMap.containsKey("BillName") ? billMap.get("BillName").toString() : "No Name";
                                String category = billMap.containsKey("Category") ? billMap.get("Category").toString() : "Uncategorized";
                                double amount = billMap.containsKey("Amount") ? Double.parseDouble(billMap.get("Amount").toString()) : 0.0;
                                String dueDate = billMap.containsKey("DueDate") ? billMap.get("DueDate").toString() : "No Due Date";
                                String statusBill = billMap.containsKey("status") ? billMap.get("status").toString() : "Error";

                                // Check if the bill's due date has passed
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                                Date dueDateObj = sdf.parse(dueDate);
                                Date currentDate = new Date();

                                if (dueDateObj != null && currentDate.after(dueDateObj) && statusBill.equalsIgnoreCase("unpaid")) {
                                    // Update the status to "unsettled"
                                    billMap.put("status", "unsettled");
                                    statusBill = "unsettled"; // Update local variable
                                }

                                // Create a new Bill_model_billpage object with the color
                                Bill_model_billpage bill = new Bill_model_billpage(id, billName, category, amount, dueDate, statusBill);


                                // Add the bill to the list
                                billList.add(bill);
                                Log.d("FirestoreData", "Parsed Bill: " + billName + ", Status: " + statusBill );
                            } catch (Exception e) {
                                Log.e("FirestoreError", "Error parsing or updating bill data", e);
                            }
                        }

                        // Update the adapter's allBillList with the newly loaded data
                        billAdapter.getAllBillList().clear();
                        billAdapter.getAllBillList().addAll(billList);
                        // Notify the adapter about the updated data
                        billAdapter.notifyDataSetChanged();

                        // Log after bills are loaded
                        Log.d("FirestoreData", "Total Bills Loaded: " + billList.size());
                        recyclerView.setVisibility(View.VISIBLE);
                        noBillsText.setVisibility(View.GONE);
                    } else {
                        // No bills in the array
                        Log.d("FirestoreData", "No bills found in Firestore");
                        noBillsText.setText("No Bills Existed");
                        noBillsText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    // Document doesn't exist
                    Log.d("FirestoreData", "Document does not exist");
                    noBillsText.setText("No Bills Existed");
                    noBillsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                // Handle Firestore errors
                Exception exception = task.getException();
                Log.e("FirestoreError", "Error fetching document", exception);
                noBillsText.setText("Error loading data");
                noBillsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }


}