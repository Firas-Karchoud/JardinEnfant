package com.example.jardinenfantmobile.events;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {
    private EditText titleInput, descriptionInput, dateInput, locationInput;
    private Button createEventButton;
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dateInput = findViewById(R.id.dateInput);
        locationInput = findViewById(R.id.locationInput);
        createEventButton = findViewById(R.id.createEventButton);

        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        createEventButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String date = dateInput.getText().toString();
            String location = locationInput.getText().toString();

            if (!title.isEmpty() && !date.isEmpty()) {
                String eventId = eventsRef.push().getKey();

                Map<String, Object> eventDetails = new HashMap<>();
                eventDetails.put("id", eventId);
                eventDetails.put("title", title);
                eventDetails.put("description", description);
                eventDetails.put("date", date);
                eventDetails.put("location", location);
                eventDetails.put("registered", new HashMap<String, Boolean>());

                eventsRef.child(eventId).setValue(eventDetails)
                        .addOnSuccessListener(aVoid -> Toast.makeText(CreateEventActivity.this, "Événement créé avec succès", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(CreateEventActivity.this, "Échec de la création de l'événement", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(CreateEventActivity.this, "Veuillez remplir tous les champs requis", Toast.LENGTH_SHORT).show();
            }
        });
    }
}