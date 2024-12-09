package com.example.jardinenfantmobile.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jardinenfantmobile.R;
import com.example.jardinenfantmobile.user.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ClientEventsActivity extends AppCompatActivity {
    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;
    private ArrayList<Event> eventsList;
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_events); // Use a layout specifically for client

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                // Handle home navigation
                startActivity(new Intent(ClientEventsActivity.this, UserProfileActivity.class));
                return true;
            } else if (id == R.id.navigation_events) {
                // Handle events navigation
                startActivity(new Intent(ClientEventsActivity.this, ClientEventsActivity.class));
                return true;
            }
            return false;
        });

        // Set up RecyclerView for displaying events
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventsList = new ArrayList<>();
        eventsAdapter = new EventsAdapter(eventsList, event -> {
            // Navigate to EventRegistrationActivity
            Intent intent = new Intent(ClientEventsActivity.this, EventRegistrationActivity.class);
            intent.putExtra("eventId", event.getId());
            startActivity(intent);
        }, this, false); // isAdmin = false
        eventsRecyclerView.setAdapter(eventsAdapter);


        // Set up Firebase reference
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Load existing events
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);
                    eventsList.add(event);
                }
                eventsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ClientEventsActivity", "Database error: " + error.getMessage());
            }
        });
    }
}
