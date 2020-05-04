package com.example.blogapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blogapp.Activities.LoginActivity;
import com.example.blogapp.Activities.RegisterActivity;
import com.example.blogapp.Models.Post;
import com.example.blogapp.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final int PRegCode = 2;
    private final int REQUESTCODE = 2;
    // custom variable
    TextView userName, userEmail;
    ImageView userPhoto;
    DrawerLayout drawer;
    NavController navController;
    Uri pickedImageUri = null;
    // add dialog
    Dialog popAddPost;
    ImageView popupUserImage, popupImage, popupCreate;
    EditText popupUserTitle, popupUserDescription;
    ProgressBar popupProgressBar;

    // firebase auth
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();


    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // init custom variable
        initPopup();
        setUpPopup();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // custom method

        navigationView.setNavigationItemSelectedListener(this);
        // custom function
        updateNavigationBar();
    }

    private void initPopup() {

        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setGravity(Gravity.TOP);
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        // init image for popup
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupImage = popAddPost.findViewById(R.id.popup_image);
        popupCreate = popAddPost.findViewById(R.id.popup_create);
        popupProgressBar = popAddPost.findViewById(R.id.popup_progressBar);
        popupUserTitle = popAddPost.findViewById(R.id.popup_title);
        popupUserDescription = popAddPost.findViewById(R.id.popup_description);
        // load user image to popup

        Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl()).into(popupUserImage);
    }

    private void setUpPopup()
    {
        popupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestForPermission();
            }
        });
        // set listener to create
        popupCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupCreate.setVisibility(View.INVISIBLE);
                popupProgressBar.setVisibility(View.VISIBLE);
                if (!popupUserTitle.getText().toString().isEmpty() &&
                    !popupUserDescription.getText().toString().isEmpty() &&
                    pickedImageUri != null)
                {
                    // If user have completed all fields to create post
                    // first we need to upload the picked image to sever
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("post_photos");
                    final StorageReference imageFilePath = storageReference.child(pickedImageUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String image = uri.toString();
                                    // we need to create a Post Model
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    Post post = new Post(popupUserTitle.getText().toString(),
                                            popupUserDescription.getText().toString(),
                                            image,
                                            currentUser.getUid(),
                                            currentUser.getPhotoUrl().toString(),
                                            currentUser.getDisplayName());
                                    addPost(post);
                                }
                            });
                        }
                    });
                }

                else
                {
                    showMessage("Please fill all field");
                    popupProgressBar.setVisibility(View.INVISIBLE);
                    popupCreate.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").child(currentUser.getUid()).push();

        // next we will get the unique key
        String key = myRef.getKey();
        post.setKey(key);

        // put post to database
        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Post added successfully");
                popupCreate.setVisibility(View.VISIBLE);
                popupProgressBar.setVisibility(View.INVISIBLE);
                popAddPost.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_add_friend:
                initFriendDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initFriendDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Add friend");
        final EditText inputFriendEmail = new EditText(this);
        alertDialog.setView(inputFriendEmail);
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This will get email that user input in the dialog
                String friendEmail = inputFriendEmail.getText().toString();
                // This will convert email to uid
                String friendUID = convertEmailToUID(friendEmail);
                // This will add friend's id to current user
            }
        }).setNegativeButton("Cancel", null);
        alertDialog.show();
    }

    public String convertEmailToUID(final String email)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // this will get all the user
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        // we will search for all the user email
                        // if it is equal to the email, we log it
                        Query query = data.getRef().orderByChild("userEmail").equalTo(email);
                        // we will retrieve data from query
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // All the related friend
                                List<User> userSearching = new ArrayList<>();
                                if (dataSnapshot.exists())
                                {
                                    for (DataSnapshot allRelatedUID : dataSnapshot.getChildren())
                                    {
                                        // make the user store email
                                        // then return that uid
                                        User user = allRelatedUID.getValue(User.class);
                                        userSearching.add(user);
                                        logMessage(user.toString());
                                        addFriendToUser(currentUser.getUid(), user.getUserId());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return null;
    }


    public void addFriendToUser(String userUID, String friendUID)
    {
        new FireBaseFriendHelper().addFriend(userUID, friendUID, new FireBaseFriendHelper.IFireBaseFriendHelper() {
            @Override
            public void onAdd() {
                showMessage("Add Friend completed");
            }

            @Override
            public void onRead(List<String> keys) {

            }

            @Override
            public void onUpdate() {

            }

            @Override
            public void onDelete() {

            }
        });
    }


    public void logMessage(String message)
    {
        Log.d("MyHome", message);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateNavigationBar()
    {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        // init custom view
        userEmail = header.findViewById(R.id.nav_user_email);
        userName = header.findViewById(R.id.nav_user_name);
        userPhoto = header.findViewById(R.id.nav_user_photo);

        FirebaseUser user = mAuth.getCurrentUser();
        userName.setText(user.getDisplayName());
        userEmail.setText(user.getEmail());
        // init image using Glide
        if (user.getPhotoUrl() != null)
        {
            Glide.with(MainHome.this).load(user.getPhotoUrl()).into(userPhoto);
        }
        else
        {
            userPhoto.setImageResource(R.drawable.userphoto);
        }
    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    // this session we will check and request to add a picture

    private void checkAndRequestForPermission()
    {
        if (ContextCompat.checkSelfPermission(MainHome.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Check if the user click never show again permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainHome.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                showMessage("not granted permission");
            }
            // if user not click then we will send another permission to the user again
            else
            {
                ActivityCompat.requestPermissions(MainHome.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PRegCode);
            }
        }

        else
        {
            openGallery();
        }
    }

    private void openGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null)
        {
            pickedImageUri = data.getData();
            popupImage.setImageURI(pickedImageUri);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_home:
                navController.navigate(R.id.homeFragment);
                break;
            case R.id.nav_profile:
                navController.navigate(R.id.profileFragment);
                break;
            case R.id.nav_setting:
                navController.navigate(R.id.settingFragment);
                break;
            case R.id.nav_logout:
                logOut();
                break;
        }
        drawer.closeDrawers();
        return true;
    }

    private void logOut()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        mAuth.signOut();
        startActivity(intent);
        finish();
    }

}
