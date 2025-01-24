package com.example.pingmate;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
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
    private String profileImageUrl;
    private ImageView profileImageView, addProfilePicture;
    private static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);


        FirebaseApp.initializeApp(this);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImageView = findViewById(R.id.ivProfilePicture);
        addProfilePicture = findViewById(R.id.ivAddProfilePicture);


        spinnerStatus = findViewById(R.id.spinnerStatus);
        TextView tvStatus = findViewById(R.id.tvStatus);
        editText = findViewById(R.id.pfp_Edit_username);
        userInfo = findViewById(R.id.userInfo);
        String[] statusOptions = {"Online", "Offline", "Busy", "Do Not Disturb"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        profileImageView.setOnClickListener(v -> showFullImageDialog());

        addProfilePicture.setOnClickListener(v -> openFileChooser());

        S3ClientManager.initialize(
                this,
                "AKIAXTORPPDQTN2Z6XFC",
                "kgKBIgXLwLZXfBE18mZTS614l9rgAx/l6WTnabtZ",
                "ap-southeast-2"
        );


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
                        profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        // Debugging logs
                        Log.d("ProfileEditActivity", "Fetched Profile Image URL: " + profileImageUrl);

                        // Load the data into the UI
                        editText.setText(username);
                        if (status != null) {
                            int spinnerPosition = ((ArrayAdapter<String>) spinnerStatus.getAdapter()).getPosition(status);
                            spinnerStatus.setSelection(spinnerPosition);
                        }
                        if (profileImageUrl != null) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);

                            // Add click listener for full image view
                            profileImageView.setOnClickListener(v -> showFullImageDialog());
                        } else {
                            Log.e("ProfileEditActivity", "Profile Image URL is null.");
                        }
                    } else {
                        Log.e("ProfileEditActivity", "User document does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileEditActivity", "Error fetching user data: " + e.getMessage()));
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
            uploadImageToS3(imageUri);
        }

    }

    private void uploadImageToS3(Uri imageUri) {
        if (imageUri != null) {
            try {
                // Convert URI to File
                String userId = firebaseAuth.getCurrentUser().getUid();
                String fileName = "profile_images/" + userId + ".jpg";
                File file = new File(getCacheDir(), "tempImage.jpg");
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

                // Upload to S3
                TransferUtility transferUtility = S3ClientManager.getTransferUtility();
                TransferObserver uploadObserver = transferUtility.upload(
                        "nu-pingmate",
                        fileName,
                        file
                );

                // Add listeners for upload progress
                uploadObserver.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state == TransferState.COMPLETED) {
                            Toast.makeText(ProfileEditActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                            String imageUrl = "https://nu-pingmate.s3.amazonaws.com/" + fileName;
                            updateProfileImageUrl(imageUrl);
                        } else if (state == TransferState.FAILED) {
                            Toast.makeText(ProfileEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        double progress = (double) bytesCurrent / bytesTotal * 100;
                        Log.d("S3 Upload", "Progress: " + progress + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("S3 Upload", "Error: " + ex.getMessage());
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ProfileEditActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }




    private void updateProfileImageUrl(String imageUrl) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    profileImageUrl = imageUrl; // Update the global variable
                    Toast.makeText(ProfileEditActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Error updating profile image", Toast.LENGTH_SHORT).show());
    }
    private void showFullImageDialog() {
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_full_image, null);
            builder.setView(dialogView);

            ImageView fullImageView = dialogView.findViewById(R.id.fullImageView);
            Button closeButton = dialogView.findViewById(R.id.closeButton);

            // Load the image using Glide
            Glide.with(this)
                    .load(profileImageUrl)
                    .into(fullImageView);

            AlertDialog dialog = builder.create();
            closeButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } else {
            Toast.makeText(this, "No profile image found", Toast.LENGTH_SHORT).show();
            Log.e("ProfileEditActivity", "Cannot show full image - profileImageUrl is null or empty.");
        }
    }




}




