package com.example.moneyball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupActivity extends AppCompatActivity implements WagerAdapter.ItemClickListener {
    private RecyclerView.Adapter wagerAdapter;
    ArrayList<Wager> wagers;
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    TextView heading, description;
    ImageButton backButton;
    String groupHeading, groupDescription;
    Uri groupPicUri;
    ImageView groupPic;
    String groupIdToPass;
    double betVal;
    String wagerResult="";
    public static final int ADD_WAGER_REQUEST = 1, JOIN_WAGER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Intent intent = getIntent(); //get intent that was passed from the previous page (UsersGroupsActivity)
        //get data passed from intent
        final String groupId = intent.getStringExtra("groupId");
        groupIdToPass = groupId; //needs to be a global variable
        groupDescription = intent.getStringExtra("description");
        groupHeading = intent.getStringExtra("heading");
        groupPicUri = Uri.parse(intent.getStringExtra("groupPic"));

        //initialize views
        heading = findViewById(R.id.tvGroup_Title);
        description = findViewById(R.id.tvGroup_Description);
        backButton = findViewById(R.id.btnGroup_Back);
        RecyclerView wagerList = findViewById(R.id.group_bets);
        groupPic = findViewById(R.id.groupPic);

        //sets the groupPic within this activity:
        Picasso.get().load(groupPicUri).into(groupPic);

        //set the heading and description text for the group
        heading.setText(groupHeading);
        description.setText(groupDescription);
        int numOfColumns = 2;
        RecyclerView.LayoutManager recyclerManager = new GridLayoutManager(getApplicationContext(), numOfColumns); //create recycler view manager
        wagerList.setLayoutManager(recyclerManager);

        wagers = new ArrayList<>(); //this will be used to store the groups wagers
        ref = ref.child("groups").child(groupId).child("wagers"); //get the reference to the wagers for this specific group in the database
        ref.addValueEventListener(new ValueEventListener() {
            //read the wager data from the database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wagers.clear(); //clear the wagers arraylist so to avoid adding duplicates
                HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue(); //get the database data as a hashmap
                if(dataMap!=null) { //check if its null to avoid errors
                    for (String key : dataMap.keySet()) {   //loop through the wagers
                        Object data = dataMap.get(key);
                        HashMap<String, Object> wagerData = (HashMap<String, Object>) data;

                        //separate the data by groupname, heading, description, picture, wager creator, list of users, status of wager, value of the bet,
                        // list of challenges, list of users votes, and the result of the wager
                        String groupName = wagerData.get("group").toString();
                        String heading = wagerData.get("heading").toString();
                        String description = wagerData.get("description").toString();
                        String pic = wagerData.get("picture").toString();
                        String wagerCreator = wagerData.get("wagerCreator").toString();
                        ArrayList<String> usersList = (ArrayList<String>)wagerData.get("usersList");
                        Boolean openStatus = (Boolean)wagerData.get("openStatus");
                        double betVal = Double.parseDouble(wagerData.get("betVal")+"");
                        ArrayList<String> challengeList = (ArrayList<String>)wagerData.get("challengeList");
                        ArrayList<String> votesList = (ArrayList<String>)wagerData.get("userVotes");
                        String wagerResult = wagerData.get("wagerResult").toString();

                        //create the wager object
                        Wager newWager = new Wager(key, heading, groupName, pic, description, wagerCreator, usersList, openStatus, betVal, challengeList, votesList, wagerResult);  //create the new wager using the data from above
                        wagers.add(newWager); //add this wager to a list of wagers
                        wagerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        wagerAdapter = new WagerAdapter(wagers);
        wagerList.setAdapter(wagerAdapter);
        ((WagerAdapter) wagerAdapter).setClickListener(this);

        // Proposing a wager
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addWager = new Intent(getApplicationContext(), CreateWagerActivity.class); //create intent to the create wager page
                addWager.putExtra("groupId", groupId);                                      //add group ID data to intent
                startActivityForResult(addWager, ADD_WAGER_REQUEST);                               //start the activity
            }
        });


        //button to return to the users groups page
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToGroupsPage = new Intent(getApplicationContext(), UserGroupsActivity.class);
                startActivity(backToGroupsPage);
            }
        });

        Button invite; //initialize invite button
        invite = findViewById(R.id.invite);

        //set on click listener for the invite button (https://stackoverflow.com/questions/2372248/launch-sms-application-with-an-intent)
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW); //create an intent to go to sms
                sendIntent.setData(Uri.parse("sms:"));

                //put message with the group ID to easily text to your friends
                sendIntent.putExtra("sms_body", "Hey! I'd like to invite you to my group. Use this code to join it on the MoneyBall app! Code: " + groupId);
                startActivity(sendIntent);
                Toast.makeText(getApplicationContext(),  "Invite your friends!", Toast.LENGTH_SHORT).show();
            }
        });

        //opens the group's corresponding chat
        Button groupchat;           //create button
        groupchat = findViewById(R.id.groupchat); //get view
        //set on click listener for the groupchat button
        groupchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toChatPage = new Intent(getApplicationContext(), GroupChatActivity.class); //create an intent to go to the chat page
                toChatPage.putExtra("groupId",groupIdToPass); //pass in the group ID
                startActivity(toChatPage);

            }
        });


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.simple_test_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.profile:
                Toast.makeText(getApplicationContext(), "opening profile page!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ProfilePreferencesPage.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    // https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the
    public void onItemClick(View view, int position) {
        //Get wager data to be passed to wager activity
        ArrayList<String> usersList = ((WagerAdapter) wagerAdapter).getItem(position).getUsersList();
        String description = ((WagerAdapter) wagerAdapter).getItem(position).getDescription();
        String heading = ((WagerAdapter) wagerAdapter).getItem(position).getHeading();
        String pic = ((WagerAdapter) wagerAdapter).getItem(position).getPicture();
        String id = ((WagerAdapter) wagerAdapter).getItem(position).getId();
        String wagerCreator = ((WagerAdapter) wagerAdapter).getItem(position).getWagerCreator();
        ArrayList<String> votesList = ((WagerAdapter) wagerAdapter).getItem(position).getUserVotes();
        double betVal = ((WagerAdapter) wagerAdapter).getItem(position).getBetVal();
        ArrayList<String> challengeList = ((WagerAdapter) wagerAdapter).getItem(position).getChallengeList();

        Intent openWager = new Intent(getApplicationContext(), WagerActivity.class); //create the intent to go to the wager page

        //pass data to intent
        openWager.putExtra("usersList", usersList);
        openWager.putExtra("votesList", votesList);
        openWager.putExtra("description", description);
        openWager.putExtra("heading", heading);
        openWager.putExtra("wagerCreator", wagerCreator);
        openWager.putExtra("group", groupHeading);
        openWager.putExtra("groupDescription", groupDescription);
        openWager.putExtra("groupId", groupIdToPass);
        openWager.putExtra("id", id);
        openWager.putExtra("pic", pic);
        openWager.putExtra("groupPic", groupPicUri.toString());
        openWager.putExtra("betVal", betVal);
        openWager.putExtra("challengeList", challengeList);
        openWager.putExtra("position", position);
        startActivity(openWager);

    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_WAGER_REQUEST) {
            if (resultCode == RESULT_OK) {
                //Get data from create wager activity to be used in adding the wager data to the database
                String imageUri = data.getStringExtra("pic");
                final String heading = data.getStringExtra("headingText");
                final String description = data.getStringExtra("descriptionText");
                final String group = data.getStringExtra("groupIdToPass");
                final String voteVal = data.getStringExtra("voteVal");
                betVal = (double) data.getDoubleExtra("betVal", 1.0D);
                final String potentialChallenge = data.getStringExtra("potentialChallenge");
                Log.d("BET", "onActivityRes: " + betVal);

                final DatabaseReference ref = database.getReference();    //Get database reference
                final DatabaseReference wagerRef = ref.child("groups").child(group).child("wagers").push();//Find specific spot in database to place data (push creates unique key)
                final String key = wagerRef.getKey(); //get the key in order to store it in the wager class

                if(!imageUri.equals("")) {//can't be null as no images selected when pressing means empty string
                   final StorageReference imageStorageReference = storage.getReference().child("images/" + key + ".png");
                   imageStorageReference.putFile(Uri.parse(imageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           Toast.makeText(getApplicationContext(), "upload image success!", Toast.LENGTH_SHORT).show();
                           //get metadata and path from storage
                           StorageMetadata snapshotMetadata = taskSnapshot.getMetadata();
                           Task<Uri> downloadUrl = imageStorageReference.getDownloadUrl();
                           downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {
                                   String imageReference = uri.toString();
                                   FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                   String UID = "";
                                   if(user!=null){
                                       UID = user.getUid();
                                   }
                                   ArrayList<String> usersList = new ArrayList<String>(); //new list of users that have entered the wager
                                   ArrayList<String> votesList = new ArrayList<String>();
                                   votesList.add(voteVal);
                                   usersList.add(UID); //auto add the creator of the wager
                                   ArrayList<String> challengeList = new ArrayList<>();
                                   challengeList.add(potentialChallenge);
                                   Wager newWager = new Wager(key, heading, group, imageReference, description, UID, usersList, true, betVal, challengeList, votesList, wagerResult); //create wager
                                       wagerRef.setValue(newWager); //set the value in the database to be that of the wager

                               }
                           });
                       }
                   });
                }
                else{//this accounts for when the user doesn't select an image for the wager, imageUri will then be empty string.
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String UID = "";
                    if(user!=null){
                        UID = user.getUid();
                    }
                    ArrayList<String> usersList = new ArrayList<String>(); //new list of users that have entered the wager
                    usersList.add(UID); //auto add the creator of the wager
                    ArrayList<String> votesList = new ArrayList<String>();
                    votesList.add(voteVal);
                    ArrayList<String> challengeList = new ArrayList<>();
                    challengeList.add(potentialChallenge);
                   final Wager newWager = new Wager(key, heading, group, "", description, UID, usersList, true, betVal,challengeList, votesList, wagerResult); //create wager
                   wagerRef.setValue(newWager); //set the value in the database to be that of the wager
                }
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(),  "New Wager Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}