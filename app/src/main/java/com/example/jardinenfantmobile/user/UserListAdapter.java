package com.example.jardinenfantmobile.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jardinenfantmobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    FirebaseAuth authProfile = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = authProfile.getCurrentUser();

    private List<ReadWriteUserDetails> userList;
    private Context context;

    public UserListAdapter(List<ReadWriteUserDetails> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ReadWriteUserDetails user = userList.get(position);

        holder.textViewName.setText(firebaseUser.getDisplayName());
        holder.textViewEmail.setText(firebaseUser.getEmail());
        holder.textViewRole.setText(user.role);

        String userId = firebaseUser.getUid();

        holder.buttonBan.setOnClickListener(v -> banUser(userId));
        holder.buttonDelete.setOnClickListener(v -> deleteUser(userId));
    }

    private void banUser(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId);
        reference.child("status").setValue("banned").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "User banned successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to ban user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users").child(userId);
        reference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewEmail, textViewRole;
        Button buttonBan, buttonDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            buttonBan = itemView.findViewById(R.id.buttonBan);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
