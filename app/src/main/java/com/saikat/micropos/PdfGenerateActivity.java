package com.saikat.micropos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PdfGenerateActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private DatabaseReference mDatabase;

    private Button btnStartDate, btnEndDate;
    private int selectedYear, selectedMonth, selectedDay;
    private int selectedEndYear, selectedEndMonth, selectedEndDay;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_pdf);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("TransactionHistory").child("GKPOkFrxVeQhqt5NU4xBbmn5XCw1");

        // Initialize views
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        Spinner paymentStatusSpinner = findViewById(R.id.paymentStatusSpinner);
        Spinner paymentTypeSpinner = findViewById(R.id.paymentTypeSpinner);
        Button generatePdfButton = findViewById(R.id.btnGeneratePDF);

        // Populate payment status spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentStatusSpinner.setAdapter(statusAdapter);

        // Populate payment type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentTypeSpinner.setAdapter(typeAdapter);

        // Handle PDF generation button click
        generatePdfButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else {
                generatePdf();
            }
        });

        // Set onClick listeners for date selection buttons
        btnStartDate.setOnClickListener(this::selectStartDate);
        btnEndDate.setOnClickListener(this::selectEndDate);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Storage Permission Required")
                    .setMessage("This app requires storage permission to generate PDF files.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        ActivityCompat.requestPermissions(PdfGenerateActivity.this,
                                STORAGE_PERMISSIONS, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdf();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void selectStartDate(View view) {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePicker, year, month, day) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = day;
                    btnStartDate.setText(getString(R.string.selected_date_format, year, month + 1, day));
                }, currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }

    public void selectEndDate(View view) {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (datePicker, year, month, day) -> {
                    selectedEndYear = year;
                    selectedEndMonth = month;
                    selectedEndDay = day;
                    btnEndDate.setText(getString(R.string.selected_date_format, year, month + 1, day));
                }, currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }

    private void generatePdf() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date startDate = null;
        Date endDate = null;
        String desiredPaymentStatus;
        String desiredPaymentType;

        // Get selected dates
        try {
            int startYear = selectedYear;
            int startMonth = selectedMonth;
            int startDay = selectedDay;
            startDate = dateFormat.parse(String.format(Locale.getDefault(), "%04d-%02d-%02d", startYear, startMonth + 1, startDay));

            int endYear = selectedEndYear;
            int endMonth = selectedEndMonth;
            int endDay = selectedEndDay;
            endDate = dateFormat.parse(String.format(Locale.getDefault(), "%04d-%02d-%02d", endYear, endMonth + 1, endDay));
        } catch (ParseException e) {
            e.getMessage();
        }

        // Get desired payment status and type
        Spinner paymentStatusSpinner = findViewById(R.id.paymentStatusSpinner);
        Spinner paymentTypeSpinner = findViewById(R.id.paymentTypeSpinner);

        desiredPaymentStatus = paymentStatusSpinner.getSelectedItem().toString();
        desiredPaymentType = paymentTypeSpinner.getSelectedItem().toString();

        Document document = new Document(PageSize.A4.rotate());
        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    + File.separator + "example.pdf";

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            PdfPTable table = new PdfPTable(7); // 7 columns for each transaction detail
            table.setWidthPercentage(100); // Make table fill the width of the page

            // Add table headers
            addTableHeader(table);

            Date finalStartDate = startDate;
            Date finalEndDate = endDate;
            mDatabase.orderByKey().startAt(formatDate(startDate)).endAt(formatDate(endDate)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            String transactionTime = snapshot.child("transactionTime").getValue(String.class);
                            if (isWithinDateRange(transactionTime, finalStartDate, finalEndDate)) {
                                String cashEntry = snapshot.child("cashEntry").getValue(String.class);
                                String itemValues = snapshot.child("itemValues").getValue(String.class);
                                String paymentStatus = snapshot.child("paymentStatus").getValue(String.class);
                                String paymentType = snapshot.child("paymentType").getValue(String.class);
                                String totalValue = snapshot.child("totalValue").getValue(String.class);
                                String transactionId = snapshot.child("transactionId").getValue(String.class);

                                // Check if the transaction meets the desired conditions
                                boolean meetsConditions = false;
                                if (desiredPaymentStatus.equals("All Transactions") && desiredPaymentType.equals("All Transactions")) {
                                    meetsConditions = true; // Include all transactions
                                } else if (desiredPaymentStatus.equals("All Transactions")) {
                                    meetsConditions = desiredPaymentType.equals(paymentType); // Filter by payment type only
                                } else if (desiredPaymentType.equals("All Transactions")) {
                                    meetsConditions = desiredPaymentStatus.equals(paymentStatus); // Filter by payment status only
                                } else {
                                    meetsConditions = desiredPaymentStatus.equals(paymentStatus) && desiredPaymentType.equals(paymentType); // Filter by both payment status and type
                                }

                                if (meetsConditions) {
                                    // Add transaction details to the table
                                    addRow(table, transactionTime, transactionId, cashEntry, itemValues, paymentStatus, paymentType, totalValue);
                                }
                            }
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }


                    // Add the table to the document
                    try {
                        document.add(table);
                    } catch (DocumentException e) {
                        throw new RuntimeException(e);
                    }
                    document.close();
                    Toast.makeText(PdfGenerateActivity.this, "PDF generated successfully", Toast.LENGTH_SHORT).show();
                    createNotification(filePath);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PdfGenerateActivity.this, "Failed to retrieve data from Firebase", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (FileNotFoundException | DocumentException e) {
            e.getMessage();
            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
        }
    }


    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(date);
    }

    private boolean isWithinDateRange(String transactionTime, Date startDate, Date endDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dateFormat.parse(transactionTime);
            assert date != null;
            return date.after(startDate) && date.before(endDate);
        } catch (ParseException e) {
            e.getMessage();
            return false;
        }
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"Date", "Transaction ID", "Cash Entry", "Item Values", "Payment Status", "Payment Type", "Total Value"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header));
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, String... rowData) {
        for (String data : rowData) {
            table.addCell(data);
        }
    }

    private void createNotification(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(filePath));
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            String channelName = "Channel Name";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("PDF Exported")
                .setContentText("Click to open the exported PDF")
                .setSmallIcon(R.drawable.icon_save)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

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
        notificationManager.notify(123, builder.build());
    }
}
