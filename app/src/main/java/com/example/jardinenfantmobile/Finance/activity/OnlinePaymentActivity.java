package com.example.jardinenfantmobile.Finance.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jardinenfantmobile.R;
import com.example.jardinenfantmobile.Finance.activity.model.Parent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OnlinePaymentActivity extends AppCompatActivity {

    private EditText creditCardNumberEditText;
    private EditText cvvEditText;
    private EditText expirationDateEditText;
    private Button confirmPaymentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_payment);

        // Récupération de l'objet Parent
        Parent parent = (Parent) getIntent().getSerializableExtra("parent_info");

        // Initialisation des champs de saisie de carte
        creditCardNumberEditText = findViewById(R.id.creditCardNumber);
        cvvEditText = findViewById(R.id.cvv);
        expirationDateEditText = findViewById(R.id.expirationDate);
        confirmPaymentButton = findViewById(R.id.confirmPaymentButton);

        // Action du bouton pour confirmer le paiement
        confirmPaymentButton.setOnClickListener(view -> {
            String creditCardNumber = creditCardNumberEditText.getText().toString();
            String cvv = cvvEditText.getText().toString();
            String expirationDate = expirationDateEditText.getText().toString();

            if (creditCardNumber.isEmpty() || cvv.isEmpty() || expirationDate.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Message de confirmation
            Toast.makeText(this, "Paiement confirmé", Toast.LENGTH_SHORT).show();

            // Envoi du reçu par email
            sendReceiptEmail(parent);

            // Lancer ReceiptActivity avec les informations de paiement
            Intent intent = new Intent(OnlinePaymentActivity.this, ReceiptActivity.class);
            intent.putExtra("parent_name", parent.getNom());
            intent.putExtra("payment_amount", parent.getMontantAPayer());
            startActivity(intent);
        });
    }

    private void sendReceiptEmail(Parent parent) {
        Log.d("EmailDebug", "Envoi du reçu par email pour " + parent.getNom());

        if (parent.getEmail() == null || parent.getEmail().isEmpty()) {
            Log.e("EmailDebug", "L'email du parent est vide ou null.");
            Toast.makeText(this, "L'email du parent est invalide.", Toast.LENGTH_SHORT).show();
            return; // Quittez si l'email est invalide
        }

        String subject = "Reçu de paiement - Jardin d'enfants";
        String message = "Bonjour " + parent.getNom() + ",\n\n" +
                "Merci pour votre paiement.\n\n" +
                "Détails du paiement :\n" +
                "- Montant : " + parent.getMontantAPayer() + " €\n" +
                "- Date : " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()) + "\n\n" +
                "Cordialement,\n" +
                "L'équipe du Jardin d'enfants";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Assure l'ouverture dans les apps d'email
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"nouha.ouertani@gmail.com"}); // Votre adresse email
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, "Envoyer le reçu par email"));
        } else {
            Log.e("EmailDebug", "Pas d'application capable d'envoyer des emails.");
            Toast.makeText(this, "Aucune application d'email trouvée", Toast.LENGTH_SHORT).show();
        }
    }
}
