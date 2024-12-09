package com.example.jardinenfantmobile.Student;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jardinenfantmobile.R;
import com.example.jardinenfantmobile.user.ReadWriteUserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentDetailsActivity extends AppCompatActivity {

    private TextView studentName, studentBirthDate, studentGender, studentClass, studentParentName;
    private ImageView studentImageView;
    private Button editStudentButton, deleteStudentButton;
    private DatabaseReference studentRef, classesRef, usersRef;
    private String studentId, classId, parentId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        studentName = findViewById(R.id.studentName);
        studentBirthDate = findViewById(R.id.studentBirthDate);
        studentGender = findViewById(R.id.studentGender);
        studentClass = findViewById(R.id.studentClass);
        studentParentName = findViewById(R.id.studentParentName);
        studentImageView = findViewById(R.id.studentImageView);
        editStudentButton = findViewById(R.id.editStudentButton);
        deleteStudentButton = findViewById(R.id.deleteStudentButton);

        // Get student ID from Intent
        studentId = getIntent().getStringExtra("studentId");

        // Firebase references
        studentRef = FirebaseDatabase.getInstance().getReference("students").child(studentId);
        classesRef = FirebaseDatabase.getInstance().getReference("classes");
        usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check user role to set button visibility
        checkUserRole();

        // Load student data
        loadStudentData();

        // Set up edit button
        editStudentButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDetailsActivity.this, UpdateStudentActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        // Set up delete button
        deleteStudentButton.setOnClickListener(v -> deleteStudent());
    }

    private void checkUserRole() {
        usersRef.child(currentUserId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if ("admin".equals(role)) {
                    // Show edit and delete buttons if the user is an admin
                    editStudentButton.setVisibility(View.VISIBLE);
                    deleteStudentButton.setVisibility(View.VISIBLE);
                } else {
                    // Hide edit and delete buttons for non-admin users
                    editStudentButton.setVisibility(View.GONE);
                    deleteStudentButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDetailsActivity.this, "Failed to check user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentData() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String birthDate = snapshot.child("birthDate").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    classId = snapshot.child("classId").getValue(String.class);
                    parentId = snapshot.child("parent_id").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Display data in TextViews and ImageView
                    studentName.setText(firstName + " " + lastName);
                    studentBirthDate.setText(birthDate);
                    studentGender.setText(gender);
                    loadClassName(classId);
                    loadParentName(parentId);
                    if (imageUrl != null) {
                        Glide.with(StudentDetailsActivity.this).load(Uri.parse(imageUrl)).into(studentImageView);
                    } else {
                        studentImageView.setImageResource(R.drawable.default_image);
                    }
                } else {
                    Toast.makeText(StudentDetailsActivity.this, "Student data not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDetailsActivity.this, "Failed to load student data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClassName(String classId) {
        if (classId != null) {
            classesRef.child(classId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String className = snapshot.child("name").getValue(String.class);
                    studentClass.setText(className != null ? className : "Class not found");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    studentClass.setText("Error loading class");
                }
            });
        } else {
            studentClass.setText("No class assigned");
        }
    }

    private void loadParentName(String parentId) {
        if (parentId != null) {
            usersRef.child(parentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String parentName = snapshot.child("name").getValue(String.class);
                    studentParentName.setText(parentName != null ? parentName : "Parent ID: " + parentId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    studentParentName.setText("Error loading parent");
                }
            });
        } else {
            studentParentName.setText("No parent assigned");
        }
    }

    private void deleteStudent() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    studentRef.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(StudentDetailsActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e ->
                            Toast.makeText(StudentDetailsActivity.this, "Failed to delete student", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
