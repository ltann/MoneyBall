package com.example.moneyball;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends AppCompatActivity {

    ImageButton venmoButton;
    ImageButton googlepayButton;
    TextView betValButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        venmoButton = findViewById(R.id.venmoButton);
        googlepayButton = findViewById(R.id.googlepayButton);
        betValButton = findViewById(R.id.betValueButton);
        Bundle extras = getIntent().getExtras();
        final double betVal = extras.getDouble("betVal");

        betValButton.setText("Pay The Winner of Wager");


        venmoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.venmo");

                if (launchIntent != null){
                    startActivity(launchIntent);
                }else{
                    Toast.makeText(PaymentActivity.this,"Venmo Download Required",Toast.LENGTH_LONG).show();

                }

            }
        });

        googlepayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.walletnfcrel");


                if (launchIntent != null){
                    startActivity(launchIntent);
                }else{
                    Toast.makeText(PaymentActivity.this,"Google Pay Download Required",Toast.LENGTH_LONG).show();

                }

            }
        });


    }

}
