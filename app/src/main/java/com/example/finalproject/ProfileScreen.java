package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileScreen extends AppCompatActivity {
    ImageButton home, add, bills, profile, logout, changeProf;

    String loggedUserId;
    private FirebaseAuth mAuth;
    private ImageView profilePic;
    private Uri filepath;

    private final int PICK_IMAGE_REQUEST = 22;
    private final int CAMERA_REQUEST_CODE = 100;

    private StorageReference storageRef;
    private FirebaseFirestore firestore;

    private static final String API_KEY = "b5df3157465eceaa2351eecae4aa3434";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";

    @SuppressLint("MissingInflatedId")
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

        changeProf = findViewById(R.id.imageButtonChangeImage);

        profilePic = findViewById(R.id.profilePicture);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        firestore = FirebaseFirestore.getInstance();

        loadProfileImage();

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

        changeProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

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

    boolean logOutofApp(){
        mAuth.signOut();
        return true;
    }

    public void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(
                intent, "upload image files here.."), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filepath = data.getData();
            try{
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(getContentResolver(), filepath);
                profilePic.setImageBitmap(bitmap);
                uploadImage(bitmap);
            }catch(IOException exception){
                exception.printStackTrace();
            }
        }
    }

    private void uploadImage(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
//            return;
//        } else {
//            Log.d("FirebaseAuth", "User is logged in with UID: " + currentUser.getUid());
//        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart("image", "profile.jpg", RequestBody.create(MediaType.parse("image/jpeg"), byteArray))
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileScreen.this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                    Log.e("ImgBB", "Error uploading image", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    String imageUrl = extractImageUrl(responseData);
                    runOnUiThread(() -> {
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            saveImageUriToFirestore(imageUrl);
                            Glide.with(ProfileScreen.this)
                                    .load(imageUrl)
                                    .override(200, 200)
                                    .centerCrop()
                                    .into(profilePic);
                            Toast.makeText(ProfileScreen.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileScreen.this, "Image URL is empty!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileScreen.this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                        Log.e("ImgBB", "Error response: " + response.message());
                    });
                }
            }
        });
    }

    private String extractImageUrl(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            if (jsonObject.getBoolean("success")) {
                return jsonObject.getJSONObject("data").getString("url");
            } else {
                Log.e("ImgBB", "Upload failed: " + jsonObject.getString("error"));
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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


    private void saveImageUriToFirestore(String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("profileImageUrl", imageUrl);

        firestore.collection("users")
                .document(userId)
                .set(userData, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Image URL saved successfully!");
                    // Save the image URL in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("profileImageUrl", imageUrl);
                    editor.apply();  // Save the changes
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving image URL", e));
    }

    private void loadProfileImage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
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
                                        .into(profilePic);
                            }
                        }
                    } else {
                        Log.e("Firestore", "Error getting user document", task.getException());
                    }
                });
    }
}
