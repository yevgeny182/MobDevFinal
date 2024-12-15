package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ImageButton add, bills, profile, home;
    String loggedUserId;
    TextView expensesCost;
    TextView expensesBillPaid;
    TextView expensesBillunsettled;
    ImageView profileImage;


    TextView tvUsername;

    private RecyclerView billRecyclerView;
    private TextView emptyTextView;
    private BillAdapter billAdapter;
    private List<Bill_model_homepage> billList = new ArrayList<>();

    FirebaseAuth auth = FirebaseAuth.getInstance();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        resetNotificationState(this);

        //bottom navigation
        add = findViewById(R.id.addButton);
        bills = findViewById(R.id.billButton);
        profile = findViewById(R.id.profileButton);
        home = findViewById(R.id.homeButton);
        profileImage = findViewById(R.id.profile_image);

        loadProfileImage();

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
        expensesBillPaid = findViewById(R.id.tvPaidBillValue);
        expensesBillunsettled = findViewById(R.id.tvUnsettledBillValue);

        getLoggedInUserData();


        billRecyclerView = findViewById(R.id.billRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);

//        getUserDataAndSetDefaults(loggedUserId);
        // Set up RecyclerView
        billRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        billAdapter = new BillAdapter(billList);
        billRecyclerView.setAdapter(billAdapter);


        fetchBills();
        sendUserNotification();


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

