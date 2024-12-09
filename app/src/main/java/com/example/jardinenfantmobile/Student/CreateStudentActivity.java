package com.example.jardinenfantmobile.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.jardinenfantmobile.R;
import com.example.jardinenfantmobile.user.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateStudentActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText firstNameInput, lastNameInput, birthDateInput;
    private Spinner genderSpinner, classSpinner, clientSpinner;
    private ImageView studentImageView;
    private Button selectImageButton, createStudentButton;
    private Uri imageUri;
    private DatabaseReference studentsRef, classRef, usersRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> classAdapter, clientAdapter;
    private List<String> classNames = new ArrayList<>();
    private List<String> classIds = new ArrayList<>();
    private List<String> clientIds = new ArrayList<>();
    private String selectedClassId, selectedClientId, userRole;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_student);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        birthDateInput = findViewById(R.id.birthDateInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        classSpinner = findViewById(R.id.classSpinner);
        clientSpinner = findViewById(R.id.clientSpinner); // Spinner for selecting client
        studentImageView = findViewById(R.id.studentImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        createStudentButton = findViewById(R.id.createStudentButton);

        // Set up Firebase references
        studentsRef = FirebaseDatabase.getInstance().getReference("students");
        storageRef = FirebaseStorage.getInstance().getReference("student_images");
        classRef = FirebaseDatabase.getInstance().getReference("classes");
        usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");
        auth = FirebaseAuth.getInstance();

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Set up class spinner
        classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classNames);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        loadClasses();
        checkUserRole();

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClassId = classIds.get(position); // Set the selected class ID
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedClassId = null;
            }
        });

        // Set up Date Picker for birth date input
        birthDateInput.setOnClickListener(v -> showDatePickerDialog());

        // Set up select image button
        selectImageButton.setOnClickListener(v -> openFileChooser());

        // Set up create student button
        createStudentButton.setOnClickListener(v -> createStudent());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            studentImageView.setImageURI(imageUri);
        }
    }

    private void loadClasses() {
        classRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classNames.clear();
                classIds.clear();
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.getKey();
                    String className = classSnapshot.child("name").getValue(String.class);
                    classIds.add(classId);
                    classNames.add(className);
                }
                classAdapter.notifyDataSetChanged(); // Refresh the spinner data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateStudentActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        String currentUserId = auth.getCurrentUser().getUid();
        usersRef.child(currentUserId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRole = snapshot.getValue(String.class);
                if ("admin".equals(userRole)) {
                    clientSpinner.setVisibility(View.VISIBLE); // Show client dropdown for admin
                    loadClientIds();
                } else {
                    clientSpinner.setVisibility(View.GONE); // Hide client dropdown for regular users
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateStudentActivity.this, "Failed to check user role", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadClientIds() {
        clientIds.clear();
        clientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientIds);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clientSpinner.setAdapter(clientAdapter);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clientIds.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("client".equals(role)) {
                        String clientId = userSnapshot.getKey();
                        clientIds.add(clientId);
                    }
                }
                clientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateStudentActivity.this, "Failed to load clients", Toast.LENGTH_SHORT).show();
            }
        });

        clientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClientId = clientIds.get(position); // Selected client ID for admin
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedClientId = null;
            }
        });
    }

    private void createStudent() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(CreateStudentActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            Toast.makeText(this, "Please log in to create a student", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = "admin".equals(userRole) && selectedClientId != null ? selectedClientId : auth.getCurrentUser().getUid();

        String firstName = firstNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();
        String birthDate = birthDateInput.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty() || gender.isEmpty() || selectedClassId == null) {
            Toast.makeText(this, "Please fill all required fields and select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = studentsRef.push().getKey();
        Map<String, Object> studentDetails = new HashMap<>();
        studentDetails.put("id", studentId);
        studentDetails.put("firstName", firstName);
        studentDetails.put("lastName", lastName);
        studentDetails.put("birthDate", birthDate);
        studentDetails.put("gender", gender);
        studentDetails.put("classId", selectedClassId);
        studentDetails.put("parent_id", userId);

        if (imageUri != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Image...");
            progressDialog.show();

            String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA).format(new Date());
            StorageReference fileRef = storageRef.child("images/" + fileName);

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        studentDetails.put("imageUrl", uri.toString());
                        saveStudentDetails(studentId, studentDetails);
                        progressDialog.dismiss();
                        Toast.makeText(CreateStudentActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.e("CreateStudent", "Image upload failed", e);
                        Toast.makeText(CreateStudentActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveStudentDetails(studentId, studentDetails);
        }
    }

    private void saveStudentDetails(String studentId, Map<String, Object> studentDetails) {
        studentsRef.child(studentId).setValue(studentDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateStudentActivity.this, "Student created successfully", Toast.LENGTH_SHORT).show();
                    redirectToStudentsList();
                })
                .addOnFailureListener(e -> Toast.makeText(CreateStudentActivity.this, "Failed to create student", Toast.LENGTH_SHORT).show());
    }

    private void redirectToStudentsList() {
        Intent intent = new Intent(CreateStudentActivity.this, StudentsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    birthDateInput.setText(date);
                }, year, month, day);

        datePickerDialog.show();
    }
}
