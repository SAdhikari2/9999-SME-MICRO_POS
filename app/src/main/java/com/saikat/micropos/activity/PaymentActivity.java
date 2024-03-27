package com.saikat.micropos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saikat.micropos.R;
import com.saikat.micropos.persistance.entity.TransactionHistory;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        assignId(R.id.button_cash);
        assignId(R.id.button_online);
        assignId(R.id.button_due);
    }

    void assignId(int id){
        MaterialButton btn = findViewById(id);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        MaterialButton button = (MaterialButton) view;
        String buttonText = button.getText().toString();

        Intent intent = new Intent(PaymentActivity.this, QrActivity.class);

        TransactionHistory transactionHistory = (TransactionHistory) getIntent().getSerializableExtra("transactionHistoryKey");
        assert transactionHistory != null;
        intent.putExtra("transactionHistoryKey", transactionHistory);
        // Ensure user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (buttonText.equals("Online")) {
            transactionHistory.setPaymentType(buttonText);
            if (user != null) {
                writeTransactionDataToDatabase(transactionHistory, intent, user.getUid());
            } else {
                Toast.makeText(PaymentActivity.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            }
        }
        if (buttonText.equals("Cash") | buttonText.equals("Due")) {
            Intent intentMain = new Intent(PaymentActivity.this, MainActivity.class);
            // Update the paymentStatus field of the transaction in the database
            transactionHistory.setPaymentType(buttonText);
            assert user != null;
            transactionHistory.setUserId(user.getUid());
            transactionHistory.setPaymentStatus(buttonText.equals("Cash") ? "Paid" : buttonText);
            // Write transaction data to Firebase Realtime Database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("TransactionHistory").child(user.getUid());
            databaseReference.child(transactionHistory.getTransactionTime()).setValue(transactionHistory)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PaymentActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                            startActivity(intentMain);
                            finish(); // Finish the activity after starting the intent
                        } else {
                            Toast.makeText(PaymentActivity.this, "Failed to update transaction data", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PaymentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void writeTransactionDataToDatabase(TransactionHistory transactionHistory, Intent intent, String uid) {
        transactionHistory.setUserId(uid); // Include user UID in transaction data

        // Write transaction data to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("TransactionHistory").child(uid);
        databaseReference.child(transactionHistory.getTransactionTime()).setValue(transactionHistory)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PaymentActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish(); // Finish the activity after starting the intent
                    } else {
                        Toast.makeText(PaymentActivity.this, "Failed to update transaction data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PaymentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