//                                // Safely fetch the fields, or use default values if they don't exist
//                                Long totalBillCost = document.contains("total_expenses") ? document.getLong("total_expenses") : 0L;
//                                Long paidBillCost = document.contains("paid_bills") ? document.getLong("paid_bills") : 0L;
//                                Long unsettledBillCost = document.contains("unsettled_bills") ? document.getLong("unsettled_bills") : 0L;
//
//// Convert to int if needed
//                                int totalBillCostInt = totalBillCost.intValue();
//                                int paidBillCostInt = paidBillCost.intValue();
//                                int unsettledBillCostInt = unsettledBillCost.intValue();
//
//// Update the TextViews
//                                expensesCost.setText(String.valueOf(totalBillCostInt));
//                                expensesBillPaid.setText(String.valueOf(paidBillCostInt));
//                                expensesBillunsettled.setText(String.valueOf(unsettledBillCostInt));

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

    private void fetchBills() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String loggedUserId = currentUser.getUid();

            // Reference to the user's document
            DocumentReference userDocRef = db.collection("users").document(loggedUserId);

            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // Get the "bills" array from the user document
                        List<Map<String, Object>> billsArray = (List<Map<String, Object>>) document.get("bills");

                        if (billsArray != null && !billsArray.isEmpty()) {

                            // Initialize counters and totals
                            double totalExpenses = 0.0;
                            double paidBillsCount = 0.0;
                            double unsettledBillsCount = 0.0;

                            // Clear the existing bill list
                            billList.clear();

                            // Process each bill
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                            Calendar currentCalendar = Calendar.getInstance();
                            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            currentCalendar.set(Calendar.MINUTE, 0);
                            currentCalendar.set(Calendar.SECOND, 0);
                            currentCalendar.set(Calendar.MILLISECOND, 0);

                            boolean isUpdated = false;

                            for (Map<String, Object> bill : billsArray) {
                                String amountStr = bill.get("Amount").toString();
                                double amount = Double.parseDouble(amountStr);

                                String category = bill.get("Category").toString();
                                String dueDate = bill.get("DueDate").toString();
                                String status = bill.get("status").toString();

                                // Add to total expenses
                                if ("unpaid".equalsIgnoreCase(status) || "unsettled".equalsIgnoreCase(status)) {
                                    totalExpenses += amount;
                                }

                                try {
                                    Date parsedDueDate = dateFormat.parse(dueDate);
                                    Calendar dueDateCalendar = Calendar.getInstance();

                                    if (parsedDueDate != null) {
                                        dueDateCalendar.setTime(parsedDueDate);
                                        dueDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                                        dueDateCalendar.set(Calendar.MINUTE, 0);
                                        dueDateCalendar.set(Calendar.SECOND, 0);
                                        dueDateCalendar.set(Calendar.MILLISECOND, 0);

                                        // Set status based on date comparison
                                        if (dueDateCalendar.before(currentCalendar) && !"paid".equalsIgnoreCase(status)) {
                                            bill.put("status", "unsettled");
                                            unsettledBillsCount += amount;
                                            isUpdated = true;
                                        }
                                    }
                                } catch (ParseException e) {
                                    // Log parsing errors to identify problematic data
                                    Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                }

                                // Increment counts based on status
                                if ("paid".equalsIgnoreCase(status)) {
                                    paidBillsCount += amount;
                                    Log.d("paidBillTotal", "Bills: " + paidBillsCount);
                                } else if ("unsettled".equalsIgnoreCase(status)) {
//                                    unsettledBillsCount += amount;
                                    Log.d("BillTotal", "Bills: " + unsettledBillsCount);
                                }

                                // Add to the RecyclerView list
                                billList.add(new Bill_model_homepage(String.valueOf(amount), category, dueDate));
                            }

                            // Update Firestore if any changes were made
                            if (isUpdated) {
                                userDocRef.update("bills", billsArray)
                                        .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Bills updated successfully"))
                                        .addOnFailureListener(e -> Log.e("FirestoreUpdateError", "Error updating bills: " + e.getMessage()));
                            }

                            // Notify the adapter
                            billAdapter.notifyDataSetChanged();

                            // Update TextViews
                            expensesCost.setText(String.format(Locale.getDefault(), "%.2f", totalExpenses));
                            expensesBillPaid.setText(String.valueOf(paidBillsCount));
                            expensesBillunsettled.setText(String.valueOf(unsettledBillsCount));

                            // Commented out updating Firestore totals directly for now
//                        Map<String, Object> updates = new HashMap<>();
//                        updates.put("total_expenses", totalExpenses);
//                        updates.put("paid_bills", paidBillsCount);
//                        updates.put("unsettled_bills", unsettledBillsCount);
//
//                        userDocRef.update(updates)
//                                .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Totals updated successfully"))
//                                .addOnFailureListener(e -> Log.e("FirestoreUpdateError", "Error updating totals: " + e.getMessage()));

                            // Show RecyclerView and hide empty message
                            billRecyclerView.setVisibility(View.VISIBLE);
                            emptyTextView.setVisibility(View.GONE);
                        } else {
                            // No bills found
                            Log.d("FirestoreBills", "No bills found for user.");
                            billRecyclerView.setVisibility(View.GONE);
                            emptyTextView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w("FirestoreUser", "User document does not exist.");
                        billRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Log Firestore fetch error
                    Log.e("FirestoreError", "Error fetching user document: " + task.getException().getMessage());
                    billRecyclerView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // Log missing user session
            Log.w("UserWarning", "No user is currently logged in.");
            billRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }


    private void loadProfileImage() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String imageUrl = document.getString("profileImageUrl");
                            if (imageUrl != null) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.avatar)
                                        .error(R.drawable.avatar)
                                        .into(profileImage);
                            }
                        }
                    } else {
                        Log.e("Firestore", "Error getting user document", task.getException());
                    }
                });
    }

    private void sendUserNotification() {
        /*
        This method sends notification to the user's phone,
        specs: 5 days before the due date, notify
               bill is on the exact due date, notify
               bill is past due, notify
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String loggedUserId = currentUser.getUid();

            DocumentReference userDocRef = db.collection("users").document(loggedUserId);

            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {

                        List<Map<String, Object>> billsArray = (List<Map<String, Object>>) document.get("bills");

                        if (billsArray != null && !billsArray.isEmpty()) {

                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                            Calendar currentCalendar = Calendar.getInstance();
                            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            currentCalendar.set(Calendar.MINUTE, 0);
                            currentCalendar.set(Calendar.SECOND, 0);
                            currentCalendar.set(Calendar.MILLISECOND, 0);

                            SharedPreferences preferences = getBaseContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);

                            boolean upcomingNotificationDisplayed = preferences.getBoolean("upcomingNotificationDisplayed", false);
                            boolean dueTodayNotificationDisplayed = preferences.getBoolean("dueTodayNotificationDisplayed", false);
                            boolean overdueNotificationDisplayed = preferences.getBoolean("overdueNotificationDisplayed", false);

                            boolean showUpcoming = false;
                            boolean showDueToday = false;
                            boolean showOverdue = false;

                            for (Map<String, Object> bill : billsArray) {
                                String dueDate = bill.get("DueDate").toString();
                                Log.d("notifDate", "duedate is: " + dueDate);

                                try {
                                    Date billDueDate = dateFormat.parse(dueDate);
                                    if (billDueDate != null) {
                                        Calendar billCalendar = Calendar.getInstance();
                                        billCalendar.setTime(billDueDate);

                                        long diffDays = (billCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis()) / (24 * 60 * 60 * 1000);

                                        if (diffDays == 5 && !upcomingNotificationDisplayed) {
                                            showUpcoming = true;
                                        } else if (diffDays == 0 && !dueTodayNotificationDisplayed) {
                                            showDueToday = true;
                                        } else if (diffDays < 0 && !overdueNotificationDisplayed) {
                                            showOverdue = true;
                                        }
                                    }

                                } catch (ParseException e) {
                                    Log.e("notifDate", "Invalid date format: " + e.getMessage());
                                }
                            }

                            if (showUpcoming) {
                                showNotification("Upcoming Bill", "Your bill is due in 5 days.");
                                upcomingNotificationDisplayed = true; // Update state after showing notification
                            }
                            if (showDueToday) {
                                showNotification("Bill Due", "Your bill is due today.");
                                dueTodayNotificationDisplayed = true; // Update state after showing notification
                            }
                            if (showOverdue) {
                                showNotification("Overdue Bill", "Your bill is overdue!");
                                overdueNotificationDisplayed = true; // Update state after showing notification
                            }

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("upcomingNotificationDisplayed", upcomingNotificationDisplayed);
                            editor.putBoolean("dueTodayNotificationDisplayed", dueTodayNotificationDisplayed);
                            editor.putBoolean("overdueNotificationDisplayed", overdueNotificationDisplayed);
                            editor.apply();

                        } else {
                            // No bills found
                            Log.d("notif", "No bills found for user.");
                            billRecyclerView.setVisibility(View.GONE);
                            emptyTextView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w("notif", "User document does not exist.");
                        billRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            Log.w("UserWarning", "No user is currently logged in.");
            billRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }

    }

    private void showNotification(String title, String message) {
        if (shouldShowRequestPermissionRationale("android.permission.POST_NOTIFICATIONS")) {
            new AlertDialog.Builder(this)
                    .setTitle("Notification Permission Required")
                    .setMessage("This app requires notification permission to alert you about important updates.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
                return; // Exit until permission is granted
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notify = new NotificationChannel(
                    "bill_notify",
                    "Bill Notify",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notify);
            }
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, "bill_notify")
                .setSmallIcon(R.drawable.spend_insight_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notifManager = NotificationManagerCompat.from(this);
        notifManager.notify((int) System.currentTimeMillis(), notifBuilder.build());
    }
    public void resetNotificationState(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("upcomingNotificationDisplayed", false);
        editor.putBoolean("dueTodayNotificationDisplayed", false);
        editor.putBoolean("overdueNotificationDisplayed", false);
        editor.apply();
    }


}

