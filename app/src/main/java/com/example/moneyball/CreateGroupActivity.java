package com.example.moneyball;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {
    private final int PICK_IMAGE = 1;   //initialize some codes for sending intent
    private final int NEW_GROUP = 123;
    private final int ADD_GROUP = 321, REQUEST_READ_STORAGE = 101;
    Drawable bg;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    private ImageButton uploadGP;
    private RelativeLayout groupPicHolder;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize UI
        setContentView(R.layout.activity_create_group);
        final EditText heading = findViewById(R.id.groupHeading);
        final EditText description = findViewById(R.id.groupDescription);
        final EditText join_group = findViewById(R.id.etJoin_Group);
        Button btn_join_group = findViewById(R.id.btnJoin_group);
        Button exit = findViewById(R.id.exit);
        Button done = findViewById(R.id.done);
        groupPicHolder = findViewById(R.id.groupPicHolder);
        final TextView uploadTV = findViewById(R.id.uploadTV);
        uploadGP = findViewById(R.id.uploadGroupPic);

        uploadGP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) { // if we need to ask for permissions (sdk 23 and above)
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(CreateGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Toast.makeText(getApplicationContext(), "We need permission to upload pictures to associate with your wager", Toast.LENGTH_LONG).show();
                        } else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(CreateGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
                        }
                    } else {
                        uploadTV.setVisibility(View.GONE); // so that the image isn't blocked after uploading

                        // defining that we are collecting an image
                        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getIntent.setType("image/*");

                        // generating a picker so the user chooses which app to use to upload the image
                        Intent pickIntent = new Intent(Intent.ACTION_PICK);
                        pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                        // opening the chosen app to select image
                        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                        startActivityForResult(chooserIntent, PICK_IMAGE);
                    }
                }
                else{
                    uploadTV.setVisibility(View.GONE);

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);

                }
            }
        });

        final Intent intent = new Intent(); //create an intent
        //set on click listener for "done" button
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String headingText = heading.getText().toString(); //get the heading the user has entered
                String descriptionText = description.getText().toString(); //get the description the user has entered
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get the user ID
                String UID = "";
                String picUriStr = "";
                if(user!=null){
                    UID = user.getUid();
                }

                if(headingText.equals("")|| descriptionText.equals("")){ //make sure the necessary info is entered
                    Toast.makeText(getApplicationContext(),  "Please enter group information for all text fields", Toast.LENGTH_SHORT).show();
                }
                else { //if the info is all there
                    if (selectedImageUri == null || selectedImageUri.toString().equals("")) {
                        picUriStr = ""; //if no picture is uploaded, set the string to ""
                    } else {
                        picUriStr = selectedImageUri.toString(); //if a picture has been uploaded, get the path
                    }
                    Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
                    //pass the data through the intent
                    intent.putExtra("groupCreator", UID);
                    intent.putExtra("headingText", headingText);
                    intent.putExtra("descriptionText", descriptionText);
                    intent.putExtra("pic", picUriStr);
                    setResult(NEW_GROUP, intent);
                    finish();
                }

            }
        });

        //Button for joining a group
        btn_join_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String group = join_group.getText().toString(); //get the group ID the user has entered
                ref = ref.child("groups"); //get the database reference
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        if(dataMap!=null) {
                            for (String key : dataMap.keySet()) {
                                //loop through the groups
                                Object data = dataMap.get(key);
                                HashMap<String, Object> groupData = (HashMap<String, Object>) data;
                                //get the group data
                                String heading = groupData.get("heading").toString();
                                String description = groupData.get("description").toString();
                                String groupCreator = groupData.get("groupCreator").toString();
                                String groupId = groupData.get("id").toString();
                                if(groupId.equals(group)){ //if the ID matches a group
                                    //pass data through intent for the matched group
                                    intent.putExtra("groupCreator", groupCreator);
                                    intent.putExtra("headingText", heading);
                                    intent.putExtra("descriptionText", description);
                                    intent.putExtra("id", groupId);
                                    setResult(ADD_GROUP, intent);
                                    finish(); //return to users groups page
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });


            }
        });


        // TODO
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_IMAGE){
            if(resultCode==RESULT_OK){
                selectedImageUri = data.getData();
                uploadGP.setVisibility(View.GONE);
                RelativeLayout layout = findViewById(R.id.groupPicHolder);
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri); // creating an inputstream of the image form the uri
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bg = Drawable.createFromStream(inputStream, selectedImageUri.toString()); // creating a drawable from the input stream
                layout.setBackground(bg); // displaying the chosen image on the screen
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Toast.makeText(getApplicationContext(),  "Thanks! Please click again", Toast.LENGTH_LONG).show();
                } else {
                    // permission denied
                }
                return;
            }

        }
    }

}
