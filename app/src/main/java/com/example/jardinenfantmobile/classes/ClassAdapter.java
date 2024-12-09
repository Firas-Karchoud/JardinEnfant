package com.example.jardinenfantmobile.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<ClassModel> classList;
    private List<ClassModel> fullList; // Full list for restoring data after filtering
    private final Context context;

    public ClassAdapter(Context context, List<ClassModel> classList) {
        this.context = context;
        this.classList = classList;
        this.fullList = new ArrayList<>(classList); // Initialize full list for filtering
    }

    // Method to update the full list
    public void updateFullList(List<ClassModel> newFullList) {
        this.fullList.clear();
        this.fullList.addAll(newFullList);
        this.classList = new ArrayList<>(newFullList); // Sync displayed list with full list
        notifyDataSetChanged();
    }

    // Method for filtering class list based on a query
    public void filter(String query) {
        if (query.isEmpty()) {
            classList.clear();
            classList.addAll(fullList); // Restore full list if query is empty
        } else {
            List<ClassModel> filteredList = new ArrayList<>();
            for (ClassModel classModel : fullList) {
                if (classModel.getName().toLowerCase().contains(query.toLowerCase()) ||
                        classModel.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(classModel);
                }
            }
            classList.clear();
            classList.addAll(filteredList);
        }
        notifyDataSetChanged(); // Refresh the adapter
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classList.get(position);
        holder.textViewName.setText(classModel.getName());
        holder.textViewDescription.setText(classModel.getDescription());

        // Set up delete icon with confirmation dialog
        holder.iconDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmation")
                    .setMessage("Voulez-vous vraiment supprimer cette classe ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        DatabaseReference classRef = FirebaseDatabase.getInstance()
                                .getReference("classes")
                                .child(classModel.getId());
                        classRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Classe supprimée avec succès", Toast.LENGTH_SHORT).show();
                                    classList.remove(position);
                                    notifyItemRemoved(position);
                                    updateFullList(classList); // Sync full list with removed item
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        // Set up edit icon with intent to EditClassActivity
        holder.iconEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditClassActivity.class);
            intent.putExtra("classId", classModel.getId());
            intent.putExtra("className", classModel.getName());
            intent.putExtra("classDescription", classModel.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDescription;
        ImageView iconEdit, iconDelete;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            iconEdit = itemView.findViewById(R.id.iconEdit);
            iconDelete = itemView.findViewById(R.id.iconDelete);
        }
    }
}
