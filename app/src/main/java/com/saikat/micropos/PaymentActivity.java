package com.saikat.micropos;

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
        MaterialButton button =(MaterialButton) view;
        String buttonText = button.getText().toString();

        Intent intent = new Intent(PaymentActivity.this, QrActivity.class);

        TransactionHistory transactionHistory = (TransactionHistory) getIntent().getSerializableExtra("transactionHistoryKey");
        intent.putExtra("transactionHistoryKey", transactionHistory);
        if(buttonText.equals("Online")) {
            assert transactionHistory != null;
            transactionHistory.setPaymentType(buttonText);
            // Ensure user is authenticated
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                transactionHistory.setUserId(uid); // Include user UID in transaction data

                // Write transaction data to Firebase Realtime Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("TransactionHistory").child(uid);
                databaseReference.child(transactionHistory.getTransactionId()).setValue(transactionHistory)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PaymentActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            } else {
                                Toast.makeText(PaymentActivity.this, "Failed to update transaction data", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PaymentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(PaymentActivity.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
