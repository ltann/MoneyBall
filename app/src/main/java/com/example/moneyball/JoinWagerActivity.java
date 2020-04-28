package com.example.moneyball;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class JoinWagerActivity extends AppCompatActivity {
    Button btnYes, btnNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_wager);
        //initialize UI
        final TextView tvWagerTitle = findViewById(R.id.tvWagerTitle);
        final TextView tvWagerDescription = findViewById(R.id.tvWagerDescription);
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);

        Intent wagerInfo = getIntent(); //get the intent data

        //get the heading and description
        final String wagerTitle = wagerInfo.getStringExtra("heading");
        final String wagerDescription = wagerInfo.getStringExtra("description");

        //set the text of the text views
        tvWagerDescription.setText(wagerDescription);
        tvWagerTitle.setText(wagerTitle);

        //on click listener to vote yes
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wagerIntent = new Intent(getApplicationContext(), GroupActivity.class); //create intent to go back to group activity
                wagerIntent.putExtra("vote", "Y");  //pass data of the vote
                setResult(RESULT_OK, wagerIntent); //set the result
                finish(); //go back to the group activity
            }
        });

        //on click listener to vote no
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wagerIntent = new Intent(getApplicationContext(), GroupActivity.class);//create intent to go back to group activity
                wagerIntent.putExtra("vote", "N");//pass data of the vote
                setResult(RESULT_OK, wagerIntent);//set the result
                finish();//go back to the group activity
            }
        });

    }
}
