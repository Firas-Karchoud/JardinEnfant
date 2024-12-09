package com.example.jardinenfantmobile.Student;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.example.jardinenfantmobile.R;
import com.example.jardinenfantmobile.user.UserProfileActivity;
import com.example.jardinenfantmobile.classes.ClassListActivity;
import com.example.jardinenfantmobile.events.AdminEventsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class StudentsListActivity extends AppCompatActivity {
    private Button generatePdfButton, showStatisticsButton;
    private RecyclerView studentsRecyclerView;
    private StudentsAdapter studentsAdapter;
    private ArrayList<Student> studentsList;
    private ArrayList<Student> filteredList; // List for gender and class-based filtering
    private DatabaseReference studentsRef, usersRef, classesRef;
    private FloatingActionButton addStudentButton;
    private String userRole;
    private String userId;
    private Spinner genderFilterSpinner, classFilterSpinner;
    private ArrayList<String> classNames = new ArrayList<>();
    private ArrayList<String> classIds = new ArrayList<>(); // To store class IDs for filtering
    private Map<String, String> classIdToNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_students);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Registered Users");
        studentsRef = FirebaseDatabase.getInstance().getReference("students");
        classesRef = FirebaseDatabase.getInstance().getReference("classes");

        generatePdfButton = findViewById(R.id.generatePdfButton);
        generatePdfButton.setOnClickListener(v -> generatePDF(studentsList));

        showStatisticsButton = findViewById(R.id.showStatisticsButton);
        showStatisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentsListActivity.this, StudentsStatisticsActivity.class);
            startActivity(intent);
        });

        addStudentButton = findViewById(R.id.addStudentButton);
        addStudentButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentsListActivity.this, CreateStudentActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
        setupRecyclerView();
        setupGenderFilter(); // Initialize gender filter
        setupClassFilter(); // Initialize class filter
        fetchUserRoleAndLoadStudents();
        loadClassNames();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.navigation_home) {
                intent = new Intent(StudentsListActivity.this, UserProfileActivity.class);
            } else if (id == R.id.navigation_events) {
                intent = new Intent(StudentsListActivity.this, AdminEventsActivity.class);
            } else if (id == R.id.navigation_students) {
                intent = new Intent(StudentsListActivity.this, StudentsListActivity.class);
            } else if (id == R.id.navigation_classes) {
                intent = new Intent(StudentsListActivity.this, ClassListActivity.class);
            }
            if (intent != null) startActivity(intent);
            return true;
        });
    }

    private void setupGenderFilter() {
        genderFilterSpinner = findViewById(R.id.genderFilterSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderFilterSpinner.setAdapter(adapter);

        genderFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGender = genderFilterSpinner.getSelectedItem() != null
                        ? genderFilterSpinner.getSelectedItem().toString()
                        : "All";
                filterByClassAndGender(classFilterSpinner.getSelectedItem() != null
                        ? classFilterSpinner.getSelectedItem().toString()
                        : "All", selectedGender);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterByClassAndGender(classFilterSpinner.getSelectedItem() != null
                        ? classFilterSpinner.getSelectedItem().toString()
                        : "All", "All");
            }
        });
    }

    private void setupClassFilter() {
        classFilterSpinner = findViewById(R.id.classFilterSpinner);

        classesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classNames.clear();
                classIds.clear();
                classNames.add("All");
                classIds.add("All");
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.getKey();
                    String className = classSnapshot.child("name").getValue(String.class);

                    if (classId != null && className != null) {
                        classIds.add(classId);
                        classNames.add(className);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(StudentsListActivity.this,
                        android.R.layout.simple_spinner_item, classNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classFilterSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentsListActivity", "Failed to load classes: " + error.getMessage());
            }
        });

        classFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClassId = classIds.get(position);
                filterByClassAndGender(selectedClassId, genderFilterSpinner.getSelectedItem() != null
                        ? genderFilterSpinner.getSelectedItem().toString()
                        : "All");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterByClassAndGender("All", genderFilterSpinner.getSelectedItem() != null
                        ? genderFilterSpinner.getSelectedItem().toString()
                        : "All");
            }
        });
    }

    private void filterByClassAndGender(String classId, String gender) {
        filteredList.clear();

        for (Student student : studentsList) {
            String studentGender = student.getGender() != null ? student.getGender() : "";
            String studentClassId = student.getclassId() != null ? student.getclassId() : "";

            boolean matchesGender = gender.equals("All") || studentGender.equalsIgnoreCase(gender);
            boolean matchesClass = classId.equals("All") || studentClassId.equals(classId);

            if (matchesGender && matchesClass) {
                filteredList.add(student);
            }
        }

        studentsAdapter.updateFullList(filteredList);
        studentsAdapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        studentsAdapter = new StudentsAdapter(filteredList, student -> {
            Intent intent = new Intent(StudentsListActivity.this, StudentDetailsActivity.class);
            intent.putExtra("studentId", student.getId());
            startActivity(intent);
        }, this);
        studentsRecyclerView.setAdapter(studentsAdapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                studentsAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                studentsAdapter.filter(newText);
                return false;
            }
        });
    }

    private void fetchUserRoleAndLoadStudents() {
        usersRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRole = snapshot.getValue(String.class);
                if (userRole != null) {
                    if ("admin".equals(userRole)) {
                        generatePdfButton.setVisibility(View.VISIBLE);
                        showStatisticsButton.setVisibility(View.VISIBLE);
                    } else {
                        generatePdfButton.setVisibility(View.GONE);
                        showStatisticsButton.setVisibility(View.GONE);
                    }
                    loadStudentsBasedOnRole();
                } else {
                    Log.e("StudentsListActivity", "User role not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentsListActivity", "Failed to retrieve user role: " + error.getMessage());
            }
        });
    }

    private void loadStudentsBasedOnRole() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Student student = dataSnapshot.getValue(Student.class);
                    if (student != null) {
                        String parentId = student.getparent_id();
                        if ("admin".equals(userRole) || ("client".equals(userRole) && parentId != null && parentId.equals(userId))) {
                            studentsList.add(student);
                        }
                    }
                }
                filterByClassAndGender(
                        classFilterSpinner.getSelectedItem() != null ? classFilterSpinner.getSelectedItem().toString() : "All",
                        genderFilterSpinner.getSelectedItem() != null ? genderFilterSpinner.getSelectedItem().toString() : "All"
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentsListActivity", "Database error: " + error.getMessage());
            }
        });
    }

    private void loadClassNames() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classIdToNameMap.clear();
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.getKey();
                    String className = classSnapshot.child("name").getValue(String.class);
                    if (classId != null && className != null) {
                        classIdToNameMap.put(classId, className);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentsListActivity", "Failed to load class names: " + error.getMessage());
            }
        });
    }

    public void generatePDF(List<Student> studentsList) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, 800, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(14);

        int yPosition = 40;

        // Title
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText("Student List", 130, yPosition, paint);
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        yPosition += 40;

        for (Student student : studentsList) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(10, yPosition - 20, 380, yPosition + 100, paint);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawText("Name: " + student.getFirstName() + " " + student.getLastName(), 20, yPosition, paint);
            yPosition += 25;

            String ageOrBirthDate = "Birth Date: " + student.getBirthDate();
            String gender = "Gender: " + student.getGender();
            String className = "Class: " + (classIdToNameMap.containsKey(student.getclassId())
                    ? classIdToNameMap.get(student.getclassId()) : "Unknown");
            String parentId = "Parent ID: " + (student.getparent_id() != null ? student.getparent_id() : "N/A");

            canvas.drawText(ageOrBirthDate, 20, yPosition, paint);
            yPosition += 25;
            canvas.drawText(gender, 20, yPosition, paint);
            yPosition += 25;
            canvas.drawText(className, 20, yPosition, paint);
            yPosition += 25;
            canvas.drawText(parentId, 20, yPosition, paint);
            yPosition += 35;

            paint.setColor(0xFFCCCCCC);
            canvas.drawLine(10, yPosition, 380, yPosition, paint);
            paint.setColor(0xFF000000);
            yPosition += 15;

            if (yPosition > 750) {
                pdfDocument.finishPage(page);
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = 40;
            }
        }

        pdfDocument.finishPage(page);

        File filePath = new File(getFilesDir(), "StudentList.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "PDF generated at: " + filePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}
