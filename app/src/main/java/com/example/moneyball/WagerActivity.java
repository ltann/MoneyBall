package com.example.moneyball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class WagerActivity extends AppCompatActivity {
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    //Initialize some global values
    final int JOIN_WAGER_REQUEST = 123;
    final int CLOSE_WAGER_REQUEST = 1000;
    ArrayList<String> votesListToPass, usersList;
    String groupID, wagerID, headingToPass, descriptionToPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wager);
        Bundle extras = getIntent().getExtras(); //get data from passed intent
        //get data from the intent in the form of strings/doubles
        final String description = extras.getString("description");
        String groupDesc = extras.getString("groupDescription");
        final String heading = extras.getString("heading");
        String group = extras.getString("group");
        final String wagerCreator = extras.getString("wagerCreator");
        final String id = extras.getString("id");
        final String groupId = extras.getString("groupId");
        final double betVal = extras.getDouble("betVal");
        final int position = extras.getInt("position");
        usersList = extras.getStringArrayList("usersList");
        final ArrayList<String> votesList = extras.getStringArrayList("votesList");
        final ArrayList<String> challengeList = extras.getStringArrayList("challengeList");
        Uri groupPicUri = Uri.parse(extras.getString("groupPic"));
        //some need to be global
        groupID = groupId;
        wagerID = id;
        descriptionToPass = description;
        headingToPass = heading;
        votesListToPass = votesList;

        //initialize the UI
        final EditText potentialChallengeText = findViewById(R.id.potentialChallenge);


        //setting the group picture holder
        ImageView groupPicHolder = findViewById(R.id.groupPic);
        Picasso.get().load(groupPicUri).into(groupPicHolder);

        //setting the picture holder with picture url from internet
        Uri picUri = Uri.parse(extras.getString("pic"));//gets the picture from the bundle as a URL
        ImageView wagerPic = findViewById(R.id.wagerPic);
        Picasso.get().load(picUri).into(wagerPic);//use PICASSSOOOO for loading picture urls into imageView or whatever holders u need.

        //Initialize the UI
        final TextView groupName, groupDescription, wagerName, wagerDescription, betValTV, wagerResults;
        groupDescription = findViewById(R.id.groupDescription);
        groupDescription.setText(groupDesc);
        groupName = findViewById(R.id.groupName);
        groupName.setText(group);
        wagerName = findViewById(R.id.wagerName);
        wagerName.setText(heading);
        wagerDescription = findViewById(R.id.wagerDescription);
        wagerDescription.setText(description);
        betValTV = findViewById(R.id.betValTV);
        betValTV.setText("Value: $"+betVal);
        wagerResults = findViewById(R.id.wagerResultsTV);
        wagerResults.setVisibility(INVISIBLE); //set some of the views are set to invisible since the wager is assumed to still be open

        final TextView userResult, winners, losers;
        userResult = findViewById(R.id.userResult);
        winners = findViewById(R.id.winners);
        losers = findViewById(R.id.losers);

        final Button bet, challenge, invite, btn_closeWager;
        bet = findViewById(R.id.bet);
        bet.setVisibility(INVISIBLE);
        challenge = findViewById(R.id.challenge);
        challenge.setVisibility(INVISIBLE);
        invite = findViewById(R.id.invite);
        btn_closeWager = findViewById(R.id.btn_closeWager);

        DatabaseReference ref = database.getReference(); //get db reference
        final DatabaseReference openStatusRef = ref;
        openStatusRef.addValueEventListener(new ValueEventListener() {
            //read the wager data from the database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue(); //get the database data as a hashmap
                HashMap<String, Object> groupsData = (HashMap<String, Object>) dataMap.get("groups");
                HashMap<String, Object> groupData = (HashMap<String, Object>) groupsData.get(groupId);
                HashMap<String, Object> wagersData = (HashMap<String, Object>) groupData.get("wagers");
                HashMap<String, Object> usersData = (HashMap<String, Object>) dataMap.get("users");
                Log.d("db", dataMap.toString());
                if(dataMap!=null) { //check if its null to avoid errors
                    for (String key : wagersData.keySet()) {   //loop through the wagers
                        Object data = wagersData.get(key);
                        HashMap<String, Object> wagerData = (HashMap<String, Object>) data;
                        String wagerId = wagerData.get("id").toString();
                        if(!wagerId.equalsIgnoreCase(id))
                            continue;

                        Boolean openStatus = (Boolean)wagerData.get("openStatus");
                        if(!openStatus){
                            //adjust visibility of views for a closed wager
                            btn_closeWager.setVisibility(View.GONE);
                            invite.setVisibility(View.GONE);
                            userResult.setVisibility(VISIBLE);
                            winners.setVisibility(VISIBLE);
                            losers.setVisibility(VISIBLE);
                            bet.setVisibility(VISIBLE);
                            challenge.setVisibility(VISIBLE);
                            potentialChallengeText.setVisibility(INVISIBLE);
                            wagerResults.setVisibility(VISIBLE);
                            ArrayList<String> votesList = (ArrayList<String>)wagerData.get("userVotes");
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String UID = "";
                            if(user!=null){
                                UID = user.getUid();
                            }
                            ArrayList<String> usersList = (ArrayList<String>)wagerData.get("usersList");
                            String userVote = "NA";
                            if(usersList.contains(UID)) { //to avoid null pointer on the next line
                                userVote = votesList.get(usersList.indexOf(UID)); //find whether the user voted "Yes" or "No" to the bet
                            }
                            String wagerResult = wagerData.get("wagerResult").toString();
                            ArrayList<String> winnerList = new ArrayList<>(), loserList = new ArrayList<>(); //create arraylists to store user IDs of winners and losers
                            for(int i =0; i <votesList.size(); i++){ //loop through arraylist of votes
                                String res = votesList.get(i);
                                HashMap<String, Object> userData = (HashMap<String, Object>)usersData.get(usersList.get(i));
                                HashMap<String, Object> profData = (HashMap<String, Object>)userData.get("profile");
                                String username = (String)profData.get("username"); //get username of the user we are looking at
                                if(res.equalsIgnoreCase(wagerResult)){
                                    winnerList.add(username); //add to winners
                                }
                                else
                                    loserList.add(username); //add to losers
                            }

                            String winnersString = "Winners: "+winnerList.toString(), losersString = "Losers: "+loserList.toString();; //get winners and losers as strings
                            winners.setText(winnersString); //set the text of these views to the winners/losers
                            losers.setText(losersString);
                            String userResultString; //initialize string
                            if(userVote.equalsIgnoreCase(wagerResult)){ //if current user is a winner give them some info
                                userResultString = "You Win!";
                                userResult.setText(userResultString);
                                wagerResults.setText("You win! Watch your friends' dare videos or hop in the chat and ask for your payment from these people: " + loserList.toString());
                            } else if(userVote.equals("NA")){ //if current user is viewing a closed wager that they didnt enter, let them know
                                wagerResults.setText("You weren't part of this wager!");
                            }
                            else{ //if the current user lost the wager, let them know how much they owe the winners each, or inform them to do a dare
                                userResultString = "You Lose...";
                                userResult.setText(userResultString);
                                wagerResults.setText("You lose! Do a dare or pay $" + betVal/winnerList.size() + " to these people: " + winnerList.toString());
                            }
                        } else { //if the wager is still open
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String UID = "";
                            if(user!=null){
                                UID = user.getUid();
                            }
                            ArrayList<String> usersList = (ArrayList<String>)wagerData.get("usersList");
                            if(usersList.contains(UID)) { //as long as the user is in the wager, inform them that the wager has yet to be closed
                                wagerResults.setVisibility(VISIBLE);
                                wagerResults.setText("This wager is still open. Wait for the wager creator to close it to find out how you did!");
                            }
                        }
//                        Toast.makeText(getApplicationContext(), openStatus+", id: "+id, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = "";
        if(user!=null){
            UID = user.getUid();
        }
        if(UID.equals(wagerCreator)){ //check if the current user is the creator of the wager
            btn_closeWager.setText("Close Wager"); //set buttons text to "close wager"
        }
        else if(usersList!=null && usersList.contains(UID)) { //if the user is part of the wager and not the creator
            btn_closeWager.setVisibility(View.GONE);  //hide this button
            potentialChallengeText.setVisibility(View.GONE); //hide the form asking for a challenge idea
        }

        if(btn_closeWager.getText().toString().equalsIgnoreCase("Close Wager")) { //if the current user is the wager creator
            potentialChallengeText.setVisibility(INVISIBLE); //hide the form asking for a challenge, since they have already suggested one when creating the wager
        }
        btn_closeWager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sets the wager openStatus to false (closed)
                if(btn_closeWager.getText().toString().equalsIgnoreCase("Close Wager")) {
                    DatabaseReference ref = database.getReference(); //get db reference
                    final DatabaseReference openStatusRef = ref.child("groups").child(groupId).child("wagers").child(id).child("openStatus"); //get reference to openStatus of the wager
                    openStatusRef.setValue(false); //sets the value to false
                    Toast.makeText(getApplicationContext(), "Wager Closed", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), CloseWagerActivity.class); //create intent to close wager activity
                    intent.putExtra("heading", headingToPass); //pass data into the intent
                    intent.putExtra("description", descriptionToPass);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("id", id);
                    btn_closeWager.setVisibility(View.GONE); //make the button invisible
                    startActivityForResult(intent, CLOSE_WAGER_REQUEST); //start the intent
                }
                else{ //if the current user is not the wager creator, this button allows them to join the wager instead.
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get user ID
                    String UID = "";
                    if(user!=null){
                        UID = user.getUid();
                    }
                    DatabaseReference ref = database.getReference();
                    Intent intent = new Intent(getApplicationContext(), JoinWagerActivity.class); //create intent to go to join wager activity
                    String potentialChallenge = potentialChallengeText.getText().toString();      //get the potential challenge that the user has entered
                    if(potentialChallenge.equals(""))                                             //check that the user entered a challenge and if not inform them that they must
                        Toast.makeText(getApplicationContext(),  "Enter one potential challenge!", Toast.LENGTH_SHORT).show();
                    else {
                        challengeList.add(potentialChallenge);  //add this challenge to the list of challenges that other users have entered
                        final DatabaseReference challengeListRef = ref.child("groups").child(groupId).child("wagers").child(id).child("challengeList");
                        challengeListRef.setValue(challengeList); //set the value of the above reference to the challenge list
                        Toast.makeText(getApplicationContext(), "Joined Wager", Toast.LENGTH_SHORT).show(); //inform the user that they have joined the wager
                        btn_closeWager.setVisibility(View.GONE); //hide the button since the user has joined
                        potentialChallengeText.setVisibility(View.GONE); //hide this form for the same reason

                        //pass important data through the intent
                        intent.putExtra("description", descriptionToPass);
                        intent.putExtra("heading", headingToPass);
                        intent.putExtra("userID", UID);
                        intent.putExtra("groupID", groupId);
                        intent.putExtra("wagerID", id);
                        intent.putExtra("votesList", votesListToPass);
                        startActivityForResult(intent, JOIN_WAGER_REQUEST);
                    }
                }
            }
        });

        //create on click listener for the "bet" button, which allows user to pay what they owe
        bet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent payIntent = new Intent(WagerActivity.this,  PaymentActivity.class); //create intent to go to payment activity
                payIntent.putExtra("betVal",betVal); //put data into the intent
                startActivity(payIntent); //start the activity
                Toast.makeText(getApplicationContext(),  "Select a Payment Option", Toast.LENGTH_LONG).show();
            }
        });

        //create on click listener for the "challenge" button, which allows a user to submit their dare video if they dont want to pay
        challenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(WagerActivity.this,  DareActivity.class); //create intent to go to the dare activity
                Toast.makeText(getApplicationContext(),"Spin for Challenge", Toast.LENGTH_LONG).show(); //create a toast
                myIntent.putExtra("challengeList", challengeList); //pass the list of challenges through the intent
                startActivity(myIntent); //start the activity
            }
        });

        //set on click listener for the invite button (https://stackoverflow.com/questions/2372248/launch-sms-application-with-an-intent)
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);//create an intent to go to sms
                sendIntent.setData(Uri.parse("sms:"));
                //put message with the group ID to easily text to your friends
                sendIntent.putExtra("sms_body", "Hey! I'd like to invite you to my group. Use this code to join it on the MoneyBall app! Code: " + groupId);
                startActivity(sendIntent);
                Toast.makeText(getApplicationContext(),  "Invite your friends!", Toast.LENGTH_SHORT).show();
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

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JOIN_WAGER_REQUEST) { //for when a user has requested to join the wager
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get current user
                String UID = "";
                if (user != null) {
                    UID = user.getUid();
                }
                if (usersList.contains(UID) == false) { //check if the users list already contains this ID to avoid duplicates
                    usersList.add(UID); //add this user to the list of users
                }
                DatabaseReference ref = database.getReference(); //get db reference
                final DatabaseReference usersListRef = ref.child("groups").child(groupID).child("wagers").child(wagerID).child("usersList");
                usersListRef.setValue(usersList); //set the users list value to the updated list

                String vote = data.getStringExtra("vote"); //get the vote that was passed back from the join wager activity
                final DatabaseReference wagerVoteRef = ref.child("groups").child(groupID).child("wagers").child(wagerID).child("userVotes");
                votesListToPass.add(vote); //add this vote to the list of votes
                wagerVoteRef.setValue(votesListToPass); //set the value in the database to the updated list of votes
            }
        } else if (requestCode == CLOSE_WAGER_REQUEST) {
            if (resultCode == RESULT_OK) {

            }
        }
    }
}
