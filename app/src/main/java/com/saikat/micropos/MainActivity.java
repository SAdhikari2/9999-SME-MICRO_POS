package com.saikat.micropos;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saikat.micropos.persistance.entity.TransactionHistory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    TextView resultTv,solutionTv;

    TransactionHistory transactionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       resultTv = findViewById(R.id.result_tv);
       solutionTv = findViewById(R.id.solution_tv);

       solutionTv.setMovementMethod(new ScrollingMovementMethod());

       assignId(R.id.button_c);
       assignId(R.id.button_open_bracket);
       assignId(R.id.button_close_bracket);
       assignId(R.id.button_divide);
       assignId(R.id.button_multiply);
       assignId(R.id.button_plus);
       assignId(R.id.button_minus);
       assignId(R.id.button_equals);
       assignId(R.id.button_0);
       assignId(R.id.button_1);
       assignId(R.id.button_2);
       assignId(R.id.button_3);
       assignId(R.id.button_4);
       assignId(R.id.button_5);
       assignId(R.id.button_6);
       assignId(R.id.button_7);
       assignId(R.id.button_8);
       assignId(R.id.button_9);
       assignId(R.id.button_ac);
       assignId(R.id.button_dot);
       assignId(R.id.cash_in);
       assignId(R.id.cash_out);
    }

    void assignId(int id){
        MaterialButton btn = findViewById(id);
        btn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        MaterialButton button =(MaterialButton) view;
        String buttonText = button.getText().toString();
        String dataToCalculate = solutionTv.getText().toString();

        if(buttonText.equals("AC")){
            solutionTv.setText("");
            resultTv.setText("0");
            return;
        }
        if(buttonText.equals("=")){
            return;
        }
        if(buttonText.equals("C")){
            dataToCalculate = dataToCalculate.isEmpty() | dataToCalculate.equals("0") ?
                    String.valueOf(0) : dataToCalculate.substring(0,dataToCalculate.length()-1);
        }else{
            dataToCalculate = buttonText.equals("Cash In +") | buttonText.equals("Cash Out -") ? dataToCalculate :
                    dataToCalculate+buttonText;
        }

        solutionTv.setText(dataToCalculate);

        String finalResult = getResult(dataToCalculate);

        if(!finalResult.equals("Err")){
            resultTv.setText(finalResult);
        }
        if(buttonText.equals("Cash In +")){
            proceedToPayment(dataToCalculate, buttonText);
            return;
        }
        if(buttonText.equals("Cash Out -")){
            proceedToPayment(dataToCalculate, buttonText);
        }



    }

    String getResult(String data){
        try{
            Context context  = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            String finalResult =  context.evaluateString(scriptable,data,"Javascript",1,null).toString();
            if(finalResult.endsWith(".0")){
                finalResult = finalResult.replace(".0","");
            }
            return finalResult;
        }catch (Exception e){
            return "Err";
        }
    }

    void proceedToPayment(String dataToCalculate, String buttonText) {
        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);

        transactionHistory = TransactionHistory.builder()
                .transactionId(UUID.randomUUID().toString())
                .itemValues(dataToCalculate)
                .totalValue(buttonText.equals("Cash Out -") ? "-".concat(resultTv.getText().toString()) : resultTv.getText().toString())
                .cashEntry(buttonText)
                .paymentType("")
                .paymentStatus("")
                .transactionTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()))
                .build();

        intent.putExtra("transactionHistoryKey", transactionHistory);
        startActivity(intent);
    }

}