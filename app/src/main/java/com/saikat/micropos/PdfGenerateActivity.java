package com.saikat.micropos;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PdfGenerateActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_pdf);

        Button generatePdfButton = findViewById(R.id.btnGeneratePDF);
        generatePdfButton.setOnClickListener(v -> {
            // Check if the WRITE_EXTERNAL_STORAGE permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                requestStoragePermission();
            } else {
                // Permission is granted, proceed with PDF generation
                generatePdf();
            }
        });
    }

    private void requestStoragePermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Show an explanation to the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Storage Permission Required")
                    .setMessage("This app requires storage permission to generate PDF files.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        // Request the permission
                        ActivityCompat.requestPermissions(PdfGenerateActivity.this,
                                STORAGE_PERMISSIONS, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Cancel the dialog
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            // No explanation needed, request the permission
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // Check if permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with PDF generation
                generatePdf();
            } else {
                // Permission denied
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdf() {
        // Proceed with PDF generation
        Document document = new Document();
        try {
            // Define the file path
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    + File.separator + "example.pdf";

            // Create a PdfWriter instance to write the document to the file
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Open the document
            document.open();

            // Add content to the document
            Paragraph paragraph = new Paragraph("Hello, this is a PDF document.");
            document.add(paragraph);

            // Close the document
            document.close();

            // Check if the activity is still running before showing the Toast
            if (!isFinishing()) {
                // Show a success message
                Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show();
            }

            // Create a notification
            createNotification(filePath);

        } catch (FileNotFoundException | DocumentException e) {
            e.getMessage();
            // Handle exceptions
            // Check if the activity is still running before showing the Toast
            if (!isFinishing()) {
                Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotification(String filePath) {
        // Create an intent to open the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create a PendingIntent for the intent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "pdf_notification_channel")
                .setSmallIcon(R.drawable.icon_save)
                .setContentTitle("PDF Generated")
                .setContentText("Click to open the PDF file")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Get the NotificationManager and notify the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
