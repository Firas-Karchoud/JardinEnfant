package com.example.jardinenfantmobile.events;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateEventActivity extends AppCompatActivity {

    private EditText titleInput, dateInput, locationInput;
    private Button updateEventButton;
    private DatabaseReference eventsRef;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        dateInput = findViewById(R.id.dateInput);
        locationInput = findViewById(R.id.locationInput);
        updateEventButton = findViewById(R.id.updateEventButton);

        // Get the event ID from intent
        eventId = getIntent().getStringExtra("eventId");

        // Reference to the specific event in Firebase
        eventsRef = FirebaseDatabase.getInstance().getReference("events").child(eventId);

        // Load event data (assuming you already have a method to fetch event data)
        loadEventData();

        // Update event button click listener
        updateEventButton.setOnClickListener(v -> updateEvent());
    }

    // Load event data into the input fields
    private void loadEventData() {
        // Fetch event data from Firebase and set it to input fields
        eventsRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String title = dataSnapshot.child("title").getValue(String.class);
                String date = dataSnapshot.child("date").getValue(String.class);
                String location = dataSnapshot.child("location").getValue(String.class);

                titleInput.setText(title);
                dateInput.setText(date);
                locationInput.setText(location);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(UpdateEventActivity.this, "Failed to load event data", Toast.LENGTH_SHORT).show());
    }

    // Method to update the event
    private void updateEvent() {
        String title = titleInput.getText().toString();
        String date = dateInput.getText().toString();
        String location = locationInput.getText().toString();

        // Validation
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map with updated data
        eventsRef.child("title").setValue(title);
        eventsRef.child("date").setValue(date);
        eventsRef.child("location").setValue(location)
                .addOnSuccessListener(aVoid -> Toast.makeText(UpdateEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(UpdateEventActivity.this, "Failed to update event", Toast.LENGTH_SHORT).show());

        // Close the activity after update
        finish();
    }
}
