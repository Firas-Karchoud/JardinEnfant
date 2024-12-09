package com.example.jardinenfantmobile.events;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    private ArrayList<Event> eventsList;
    private OnEventClickListener listener;
    private Context context;
    private boolean isAdmin; // Flag to indicate if the adapter is for admin or client

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventsAdapter(ArrayList<Event> eventsList, OnEventClickListener listener, Context context, boolean isAdmin) {
        this.eventsList = eventsList;
        this.listener = listener;
        this.context = context;
        this.isAdmin = isAdmin; // Set the flag based on activity type
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));

        // Show/hide edit and delete buttons based on isAdmin flag
        if (isAdmin) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            // Edit button logic
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, UpdateEventActivity.class);
                intent.putExtra("eventId", event.getId());
                context.startActivity(intent);
            });

            // Delete button logic
            holder.deleteButton.setOnClickListener(v -> {
                DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(event.getId());
                eventRef.removeValue().addOnSuccessListener(aVoid -> {
                    // Find the index of the event by matching the event ID
                    int index = -1;
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).getId().equals(event.getId())) {
                            index = i;
                            break;
                        }
                    }

                    // If the event was found, remove it safely
                    if (index != -1) {
                        eventsList.remove(index);
                        notifyItemRemoved(index);
                        notifyItemRangeChanged(index, eventsList.size());
                        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show());
            });
        } else {
            // Hide buttons for client
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle;
        Button editButton, deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
