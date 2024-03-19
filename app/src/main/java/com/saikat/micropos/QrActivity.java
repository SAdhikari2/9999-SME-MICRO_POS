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

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.material.button.MaterialButton;
import com.saikat.micropos.persistance.entity.TransactionHistory;
import com.saikat.micropos.util.TransactionHistoryManager;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

        // Call the API endpoint with dynamic query parameters
        String vpa = "8967859971@ybl";
        @SuppressLint("DefaultLocale") String amount = String.format("%.2f", Double.parseDouble(transactionHistory.getTotalValue()));
        String name = "Saikat Adhikari";
        String apiUrl = "https://upiqr.in/api/qr" +
                "?vpa=" + vpa +
                "&amount=" + amount +
                "&name=" + name;

        // Update the TextView with the formatted amount
        amountTextView.setText("Amount: $" + amount);

        new FetchQRCodeTask().execute(apiUrl);
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
                transactionHistory.getTransactionId(),
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
            Drawable bitmap = null;
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
            return bitmap;
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
