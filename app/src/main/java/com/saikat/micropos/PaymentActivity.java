package com.saikat.micropos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.saikat.micropos.persistance.entity.TransactionHistory;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener{

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

//        TransactionHistory transactionHistory = (TransactionHistory) getIntent().getSerializableExtra("transactionHistoryKey");

        if(buttonText.equals("Online")) {
            startActivity(intent);
        }

    }
}
