package com.example.pingmate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);

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


    private void fetchUserdata(){
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        String username = documentSnapshot.getString("username");
                        String status = documentSnapshot.getString("status");
                        String firstName = documentSnapshot.getString("first name");
                        String lastName = documentSnapshot.getString("last name");



                        editText.setText(username);
                        if (status !=  null){
                            int spinnerPosition = ((ArrayAdapter<String>) spinnerStatus. getAdapter()).getPosition(status);
                            spinnerStatus.setSelection(spinnerPosition);
                        }
                        String userInfoText = "name: " + firstName + "" + lastName + "\nUsername: " + username + "\nstatus: " + status;
                        userInfo.setText(userInfoText);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }

    private void updateProfile() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        String newUsername = editText.getText().toString().trim();

        if (newUsername.isEmpty()){
            editText.setError("Username is Required");
            editText.requestFocus();
            return;
        }

        db.collection("users").document(uid)
                .update("status", selectedStatus, "username", newUsername)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileEditActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileEditActivity.this, "Error updating status", Toast.LENGTH_SHORT).show());
    }

}
