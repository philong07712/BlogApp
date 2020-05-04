package com.example.blogapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blogapp.Adapters.CommentAdapter;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    RecyclerView rvComments;
    ImageView postImg, postUser, postCommentImg;
    TextView tvDate, tvTitle, tvDescription;
    EditText editComment;
    Button postAdd;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    String postKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        // Make the action bar hide
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        // setup view
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        postImg = findViewById(R.id.post_detail_img);
        postUser = findViewById(R.id.post_detail_poster_img);
        postCommentImg = findViewById(R.id.post_detail_user_img);
        tvDate = findViewById(R.id.post_detail_date_name);
        tvTitle = findViewById(R.id.post_detail_title);
        tvDescription = findViewById(R.id.post_detail_description);
        editComment = findViewById(R.id.post_detail_comment);
        postAdd = findViewById(R.id.post_detail_add_comment);
        rvComments = findViewById(R.id.rv_comments);
        postKey = getIntent().getExtras().getString("postKey");
        // setup Comments recyclerView

        setUpCommentRecyclerView();

        // get the postKey
        // set up all view in the detail
        // setup title
        String title = getIntent().getExtras().getString("Title");
        tvTitle.setText(title);
        // setup description
        String description = getIntent().getExtras().getString("Description");
        tvDescription.setText(description);
        // setup post date
        Long time = getIntent().getExtras().getLong("postDate");
        String date = convertTimeStamp(time);
        // get poster name
        String name = getIntent().getExtras().getString("posterName");
        String timeAndName = date + " by " + name;
        tvDate.setText(timeAndName);
        // setup post image
        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(postImg);
        // setup post user image
        String posterImage = getIntent().getExtras().getString("posterImage");
        Glide.with(this).load(posterImage).into(postUser);
        // setup with comment image
        if (user.getPhotoUrl() != null)
        {
            Glide.with(this).load(user.getPhotoUrl()).into(postCommentImg);
        }
        else
        {
            postCommentImg.setImageResource(R.drawable.userphoto);
        }

        // set up comment listerner
        postAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editComment.getText().toString().isEmpty())
                {
                    createComment();
                }
            }
        });
    }

    private void createComment()
    {
        postAdd.setVisibility(View.INVISIBLE);
        String content = editComment.getText().toString();
        String name = user.getDisplayName();
        String img = user.getPhotoUrl().toString();
        String id = user.getUid();
        Comment comment = new Comment(id, name, img, content);
        addComment(comment);
    }

    private void addComment(Comment comment)
    {
        DatabaseReference reference = firebaseDatabase.getReference("Comments").child(postKey).push();
        String key = reference.getKey();

        comment.setKey(key);
        reference.setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    // if comment successfully push to database
                    showMessage("Comment added successfully");
                    postAdd.setVisibility(View.VISIBLE);
                }
                else
                {
                    // if push data to database is wrong
                    showMessage(task.getException().getMessage());
                    postAdd.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String convertTimeStamp(long time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
        return date;
    }

    private void setUpCommentRecyclerView()
    {
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference reference = firebaseDatabase.getReference("Comments").child(postKey);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren())
                {
                    Comment comment = snap.getValue(Comment.class);
                    comments.add(comment);
                }
                CommentAdapter adapter = new CommentAdapter(getApplicationContext(), comments);
                rvComments.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
