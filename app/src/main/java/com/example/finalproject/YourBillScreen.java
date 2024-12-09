package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
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


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Load bills from Firestore
        billList = new ArrayList<>();

        // Set up RecyclerView
        billAdapter = new BillAdapter_billpage(billList, YourBillScreen.this,bill -> {
            // Handle item click here
//            Toast.makeText(this, "YOU CLICKED"+ bill.getId(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, EditBillScreen.class);
            intent.putExtra("bill_id", bill.getId());
            intent.putExtra("bill_name", bill.getBillName());
            intent.putExtra("bill_category", bill.getCategory());
            intent.putExtra("bill_amount", bill.getAmount());
            intent.putExtra("bill_due_date", bill.getDueDate());
            intent.putExtra("bill_status", bill.getStatus());
            Log.d("YourBillScreen", "Navigating to EditBillScreen with data: " + bill.getBillName());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(billAdapter);
        loadBillsFromFirestore();

        Log.d("BillPage", "Logging BillList:" + billList.size() + " Bills");


        searchBox.setIconifiedByDefault(false); // Ensure the search view is expanded by default
        searchBox.setFocusable(true);          // Make it focusable
        searchBox.setFocusableInTouchMode(true); // Ensure it reacts to touch

        Log.d("ForFilter", "onCreate: " + billAdapter.getItemCount());
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

        enableSwipesLeftOrRight();
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
                        for (int index = 0; index < billsArray.size(); index++) {
                            Map<String, Object> billMap = billsArray.get(index);

                            try {
                                // Safely retrieve each field with default values
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

                                // Create a new Bill_model_billpage object with the index
                                Bill_model_billpage bill = new Bill_model_billpage(String.valueOf(index), billName, category, amount, dueDate, statusBill);

                                // Add the bill to the list
                                billList.add(bill);
                                Log.d("FirestoreData", "Parsed Bill: " + billName + ", Status: " + statusBill);
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

    private void enableSwipesLeftOrRight() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // No move functionality needed
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    new AlertDialog.Builder(YourBillScreen.this)
                            .setTitle("Delete Item")
                            .setMessage("Are you sure you want to delete this item?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                deleteItem(position);
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                billAdapter.notifyItemChanged(position);
                            })
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    new AlertDialog.Builder(YourBillScreen.this)
                            .setTitle("Confirm Payment Status")
                            .setMessage("Please confirm that this bill has been paid. This action is final.")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                markAsPaid(position);
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                billAdapter.notifyItemChanged(position);
                            })
                            .show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                float maxSwipeDistance = 300;

                float newDX = Math.min(Math.max(dX, -maxSwipeDistance), maxSwipeDistance);
                if (dX > 0) {
                    int paidColor = ContextCompat.getColor(YourBillScreen.this, R.color.backgroundPaid); // Replace with your color resource
                    paint.setColor(paidColor);
                    float cornerRadius = 12 * recyclerView.getContext().getResources().getDisplayMetrics().density; // Convert 12dp to pixels
                    RectF rectF = new RectF(
                            itemView.getLeft(),
                            itemView.getTop(),
                            itemView.getLeft() + dX, // Use dX for dynamic width
                            itemView.getBottom()
                    );
                    c.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
                    Drawable checkIcon = ContextCompat.getDrawable(YourBillScreen.this, R.drawable.check_circle_24px);
                    if (checkIcon != null) {
                        DrawableCompat.setTint(checkIcon, ContextCompat.getColor(YourBillScreen.this, R.color.backgroundPaidIcon)); // Tint color (e.g., white)
                        int iconMargin = (itemView.getHeight() - checkIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + checkIcon.getIntrinsicHeight();
                        int iconLeft = itemView.getLeft() + 50;
                        int iconRight = iconLeft + checkIcon.getIntrinsicWidth();
                        checkIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        checkIcon.draw(c);
                    }
                } else if (dX < 0) {
                    int deleteColor = ContextCompat.getColor(YourBillScreen.this, R.color.backgroundDelete); // Replace with your color resource
                    paint.setColor(deleteColor);
                    // Draw the rounded rectangle
                    float cornerRadius = 12 * recyclerView.getContext().getResources().getDisplayMetrics().density; // Convert 12dp to pixels
                    RectF rectF = new RectF(
                            itemView.getRight() + dX, // Use dX for dynamic width
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom()
                    );
                    c.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
                    Drawable trashIcon = ContextCompat.getDrawable(YourBillScreen.this, R.drawable.delete_24px);
                    if (trashIcon != null) {
                        DrawableCompat.setTint(trashIcon, ContextCompat.getColor(YourBillScreen.this, R.color.backgroundDeleteIcon));
                        int iconMargin = (itemView.getHeight() - trashIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + trashIcon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - 50;
                        int iconLeft = iconRight - trashIcon.getIntrinsicWidth();
                        trashIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        trashIcon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void markAsPaid(int position) {
        // Update the item's status in your dataset
        Bill_model_billpage item = billList.get(position);
        item.setStatus("Paid");
        billAdapter.notifyItemChanged(position);
        updateStatusToPaidFromFirestore(position);
    }

//    Deletion
public void deleteItem(int position) {
    if (position >= 0 && position < billList.size()) {
        // Delete the item from Firestore using the position
        deleteFromFirestore(position);

        // Remove the item locally
        billList.remove(position);

        // Notify the adapter about the removed item
        billAdapter.notifyItemRemoved(position);
    }
}

private void updateStatusToPaidFromFirestore(int position){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference userDocs = db.collection("users").document(userID);

        userDocs.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                List<Map<String, Object>> billsArray = (List<Map<String, Object>>) documentSnapshot.get("bills");

                if (billsArray != null && position < billsArray.size()){
                    Map<String, Object> bill = billsArray.get(position);
                    bill.put("status","paid");

                    userDocs.update("bills", billsArray)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Status updated to Paid!"))
                            .addOnFailureListener(e -> Log.e("FirestoreError", "Error updating status: ", e));
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update!!", Toast.LENGTH_SHORT).show();
            Log.e("FirestoreError", "Error fetching document: ", e);
        });

}
    private void deleteFromFirestore(int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference the user's document
        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Fetch the bills array
                List<Map<String, Object>> billsArray = (List<Map<String, Object>>) documentSnapshot.get("bills");
                if (billsArray != null && position < billsArray.size()) {
                    // Remove the item at the specified position
                    billsArray.remove(position);

                    // Update Firestore with the modified array
                    userDocRef.update("bills", billsArray)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Bill deleted successfully!"))
                            .addOnFailureListener(e -> Log.e("FirestoreError", "Error deleting bill: ", e));

                }
                if(billsArray.size() == 0){
                    noBillsText.setText("No Bills Existed");
                    noBillsText.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Error fetching document: ", e);
        });
    }



}