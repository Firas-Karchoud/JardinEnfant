package com.example.jardinenfantmobile.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventRegistrationActivity extends AppCompatActivity {
    private TextView eventTitle, eventDescription, eventDate, eventLocation;
    private Spinner childSpinner;
    private Button registerButton;
    private DatabaseReference eventsRef;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        eventDate = findViewById(R.id.eventDate);
        eventLocation = findViewById(R.id.eventLocation);
        childSpinner = findViewById(R.id.childSpinner);
        registerButton = findViewById(R.id.registerButton);

        eventId = getIntent().getStringExtra("eventId");
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Charger les informations de l'événement et les enfants
        loadEventDetails();
        loadChildren();

        registerButton.setOnClickListener(view -> {
            String selectedChild = childSpinner.getSelectedItem().toString();
            if (!selectedChild.isEmpty()) {
                eventsRef.child(eventId).child("registered").child(selectedChild).setValue(true)
                        .addOnSuccessListener(aVoid -> Toast.makeText(EventRegistrationActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(EventRegistrationActivity.this, "Échec de l'inscription", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadEventDetails() {
        // Retrieve the event details from Firebase
        eventsRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve and display event details
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);

                    eventTitle.setText(title != null ? title : "N/A");
                    eventDescription.setText(description != null ? description : "N/A");
                    eventDate.setText(date != null ? date : "N/A");
                    eventLocation.setText(location != null ? location : "N/A");
                } else {
                    Toast.makeText(EventRegistrationActivity.this, "Événement non trouvé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventRegistrationActivity.this, "Erreur de chargement des détails de l'événement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildren() {
        // Charger une liste fictive d'enfants pour l'exemple
        ArrayList<String> children = new ArrayList<>();
        children.add("Enfant A");
        children.add("Enfant B");
        children.add("Enfant C");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, children);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSpinner.setAdapter(adapter);
    }
}

