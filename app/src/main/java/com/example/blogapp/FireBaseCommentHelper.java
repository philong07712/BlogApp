package com.example.blogapp;

import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.example.blogapp.Models.Comment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FireBaseCommentHelper {

    FirebaseDatabase database;
    DatabaseReference reference;

    public FireBaseCommentHelper() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Comments");
    }

    public interface ICommentUpdate
    {
        void onUpdate();
    }

    public interface ICommentRead
    {
        void onRead(List<Comment> comments, List<String> keys);
    }

    public void readComment(String postID, final ICommentRead helper)
    {
        DatabaseReference filePath = reference.child(postID);
        Query query = filePath.orderByChild("timeStamp");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> keys = new ArrayList<>();
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    String key = data.getKey();
                    Comment comment = data.getValue(Comment.class);
                    keys.add(key);
                    comments.add(comment);
                }

                helper.onRead(comments, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readCommentWithUserID(String postID, String userID, final ICommentRead helper)
    {
        Query query = reference.child(postID).orderByChild("id").equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> keys = new ArrayList<>();
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    String key = data.getKey();
                    Comment comment = data.getValue(Comment.class);
                    keys.add(key);
                    comments.add(comment);
                }
                helper.onRead(comments, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void updateComment(String postID, String commentKey, Comment comment, final ICommentUpdate helper)
    {
        DatabaseReference filePath = reference.child(postID).child(commentKey);
        filePath.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                helper.onUpdate();
            }
        });
    }

    public void updateComments(String postID, final List<String> keys, List<Comment> comments, final ICommentUpdate helper)
    {
        DatabaseReference filePath = reference.child(postID);
        for (int i = 0; i < keys.size(); i++)
        {
            final String key = keys.get(i);
            Comment comment = comments.get(i);
            final int index = i;
            updateComment(postID, key, comment, new ICommentUpdate() {
                @Override
                public void onUpdate() {
                    if (index == keys.size() - 1)
                    {
                        helper.onUpdate();
                    }
                }
            });
        }
    }

}
