package com.example.moneyball;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UserGroupsActivity extends AppCompatActivity implements GroupAdapter.ItemClickListener {
    private RecyclerView.Adapter groupAdapter;
    private RecyclerView groupList;

    public static final int ADD_WAGER_REQUEST = 1;
    private final int NEW_GROUP = 123;
    private final int ADD_GROUP = 321;

    ArrayList<Group> groups;        //an arraylist of group objects to add the user's groups into
    ArrayList<String> usersGroups;  //an arraylist of strings to hold the group IDs a user is in

    final FirebaseDatabase database = FirebaseDatabase.getInstance();   //get database instance
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get the current user
    FirebaseStorage storage = FirebaseStorage.getInstance();            //get the firebase storage instance (for images)
    DatabaseReference ref = database.getReference();                    //get the reference to the database


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group);
        groupList = findViewById(R.id.groupList);

        groups = new ArrayList<>();
        usersGroups = new ArrayList<>();

        int numOfColumns = 1;
        RecyclerView.LayoutManager recyclerManager = new GridLayoutManager(getApplicationContext(), numOfColumns);
        groupList.setLayoutManager(recyclerManager);

        DatabaseReference groupRef = ref;           //set reference to read data
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();                     //make sure the groups arraylist is empty so as not to add duplicate data

                String UID = "";        //get the user ID
                if(user!=null){         //avoid errors
                    UID = user.getUid();
                }
                HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();    //get hashmap of data
                HashMap<String, Object> objData = (HashMap<String, Object>) dataMap.get("groups");      //get group data
                HashMap<String, Object> userData = (HashMap<String, Object>) dataMap.get("users");      //get user data
                HashMap<String, Object> userSpecificData = (HashMap<String, Object>) userData.get(UID); //get data specific to the current user
                HashMap<String, Object> userGroupData = (HashMap<String, Object>) userSpecificData.get("groups");   //get the data of which groups this user is in

                if(userGroupData!=null) {   //avoid errors
                    for (String key : userGroupData.keySet()) {
                        Object data = userGroupData.get(key);   //get group IDs
                        usersGroups.add(data.toString());       //add the group IDs to the arraylist
                    }
                }

                if(objData!=null) { //avoid errors
                    for (String key : objData.keySet()) {
                        Object data = objData.get(key);
                        if(usersGroups.contains(key)){  //only get group info for the groups a user is in
                            HashMap<String, Object> groupData = (HashMap<String, Object>) data;     //get the group data for the current group
                            String heading = groupData.get("heading").toString();                   //get heading, description, groupCreator, group picture, and chat ID
                            String description = groupData.get("description").toString();
                            String groupCreator = groupData.get("groupCreator").toString();
                            String groupPic =  groupData.get("picUri").toString();
                            String chatKey = groupData.get("chatId").toString();
                            Group newGroup = new Group(key, heading, description, groupCreator, groupPic, chatKey); //create the group object
                            groups.add(newGroup);                                                                   //add the group to the arraylist of groups
                            groupAdapter.notifyDataSetChanged();                                                    //update the view
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        groupAdapter = new GroupAdapter(groups);
        groupList.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
        ((GroupAdapter) groupAdapter).setClickListener(this);

        //Click on the floating button to add or join a group
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //leads to create group activity
                Intent addGroup = new Intent(getApplicationContext(), CreateGroupActivity.class);
                startActivityForResult(addGroup, ADD_WAGER_REQUEST);
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
                startActivity(new Intent(this, ProfilePreferencesPage.class));
                return true;
            case R.id.stats:
                Toast.makeText(getApplicationContext(),  "Getting NBA Statistics! Please wait...", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, NbaActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    // https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the
    public void onItemClick(View view, int position) {
        //View the group you click on
        Intent viewGroup = new Intent(getApplicationContext(), GroupActivity.class);
        //put group data into intent to be passed to group activity
        viewGroup.putExtra("groupId",((GroupAdapter) groupAdapter).getItem(position).getId());
        viewGroup.putExtra("heading",((GroupAdapter) groupAdapter).getItem(position).getHeading());
        viewGroup.putExtra("description",((GroupAdapter) groupAdapter).getItem(position).getDescription());
        viewGroup.putExtra("groupPic", ((GroupAdapter) groupAdapter).getItem(position).getPicUri());
        startActivity(viewGroup);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_WAGER_REQUEST) {
            if (resultCode == NEW_GROUP) {
                //This is called if a user creates a new group
                //get data passed from create group activity
                final String heading = data.getStringExtra("headingText");
                final String description = data.getStringExtra("descriptionText");
                final String groupCreator = data.getStringExtra("groupCreator");
                String grouppic = data.getStringExtra("pic");

                DatabaseReference ref = database.getReference(); //get db reference
                final DatabaseReference groupRef = ref.child("groups").push(); //get the path to store the data
                final String key = groupRef.getKey(); //get the key for storing in a users database
                final DatabaseReference chatRef = ref.child("chats").push(); //adds chats to the database
                final String chatKey = chatRef.getKey(); // get chat key to store in group and user database

                //get current users ID
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String UID = "";
                if (user != null) {
                    UID = user.getUid();
                }

                final DatabaseReference userGroupsRef = ref.child("users").child(UID).child("groups").push(); //get path to store groupID under a users data
                userGroupsRef.setValue(groupRef.getKey()); //store the group ID (so we know what groups a user is in)=

                assert grouppic != null;
                if (!grouppic.equals("")) {//can't be null as no images selected when pressing means empty string
                    final StorageReference imageStorageReference = storage.getReference().child("images/groups/" + key + ".png");
                    imageStorageReference.putFile(Uri.parse(grouppic)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                    Group newGroup = new Group(key, heading, description, groupCreator, imageReference,chatKey);
                                    groupRef.setValue(newGroup);
                                    Chat newChat = new Chat(groupRef.getKey()); //create new chat object by passing on new group key
                                    chatRef.setValue(newChat); //set new chat in database
                                }
                            });
                        }
                    });
                } else {
                    Group newGroup = new Group(key, heading, description, groupCreator, grouppic,chatKey); //create the group, picUri = "" here
                    groupRef.setValue(newGroup); //set the db value
                    Chat newChat = new Chat(groupRef.getKey()); //create new chat object by passing on new group key
                    chatRef.setValue(newChat); //set new chat in database

                }
            }
            if (resultCode == ADD_GROUP) {
                //This is called if a user adds an existing group
                //To add the groupID field to a users data in the database
                String id = data.getStringExtra("id"); //get the group ID
                DatabaseReference ref = database.getReference(); //get the db reference

                //get the ID of the current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String UID = "";
                if (user != null) {
                    UID = user.getUid();
                }

                DatabaseReference userGroupsRef = ref.child("users").child(UID).child("groups").push(); //get the correct path to write the data
                userGroupsRef.setValue(id); //write the data
            }
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(),  "New Group Canceled", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
