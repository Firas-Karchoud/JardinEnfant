package com.example.jardinenfantmobile.Student;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jardinenfantmobile.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentsStatisticsActivity extends AppCompatActivity {

    private BarChart classBarChart;
    private PieChart genderPieChart;
    private DatabaseReference studentsRef;
    private DatabaseReference classesRef;
    private Map<String, String> classIdToNameMap = new HashMap<>();
    private Map<String, Integer> classData = new HashMap<>();
    private Map<String, Integer> genderData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_statistics);

        classBarChart = findViewById(R.id.classBarChart);
        genderPieChart = findViewById(R.id.genderPieChart);

        // Initialize Firebase references
        studentsRef = FirebaseDatabase.getInstance().getReference("students");
        classesRef = FirebaseDatabase.getInstance().getReference("classes");

        // Load class names and then load student data
        loadClassNames();
    }

    private void loadClassNames() {
        classesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String classId = classSnapshot.getKey();
                    String className = classSnapshot.child("name").getValue(String.class);

                    if (classId != null && className != null) {
                        classIdToNameMap.put(classId, className);
                    }
                }
                // After loading class names, load student data
                loadStudentData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentsStatistics", "Error loading class names from Firebase", error.toException());
            }
        });
    }

    private void loadStudentData() {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentClassId = studentSnapshot.child("classId").getValue(String.class);
                    String gender = studentSnapshot.child("gender").getValue(String.class);

                    // Count students per classId
                    if (studentClassId != null) {
                        int classCount = classData.getOrDefault(studentClassId, 0);
                        classData.put(studentClassId, classCount + 1);
                    }

                    // Count gender distribution
                    if (gender != null) {
                        int genderCount = genderData.getOrDefault(gender, 0);
                        genderData.put(gender, genderCount + 1);
                    }
                }
                // Populate charts after loading and processing data
                populateClassBarChart();
                populateGenderPieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StudentsStatistics", "Error loading student data from Firebase", error.toException());
            }
        });
    }

    private void populateClassBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> classNames = new ArrayList<>();

        // Create BarEntries and map class IDs to names
        int index = 0;
        for (Map.Entry<String, Integer> entry : classData.entrySet()) {
            String classId = entry.getKey();
            String className = classIdToNameMap.getOrDefault(classId, "Unknown");
            classNames.add(className); // Add className to list for axis labels

            entries.add(new BarEntry(index++, entry.getValue())); // Increment index
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Students per Class");
        barDataSet.setColor(Color.BLUE);

        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(12f);
        classBarChart.setData(barData);

        // Customize X-axis to show class names
        XAxis xAxis = classBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(classNames.size());
        xAxis.setGranularity(1f);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < classNames.size()) ? classNames.get(index) : "";
            }
        });

        // Customize chart appearance
        classBarChart.getDescription().setEnabled(false);
        classBarChart.getAxisRight().setEnabled(false);
        classBarChart.getAxisLeft().setTextSize(12f);
        classBarChart.invalidate();
    }

    private void populateGenderPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        // Loop through gender data to create PieEntries
        for (Map.Entry<String, Integer> entry : genderData.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(entries, "Gender Distribution");
        pieDataSet.setColors(Color.MAGENTA, Color.CYAN);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueFormatter(new PercentFormatter(genderPieChart));

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(12f);
        genderPieChart.setData(pieData);

        // Customize pie chart appearance
        genderPieChart.setUsePercentValues(true);
        genderPieChart.setCenterText("Gender Distribution");
        genderPieChart.setCenterTextSize(16f);
        genderPieChart.setCenterTextColor(Color.DKGRAY);
        genderPieChart.getDescription().setEnabled(false);
        genderPieChart.setHoleRadius(40f);
        genderPieChart.setTransparentCircleRadius(45f);
        genderPieChart.invalidate();
    }
}
