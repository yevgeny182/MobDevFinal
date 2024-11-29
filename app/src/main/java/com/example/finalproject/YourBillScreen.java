package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        billList = new ArrayList<>();
        billAdapter = new BillAdapter_billpage(billList,YourBillScreen.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(billAdapter);

        // Load bills from Firestore
        loadBillsFromFirestore();

        searchBox.setIconifiedByDefault(false); // Ensure the search view is expanded by default
        searchBox.setFocusable(true);          // Make it focusable
        searchBox.setFocusableInTouchMode(true); // Ensure it reacts to touch


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
    private void loadBillsFromFirestore() {
        CollectionReference billsRef = db.collection("bills");

        // Try to fetch the collection
        billsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();

                // Check if the collection exists and has documents
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Clear the current bill list
                    billList.clear();

                    // Process each document
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Bill_model_billpage bill = document.toObject(Bill_model_billpage.class);
                        billList.add(bill);
                    }

                    // Notify the adapter about data changes
                    billAdapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    noBillsText.setVisibility(View.GONE);
                } else {
                    // Collection exists but no documents
                    noBillsText.setText("No Bills Existed");
                    noBillsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                // Handle errors
                Exception exception = task.getException();
                Log.e("FirestoreError", "Error fetching collection", exception);
                noBillsText.setText("Error loading data");
                noBillsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

}