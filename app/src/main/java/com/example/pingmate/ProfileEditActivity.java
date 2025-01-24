package com.example.pingmate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class ProfileEditActivity extends Activity {
    private Button Backbutton, Updatebutton;
    private EditText editText;
    private Spinner spinnerStatus;
    private TextView userInfo;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String selectedStatus;
    private ImageView profileImageView;
    private static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);


        FirebaseApp.initializeApp(this);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImageView = findViewById(R.id.ivProfilePicture);

        spinnerStatus = findViewById(R.id.spinnerStatus);
        TextView tvStatus = findViewById(R.id.tvStatus);
        editText = findViewById(R.id.pfp_Edit_username);
        userInfo = findViewById(R.id.userInfo);
        String[] statusOptions = {"Online", "Offline", "Busy", "Do Not Disturb"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
        profileImageView.setOnClickListener(v -> openFileChooser());

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = statusOptions[position];
                tvStatus.setText(selectedStatus);
                switch (selectedStatus) {
                    case "Online":
                        tvStatus.setBackgroundColor(Color.GREEN);
                        break;
                    case "Offline":
                        tvStatus.setBackgroundColor(Color.GRAY);
                        break;
                    case "Busy":
                        tvStatus.setBackgroundColor(Color.YELLOW);
                        break;
                    case "Do Not Disturb":
                        tvStatus.setBackgroundColor(Color.RED);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        Backbutton = findViewById(R.id.btnBack);
        Backbutton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, homepage.class);
            startActivity(intent);
        });

        Updatebutton = findViewById(R.id.updateBtn);
        Updatebutton.setOnClickListener(view -> updateProfile());


        fetchUserdata();
    }


    private void fetchUserdata() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String status = documentSnapshot.getString("status");
                        String firstName = documentSnapshot.getString("first name");
                        String lastName = documentSnapshot.getString("last name");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");


                        editText.setText(username);
                        if (status != null) {
                            int spinnerPosition = ((ArrayAdapter<String>) spinnerStatus.getAdapter()).getPosition(status);
                            spinnerStatus.setSelection(spinnerPosition);
                        }
                        String userInfoText = "name: " + firstName + " " + lastName + "\nUsername: " + username + "\nstatus: " + status;
                        userInfo.setText(userInfoText);
                        // Load the profile image
                        if (profileImageUrl != null) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);
                        }
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }

    private void updateProfile() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        String newUsername = editText.getText().toString().trim();

        if (newUsername.isEmpty()) {
            editText.setError("Username is Required");
            editText.requestFocus();
            return;
        }

        db.collection("users").document(uid)
                .update("status", selectedStatus, "username", newUsername)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileEditActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Error updating status", Toast.LENGTH_SHORT).show());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }

    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Get the current user's ID
            String userId = firebaseAuth.getCurrentUser ().getUid();

            // Create a reference to the storage location
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            Log.d("Firebase Storage", "Storage instance: " + FirebaseStorage.getInstance().toString());
            StorageReference fileReference = storageRef.child("profile_images/" + userId + ".jpg");


            // Log the upload path for debugging
            Log.d("Upload Path", "Uploading to: " + fileReference.getPath());

            // Start the upload
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("Upload", "Upload successful");
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateProfileImageUrl(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof StorageException) {
                            StorageException se = (StorageException) e;
                            Log.e("Storage Error", "Error Code: " + se.getErrorCode());
                        }
                        Log.e("Upload Error", "Upload failed: " + e.getMessage());
                        Toast.makeText(ProfileEditActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            Toast.makeText(ProfileEditActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateProfileImageUrl(String imageUrl) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileEditActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Error updating profile image", Toast.LENGTH_SHORT).show());
    }
}




