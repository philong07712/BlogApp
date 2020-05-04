package com.example.blogapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blogapp.MainHome;
import com.example.blogapp.Models.User;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView imgUserPhoto;

    static int PRegCode = 1;
    static int REQUESTCODE = 1;
    Uri pickedImgUri = null;

    private EditText userEmail, userName, userPassword, userPassword2;
    private Button regButton;
    private ProgressBar regLoading;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // init View
        this.imgUserPhoto = findViewById(R.id.regUserPhoto);
        this.userEmail = findViewById(R.id.regEmail);
        this.userName = (EditText) findViewById(R.id.regName);
        this.userPassword = (EditText) findViewById(R.id.regPassword);
        this.userPassword2 = (EditText) findViewById(R.id.regPassword2);
        this.regButton = (Button) findViewById(R.id.regBtn);
        this.regLoading = (ProgressBar) findViewById(R.id.regProgress);


        // make the loading bar invisible
        this.regLoading.setVisibility(View.INVISIBLE);

        // set Click listener for the register button

        this.regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regButton.setVisibility(View.INVISIBLE);
                regLoading.setVisibility(View.VISIBLE);
                // get the string from the editText

                String email = userEmail.getText().toString();
                String name = userName.getText().toString();
                String password = userPassword.getText().toString();
                String password2 = userPassword2.getText().toString();

                mAuth = FirebaseAuth.getInstance();

                if (email.isEmpty() && name.isEmpty() && password.isEmpty() && !password.equals(password2))
                {
                    showMessage("Please verify all field");
                }

                else
                {
                    // Every is fine, so we will create user account
                    createUserAccount(email, name, password);
                }
            }
        });

        imgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 22)
                    {
                        checkAndRequestForPermisson();

                    }

                    else
                    {
                        openGallery();
                    }

            }
        });
    }

    private void createUserAccount(String email, final String name, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // The account has been successful created
                if (task.isSuccessful())
                {
                    showMessage("Account created");
                    if (pickedImgUri == null)
                    {
                        updateUserInfoWithoutPhoto(name, mAuth.getCurrentUser());
                    }
                    else
                    {
                        updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());
                    }
                }

                else
                {
                    showMessage("Account created failed" + task.getException().getMessage());
                    regLoading.setVisibility(View.INVISIBLE);
                    regButton.setVisibility(View.VISIBLE);
                }
            }
        });


    }

    private void updateUserInfo(final String name, final Uri pickedImgUri, final FirebaseUser currentUser) {

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photo/" + name);
        // set the name of the path to segment of pickedImgUri
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("MyHome", uri.toString() + "////" + pickedImgUri.toString());
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    initCustomUser();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateUserInfoWithoutPhoto(final String name, final FirebaseUser currentUser) {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photo/" + name);
        // set the name of the path to segment of pickedImgUri
        final StorageReference imageFilePath = mStorage;
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(null)
                .build();

        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    initCustomUser();
                }
            }
        });
    }

    private void initCustomUser()
    {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        String img;
        if (currentUser.getPhotoUrl() == null)
        {
            img = "";
        }
        else
        {
            img = currentUser.getPhotoUrl().toString();
        }
        String uid = currentUser.getUid();
        User user = new User(name, email, uid, img);
        createCustomUser(user);

    }

    private void createCustomUser(User user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // This will create a user data to store information
        DatabaseReference reference = database.getReference("Users").child(user.getUserId()).push();
        // Set key to the user
        String key = reference.getKey();
        user.setKey(key);
        reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    updateUI();
                }
                else
                {
                    showMessage("Adding custom user failed " + task.getException().getMessage());
                    regLoading.setVisibility(View.INVISIBLE);
                    regButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void updateUI() {
        Intent intent = new Intent(getApplicationContext(), MainHome.class);
        startActivity(intent);
    }

    private void showMessage(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void checkAndRequestForPermisson()
    {
        // Determine is that a granted permission for go to gallery
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Check if the user click never show again permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Toast.makeText(RegisterActivity.this, "Not granted permission", Toast.LENGTH_SHORT).show();
            }
            // if user not click then we will send another permission to the user again
            else
            {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PRegCode);
            }
        }
        // If the user have granted the permission to go to the gallery
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
            // The user has successful pick the image
            // we need to save it preference
            pickedImgUri = data.getData();
            imgUserPhoto.setImageURI(pickedImgUri);
        }
    }
}
