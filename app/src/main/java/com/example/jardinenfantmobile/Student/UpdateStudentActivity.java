package com.example.jardinenfantmobile.Student;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jardinenfantmobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateStudentActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText firstNameInput, lastNameInput, birthDateInput;
    private Spinner genderSpinner, classSpinner, clientSpinner;
    private ImageView studentImageView;
    private Button selectImageButton, updateStudentButton;
    private Uri imageUri;
    private DatabaseReference studentRef, classesRef, usersRef;
    private StorageReference storageRef;
    private String studentId, selectedClassId, selectedClientId, userRole;
    private List<String> classNames = new ArrayList<>();
    private List<String> classIds = new ArrayList<>();
    private List<String> clientIds = new ArrayList<>();
    private ArrayAdapter<String> clientAdapter;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_student);


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
        clientSpinner = findViewById(R.id.clientSpinner); // Client spinner for selecting parent_id
        studentImageView = findViewById(R.id.studentImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        updateStudentButton = findViewById(R.id.updateStudentButton);

        // Get the student ID from the intent
        studentId = getIntent().getStringExtra("studentId");

        // Firebase references
        studentRef = FirebaseDatabase.getInstance().getReference("students").child(studentId);
        classesRef = FirebaseDatabase.getInstance().getReference("classes");
        usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");
        storageRef = FirebaseStorage.getInstance().getReference("student_images");
        auth = FirebaseAuth.getInstance();

        // Load existing student data
        loadStudentData();

        // Set up gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Load classes into class spinner
        loadClasses();
        checkUserRole();

        birthDateInput.setOnClickListener(v -> showDatePickerDialog());

        // Set up the select image button
        selectImageButton.setOnClickListener(v -> openFileChooser());



        // Set up the update student button
        updateStudentButton.setOnClickListener(v -> updateStudent());

    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
                Toast.makeText(UpdateStudentActivity.this, "Failed to check user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClientIds() {
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
                Toast.makeText(UpdateStudentActivity.this, "Failed to load clients", Toast.LENGTH_SHORT).show();
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

    private void loadStudentData() {
        studentRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String firstName = dataSnapshot.child("firstName").getValue(String.class);
                String lastName = dataSnapshot.child("lastName").getValue(String.class);
                String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                String gender = dataSnapshot.child("gender").getValue(String.class);
                String imageUrl = dataSnapshot.child("image").getValue(String.class);
                selectedClassId = dataSnapshot.child("classId").getValue(String.class);
                selectedClientId = dataSnapshot.child("parent_id").getValue(String.class);

                firstNameInput.setText(firstName);
                lastNameInput.setText(lastName);
                birthDateInput.setText(birthDate);

                if (gender != null) {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) genderSpinner.getAdapter();
                    int position = adapter.getPosition(gender);
                    genderSpinner.setSelection(position);
                }

                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).into(studentImageView);
                } else {
                    studentImageView.setImageResource(R.drawable.default_image);
                }

                setSelectedClassInSpinner();
                setSelectedClientInSpinner();
            }
        }).addOnFailureListener(e -> Toast.makeText(UpdateStudentActivity.this, "Failed to load student data", Toast.LENGTH_SHORT).show());
    }

    private void setSelectedClassInSpinner() {
        for (int i = 0; i < classIds.size(); i++) {
            if (classIds.get(i).equals(selectedClassId)) {
                classSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setSelectedClientInSpinner() {
        if (selectedClientId != null) {
            for (int i = 0; i < clientIds.size(); i++) {
                if (clientIds.get(i).equals(selectedClientId)) {
                    clientSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void loadClasses() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classNames.clear();
                classIds.clear();
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.getKey();
                    String className = classSnapshot.child("name").getValue(String.class);
                    if (classId != null && className != null) {
                        classIds.add(classId);
                        classNames.add(className);
                    }
                }
                ArrayAdapter<String> classAdapter = new ArrayAdapter<>(UpdateStudentActivity.this, android.R.layout.simple_spinner_item, classNames);
                classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classSpinner.setAdapter(classAdapter);

                setSelectedClassInSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateStudentActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudent() {
        String firstName = firstNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();
        String birthDate = birthDateInput.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String selectedClassName = classSpinner.getSelectedItem().toString();
        int selectedClassIndex = classNames.indexOf(selectedClassName);
        selectedClassId = classIds.get(selectedClassIndex);

        String parentId = "admin".equals(userRole) && selectedClientId != null ? selectedClientId : auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("birthDate", birthDate);
        updates.put("gender", gender);
        updates.put("classId", selectedClassId);
        updates.put("parent_id", parentId);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(studentId + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("image", uri.toString());
                        saveUpdates(updates);
                    })
            ).addOnFailureListener(e -> Toast.makeText(UpdateStudentActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            saveUpdates(updates);
        }
    }

    private void saveUpdates(Map<String, Object> updates) {
        studentRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UpdateStudentActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    redirectToStudentsList();
                })
                .addOnFailureListener(e -> Toast.makeText(UpdateStudentActivity.this, "Failed to update student", Toast.LENGTH_SHORT).show());
    }

    private void redirectToStudentsList() {
        Intent intent = new Intent(UpdateStudentActivity.this, StudentsListActivity.class);
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
