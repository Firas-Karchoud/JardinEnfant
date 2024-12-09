package com.example.jardinenfantmobile.Finance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jardinenfantmobile.R;
import com.example.jardinenfantmobile.Finance.activity.model.Parent;

public class PaymentActivity extends AppCompatActivity {

    private EditText parentNameEditText;
    private EditText parentPhoneEditText;
    private EditText paymentAmountEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        parentNameEditText = findViewById(R.id.parentName);
        parentPhoneEditText = findViewById(R.id.parentPhone);
        paymentAmountEditText = findViewById(R.id.paymentAmount);

        Button proceedPaymentButton = findViewById(R.id.button_proceed_payment);
        proceedPaymentButton.setOnClickListener(view -> {
            String parentName = parentNameEditText.getText().toString();
            String parentPhone = parentPhoneEditText.getText().toString();
            double paymentAmount = Double.parseDouble(paymentAmountEditText.getText().toString());

            Parent parent = new Parent(parentName, "", "", parentPhone, "hassenahmadi540@gmail.com", paymentAmount);
            showPaymentDialog(parent);
        });
    }

    private void showPaymentDialog(Parent parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation de Paiement");
        builder.setMessage("Voulez-vous procéder au paiement pour " + parent.getNom() + " d'un montant de " + parent.getMontantAPayer() + " € ?");

        builder.setPositiveButton("Oui", (dialog, which) -> {
            Intent intent = new Intent(PaymentActivity.this, OnlinePaymentActivity.class);
            intent.putExtra("parent_info", parent);  // `parent` doit être Serializable
            startActivity(intent);
        });

        builder.setNegativeButton("Non", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
