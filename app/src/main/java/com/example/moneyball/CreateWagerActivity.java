package com.example.moneyball;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class CreateWagerActivity extends AppCompatActivity {
    private final int PICK_IMAGE = 1;
    ImageButton upload;
    Drawable bg;
    Uri selectedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wager);
        upload = findViewById(R.id.upload);
        final TextView uploadTV = findViewById(R.id.uploadTV);
        final EditText heading = findViewById(R.id.heading);
        final EditText description = findViewById(R.id.description);
        final EditText groupName = findViewById(R.id.groupName);
        Button exit = findViewById(R.id.exit);
        Button done = findViewById(R.id.done);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadTV.setVisibility(View.GONE);

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);

            }
        });

        final Intent intent = new Intent();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String headingText = heading.getText().toString();
                String descriptionText = description.getText().toString();
                String groupNameText = groupName.getText().toString();
                intent.putExtra("pic", selectedImageUri.toString());
                intent.putExtra("headingText", headingText);
                intent.putExtra("descriptionText", descriptionText);
                intent.putExtra("groupNameText", groupNameText);
                setResult(RESULT_OK, intent);
                finish();

            }
        });
        // TODO
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if(resultCode==RESULT_OK){
                selectedImageUri = data.getData();
                upload.setVisibility(View.GONE);
                RelativeLayout layout = findViewById(R.id.relativeLayout);
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bg = Drawable.createFromStream(inputStream, selectedImageUri.toString());
                layout.setBackground(bg);
            }
        }
    }
}