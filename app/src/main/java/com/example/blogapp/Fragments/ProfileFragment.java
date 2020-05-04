package com.example.blogapp.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.blogapp.FireBaseCommentHelper;
import com.example.blogapp.FireBasePostHelper;
import com.example.blogapp.FireBaseUserHelper;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.Models.Post;
import com.example.blogapp.Models.User;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static int PRegCode = 1;
    static int REQUESTCODE = 1;
    Uri pickedImgUri = null;
    Uri pickedImgAvatar;
    Uri pickedImgBackground;
    // init custom view
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    User mUser;
    ImageView background;
    ImageView avatar;
    ProgressBar progressBar;
    TextView name, email;
    Button editButton;
    View view;
    private int PICKED_FLAGS = 1;

    // TODO: Rename and change types of parameters

    public ProfileFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        getUser(currentUser.getUid());
        // init view
        return view;
    }


    private void getUser(String uid) {
        logMessage(uid);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
                initView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        avatar = view.findViewById(R.id.edit_profile_avatar);
        background = view.findViewById(R.id.edit_profile_background);
        email = view.findViewById(R.id.edit_profile_email);
        name = view.findViewById(R.id.edit_profile_name);
        editButton = view.findViewById(R.id.edit_profile_btn);
        progressBar = view.findViewById(R.id.edit_profile_progress);
        Glide.with(this).load(mUser.getUserPhoto()).into(avatar);
        if (mUser.getBackground() != null) {
            Glide.with(this).load(mUser.getBackground()).into(background);
        }
        email.setText(mUser.getUserEmail());
        name.setText(mUser.getName());

        setListener();
    }


    private void setListener() {
        // Add listener to change the avatar
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PICKED_FLAGS = 1;
                checkAndRequestForPermisson();
            }
        });
        // set Listener to change the background
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PICKED_FLAGS = 2;
                checkAndRequestForPermisson();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUser();
            }
        });
    }


    private void logMessage(String message) {
        Log.d("MyHome", message);
    }

    private void checkAndRequestForPermisson() {
        // Determine is that a granted permission for go to gallery
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check if the user click never show again permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity().getApplicationContext(), "Not granted permission", Toast.LENGTH_SHORT).show();
            }
            // if user not click then we will send another permission to the user again
            else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PRegCode);
            }
        }
        // If the user have granted the permission to go to the gallery
        else {
            openGallery();
        }

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && requestCode == REQUESTCODE && data != null) {
            // The user has successful pick the image
            // we need to save it preference
            pickedImgUri = data.getData();
            switch (PICKED_FLAGS) {
                case 1:
                    // This will be the case we want to change the avatar
                    avatar.setImageURI(pickedImgUri);
                    loadImageToStorage("users_photo", pickedImgUri);
                    break;
                case 2:
                    background.setImageURI(pickedImgUri);
                    loadImageToStorage("users_background", pickedImgUri);
                    // This will be the case we want to change the background
            }
        }
    }


    private void editUser() {
        inProgressEdit(true);
        final User newUser = new User(mUser);
        new FireBaseUserHelper().updateUser(newUser.getUserId(), newUser, new FireBaseUserHelper.IFireBaseHelper() {
            @Override
            public void onAdd() {

            }

            @Override
            public void onRead(List<User> users, List<String> keys) {

            }

            @Override
            public void onUpdate() {
                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newUser.getName())
                        .setPhotoUri((Uri.parse(newUser.getUserPhoto())))
                        .build();

                currentUser.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // We upload the user profile to firebase auth
                        editPosts();
                    }
                });
            }

            @Override
            public void onDelete() {
            }
        });
    }

    private void editPosts() {
        new FireBasePostHelper().readPosts(mUser.getUserId(), new FireBasePostHelper.IFireBasePostHelper() {
            @Override
            public void onRead(List<Post> posts, List<String> keys) {
                posts = updatePost(posts);
                new FireBasePostHelper().updatePosts(mUser.getUserId(), keys, posts, new FireBasePostHelper.IFireBasePostHelper() {
                    @Override
                    public void onRead(List<Post> posts, List<String> keys) {
                    }

                    @Override
                    public void onAdd() {

                    }

                    @Override
                    public void onUpdate() {

                    }

                    @Override
                    public void onDelete() {

                    }
                });
            }

            @Override
            public void onAdd() {

            }

            @Override
            public void onUpdate() {

            }

            @Override
            public void onDelete() {

            }
        });

    }


    private List<Post> updatePost(List<Post> posts) {
        List<Post> newPosts = new ArrayList<>();
        for (Post post : posts) {
            Post newPost = new Post(post);
            // we will change some field of the post
            // TODO: if we change something else except name and user photo then we need to update this
            newPost.setName(mUser.getName());
            newPost.setUserPhoto(mUser.getUserPhoto());

            newPosts.add(newPost);

            // This will edit the comments for us
            editComments(newPost);

        }

        return newPosts;
    }

    private void editComments(final Post post) {
        new FireBaseCommentHelper().readCommentWithUserID(post.getKey(), mUser.getUserId(), new FireBaseCommentHelper.ICommentRead() {
            @Override
            public void onRead(List<Comment> comments, List<String> keys) {
                comments = updateComment(comments);
                new FireBaseCommentHelper().updateComments(post.getKey(), keys, comments, new FireBaseCommentHelper.ICommentUpdate() {
                    @Override
                    public void onUpdate() {
                        showMessage("Update users successfully");
                        inProgressEdit(false);
                    }
                });
            }
        });
    }

    private List<Comment> updateComment(List<Comment> comments) {
        List<Comment> newComments = new ArrayList<>();
        for (int i = 0; i < comments.size(); i++) {
            Comment newComment = new Comment(comments.get(i));
            // TODO: now we will only change avatar and name
            newComment.setImg(mUser.getUserPhoto());
            newComment.setName(mUser.getName());
            // add the new comment to the list of comment
            newComments.add(newComment);
        }

        return newComments;
    }


    private void loadImageToStorage(String key, Uri imgUri) {
        // we will set progress to true
        inProgressEdit(true);
        final StorageReference reference = FirebaseStorage.getInstance().getReference().child(key).child(mUser.getName());
        reference.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // If the image got download back successful
                            switch (PICKED_FLAGS) {
                                case 1:
                                    // This will be the case we upload the avatar
                                    pickedImgAvatar = uri;
                                    showMessage("Avatar change successful");
                                    // We will change the avatar uri
                                    mUser.setUserPhoto(pickedImgAvatar.toString());
                                    break;
                                case 2:
                                    // This will be the case we upload the background
                                    pickedImgBackground = uri;
                                    showMessage("Background change successful");
                                    // We will change the background uri
                                    mUser.setBackground(pickedImgBackground.toString());
                                    break;
                            }
                            // we will set progress to false
                            inProgressEdit(false);
                        }
                    });
                }

                // If image upload got error
                else {
                    showMessage("Image change failed " + task.getException().getMessage());
                }
            }
        });
    }

    private void inProgressEdit(Boolean isProgress) {
        if (isProgress) {
            editButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

