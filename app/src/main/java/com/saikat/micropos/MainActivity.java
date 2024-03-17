package com.saikat.micropos;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saikat.micropos.databinding.ActivityMainBinding;
import com.saikat.micropos.persistance.entity.TransactionHistory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    TextView resultTv,solutionTv;

    TransactionHistory transactionHistory;
    ActivityMainBinding binding;
    String itemValues, totalValue, cashEntry, paymentType, paymentStatus;
    FirebaseDatabase db;
    DatabaseReference databaseReference;

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
            solutionTv.setText(resultTv.getText());
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
            updateDB(dataToCalculate, buttonText);
            return;
        }
        if(buttonText.equals("Cash Out -")){
            updateDB(dataToCalculate, buttonText);
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

    void updateDB(String dataToCalculate, String buttonText) {
        transactionHistory = new TransactionHistory(
                dataToCalculate,
                buttonText.equals("Cash Out -") ? "-".concat(resultTv.getText().toString()) : resultTv.getText().toString(),
                buttonText,
                "Cash",
                "Paid");
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("TransactionHistory");
        databaseReference.child(UUID.randomUUID().toString()).setValue(transactionHistory)
                .addOnCompleteListener(task -> {
                    resultTv.setText("0");
                    solutionTv.setText("");
                    Toast.makeText(MainActivity.this,"Successfully Updated", Toast.LENGTH_SHORT).show();
                });
    }

}