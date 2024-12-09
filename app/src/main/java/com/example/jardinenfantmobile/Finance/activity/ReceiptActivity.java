package com.example.jardinenfantmobile.Finance.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jardinenfantmobile.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // Récupérer les détails envoyés depuis OnlinePaymentActivity
        String parentName = getIntent().getStringExtra("parent_name");
        double paymentAmount = getIntent().getDoubleExtra("payment_amount", 0.0);
        String paymentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Afficher les informations dans les TextViews
        TextView parentNameTextView = findViewById(R.id.receipt_parent_name);
        TextView paymentAmountTextView = findViewById(R.id.receipt_amount);
        TextView paymentDateTextView = findViewById(R.id.receipt_date);

        parentNameTextView.setText("Nom du Parent : " + parentName);
        paymentAmountTextView.setText("Montant : " + paymentAmount + " €");
        paymentDateTextView.setText("Date : " + paymentDate);

        // Optionnel : Afficher un Toast pour indiquer que le reçu a été généré
        Toast.makeText(this, "Reçu généré avec succès.", Toast.LENGTH_SHORT).show();
    }
}
