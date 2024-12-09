package com.example.jardinenfantmobile.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminEventsActivity extends AppCompatActivity implements EventsAdapter.OnEventClickListener {
    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;
    private ArrayList<Event> eventsList;
    private DatabaseReference eventsRef;
    private static final String CHANNEL_ID = "events_channel";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Set up RecyclerView for displaying events
        eventsRecyclerView = findViewById(R.id.adminEventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventsList = new ArrayList<>();
        eventsAdapter = new EventsAdapter(eventsList, this, this, true); // isAdmin = true
        eventsRecyclerView.setAdapter(eventsAdapter);


        // Floating Action Button to add a new event
        findViewById(R.id.addEventButton).setOnClickListener(v -> {
            Intent intent = new Intent(AdminEventsActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        // Set up Firebase reference
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Load existing events and listen for new events to trigger notifications
        eventsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Event event = snapshot.getValue(Event.class);
                if (event != null) {
                    eventsList.add(event);
                    eventsAdapter.notifyDataSetChanged();
                    sendNotification("New Event Added", event.getTitle());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle updates if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String eventId = snapshot.getKey();
                if (eventId != null) {
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).getId().equals(eventId)) {
                            eventsList.remove(i);
                            eventsAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminEventsActivity", "Database error: " + error.getMessage());
            }
        });

        // Create a notification channel for Android 8.0 and higher
        createNotificationChannel();
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Make sure to add a logo drawable in res/drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AdminEventsActivity", "Notification permission not granted.");
            return;
        }

        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Notifications";
            String description = "Notifications for new events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(AdminEventsActivity.this, UpdateEventActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }
}
