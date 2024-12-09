package com.example.jardinenfantmobile.classes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jardinenfantmobile.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditClassActivity extends AppCompatActivity {

    private EditText editTextName, editTextDescription;
    private Button buttonSave;
    private DatabaseReference classRef;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        editTextName = findViewById(R.id.editTextClassName);
        editTextDescription = findViewById(R.id.editTextClassDescription);
        buttonSave = findViewById(R.id.buttonSaveChanges);

        // Récupérer les données de la classe à modifier
        classId = getIntent().getStringExtra("classId");
        String className = getIntent().getStringExtra("className");
        String classDescription = getIntent().getStringExtra("classDescription");

        // Vérifiez si les valeurs sont nulles
        if (classId == null || className == null || classDescription == null) {
            Toast.makeText(this, "Erreur : données de classe manquantes", Toast.LENGTH_SHORT).show();
            finish(); // Fermer l'activité si les données sont nulles
            return;
        }

        editTextName.setText(className);
        editTextDescription.setText(classDescription);

        // Référence Firebase de la classe
        classRef = FirebaseDatabase.getInstance().getReference("classes").child(classId);

        buttonSave.setOnClickListener(v -> {
            String newName = editTextName.getText().toString();
            String newDescription = editTextDescription.getText().toString();

            if (!newName.isEmpty() && !newDescription.isEmpty()) {
                classRef.child("name").setValue(newName);
                classRef.child("description").setValue(newDescription)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditClassActivity.this, "Classe modifiée avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditClassActivity.this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(EditClassActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
