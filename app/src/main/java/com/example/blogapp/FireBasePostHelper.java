package com.example.blogapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.blogapp.Models.Post;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FireBasePostHelper {
    FirebaseDatabase database;
    DatabaseReference reference;

    public interface IFireBasePostHelper
    {
        void onRead(List<Post> posts, List<String> keys);
        void onAdd();
        void onUpdate();
        void onDelete();
    }

    public FireBasePostHelper() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Posts");
    }

    public void readPosts(String userID, final IFireBasePostHelper helper)
    {
        DatabaseReference filePath = reference.child(userID);
        filePath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Post> posts = new ArrayList<>();
                List<String> keys = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    String key = data.getKey();
                    Post post = data.getValue(Post.class);
                    posts.add(post);
                    keys.add(key);
                }
                // We will return the posts and keys
                helper.onRead(posts, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updatePost(String userID, String key, Post post, final IFireBasePostHelper helper)
    {
        DatabaseReference filePath = reference.child(userID).child(key);
        filePath.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                helper.onUpdate();
            }
        });
    }

    public void updatePosts(String userID, final List<String> keys, List<Post> posts, final IFireBasePostHelper helper)
    {
        DatabaseReference filePath = reference.child(userID);
        for (int i = 0; i < keys.size(); i++)
        {
            String key = keys.get(i);
            Post post = posts.get(i);
            final int index = i;
            updatePost(userID, key, post, new IFireBasePostHelper() {
                @Override
                public void onRead(List<Post> posts, List<String> keys) {
                }

                @Override
                public void onAdd() {

                }

                @Override
                public void onUpdate() {
                    Log.d("MyHome", "Index : " + Integer.toString(index));
                    if (index == keys.size() - 1)
                    {
                        helper.onUpdate();
                    }
                }

                @Override
                public void onDelete() {

                }
            });
        }
    }

}
