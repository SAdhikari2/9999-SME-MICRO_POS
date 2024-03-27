package com.saikat.micropos.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TransactionHistoryManager {
    private final DatabaseReference databaseReference;

    public TransactionHistoryManager() {
        // Initialize Firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("TransactionHistory");
    }

    // Method to update a field in the transaction history
    public void updateTransactionField(String userId, String transactionTime, String fieldToUpdate, Object newValue) {
        // Get reference to the specific node based on transactionId
        DatabaseReference transactionRef = databaseReference.child(userId).child(transactionTime);

        // Update the field with the new value
        transactionRef.child(fieldToUpdate).setValue(newValue);
    }

}
