package com.example.moneyball;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class ProfilePreferencesPage extends AppCompatActivity {
    private static final String SCHEME = "package";

    private static final String APP_PKG_NAME_21 = "com.example.moneyball";

    private static final String APP_PKG_NAME_22 = "pkg";

    private static final String APP_DETAILS_PACKAGE_NAME = "com.example.moneyball";

    private static final String APP_DETAILS_CLASS_NAME = "com.example.moneyball.InstalledAppDetails";

    private final int PICK_IMAGE = 1, REQUEST_READ_STORAGE = 100;

    Drawable bg;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    final DatabaseReference db_ref = database.getReference();
    final private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ImageButton uploadProfilePic;
    private RelativeLayout profilePicHolder;
    private TextView emailTV;
    private EditText usernameET;
    private Uri profilePicUri;
    private Button notifyButton;
    private Button sendNotifyButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        emailTV = findViewById(R.id.emailTV);
        if(user.getEmail() != null){
            emailTV.setText(user.getEmail());
        }
        else{
            emailTV.setText("Anonymous");
        }

        StorageReference ref = storage.getReference();
        ref.child("profile_pictures/" + user.getUid() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                profilePicHolder = findViewById(R.id.profilePicHolder);

                Picasso.get().load(uri).into(new Target(){
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        profilePicHolder.setBackground(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e("TAG", "Failed");
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        Log.e("TAG", "Prepare Load");
                    }
                });
            }
        });

        final TextView uploadTV = findViewById(R.id.uploadTV);
        uploadProfilePic = findViewById(R.id.uploadProfilePic);
        uploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ProfilePreferencesPage.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Toast.makeText(getApplicationContext(), "We need permission to upload pictures to associate with your wager", Toast.LENGTH_LONG).show();
                        } else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(ProfilePreferencesPage.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
                        }
                    } else {
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
                else {
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

        final DatabaseReference username_ref = db_ref.child("users/" + user.getUid()).child("profile").child("username");
        usernameET = findViewById(R.id.usernameInput);
        username_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = (String) dataSnapshot.getValue();//we can do this because we are already at the username key and we are just getting access to what "username" holds
                if (username != null) {
                    usernameET.setText(username);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String username = usernameET.getText().toString();
                if (!username.equals("")){
                    username_ref.setValue(username);
                }

                final StorageReference ref = storage.getReference();
                if (profilePicUri != null) {
                    final StorageReference profilePicRef = ref.child("profile_pictures/" + user.getUid() + ".png");
                    profilePicRef.putFile(profilePicUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    profilePicUri = uri;
                                }
                            });
                        }

                    });
                }
                finish();
            }
        });

        notifyButton = findViewById(R.id.notifications);
        notifyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showInstalledAppDetails(getApplicationContext(), "com.example.moneyball");
            }
        });
    }

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                profilePicUri = data.getData();
                RelativeLayout layout = findViewById(R.id.profilePicHolder);
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(profilePicUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bg = Drawable.createFromStream(inputStream, profilePicUri.toString());
                layout.setBackground(bg);
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

            // other 'case' lines to check for other permissions this app might request.
        }
    }
}
