package com.saikat.micropos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saikat.micropos.persistance.entity.TransactionHistory;
import com.saikat.micropos.util.TransactionHistoryManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.NonNull;

public class QrActivity extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG = "QRActivity";
    private ImageView qrCodeImageView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        TextView amountTextView = findViewById(R.id.amountTextView);
        assignId(R.id.paidButton);
        assignId(R.id.cancelButton);

        TransactionHistory transactionHistory = (TransactionHistory) getIntent().getSerializableExtra("transactionHistoryKey");

        assert transactionHistory != null;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("TransactionHistory").child(transactionHistory.getUserId());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    @SuppressLint("DefaultLocale") String amount = String.format("%.2f", Double.parseDouble(transactionHistory.getTotalValue()));
                    String apiUrl = "https://upiqr.in/api/qr" +
                            "?vpa=" + dataSnapshot.child("vpa").getValue(String.class) +
                            "&amount=" + amount +
                            "&name=" + dataSnapshot.child("name").getValue(String.class);;

                    // Update the TextView with the formatted amount
                    amountTextView.setText("Amount: ₹" + amount);

                    new FetchQRCodeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiUrl);
                } else {
                    Log.d(TAG, "No data found at the specified path");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    void assignId(int id){
        MaterialButton btn = findViewById(id);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        MaterialButton button =(MaterialButton) view;
        String buttonText = button.getText().toString();

        TransactionHistoryManager transactionHistoryManager = new TransactionHistoryManager();

        Intent intent = new Intent(QrActivity.this, MainActivity.class);
        TransactionHistory transactionHistory = (TransactionHistory) getIntent().getSerializableExtra("transactionHistoryKey");

        assert transactionHistory != null;

        // Update the paymentStatus field of the transaction in the database
        transactionHistoryManager.updateTransactionField(
                transactionHistory.getUserId(),
                transactionHistory.getTransactionTime(),
                "paymentStatus",
                buttonText
        );

        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchQRCodeTask extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... urls) {
            String urlString = urls[0];
            HttpURLConnection urlConnection = null;
            try {
                // Create URL object
                URL url = new URL(urlString);

                // Open a connection
                urlConnection = (HttpURLConnection) url.openConnection();

                // Set request method to GET
                urlConnection.setRequestMethod("GET");

                String contentType = urlConnection.getHeaderField("Content-Type");
                Log.d(TAG, "Content-Type: " + contentType);

                // Check if response code is OK
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Get input stream
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                    // Parse SVG from input stream
                    SVG svg = SVG.getFromInputStream(inputStream);

                    // Close input stream
                    inputStream.close();

                    // Resize the SVG
                    svg.setDocumentWidth("1000px");
                    svg.setDocumentHeight("1000px");

                    // Create drawable from SVG
                    return new PictureDrawable(svg.renderToPicture());
                } else {
                    Log.e(TAG, "HTTP error code: " + urlConnection.getResponseCode());
                }
            } catch (IOException | SVGParseException e) {
                Log.e(TAG, "Error fetching QR code: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            if (drawable != null) {
                // Display the SVG image in the ImageView
                qrCodeImageView.setImageDrawable(drawable);
            } else {
                Log.e(TAG, "Failed to fetch and parse SVG");
            }
        }
    }
}
