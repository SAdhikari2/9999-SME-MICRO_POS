package com.saikat.micropos.util;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.saikat.micropos.persistance.entity.TransactionHistory;

public class TransactionHistoryManager {
    private final DatabaseReference databaseReference;

    public TransactionHistoryManager() {
        // Initialize Firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("TransactionHistory");
    }

    // Method to retrieve transaction history based on transactionId
    public void retrieveTransaction(String transactionId, final TransactionCallback callback) {
        // Query the database based on transactionId
        Query query = databaseReference.orderByChild("transactionId").equalTo(transactionId);

        // Add ValueEventListener to retrieve data
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through the dataSnapshot to retrieve TransactionHistory objects
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TransactionHistory transactionHistory = snapshot.getValue(TransactionHistory.class);
                    // Pass the retrieved TransactionHistory object to the callback method
                    callback.onTransactionRetrieved(transactionHistory);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    // Method to update a field in the transaction history
    public void updateTransactionField(String transactionId, String fieldToUpdate, Object newValue) {
        // Get reference to the specific node based on transactionId
        DatabaseReference transactionRef = databaseReference.child(transactionId);

        // Update the field with the new value
        transactionRef.child(fieldToUpdate).setValue(newValue);
    }

    // Define a callback interface to handle retrieved TransactionHistory objects
    public interface TransactionCallback {
        void onTransactionRetrieved(TransactionHistory transactionHistory);
    }
}
