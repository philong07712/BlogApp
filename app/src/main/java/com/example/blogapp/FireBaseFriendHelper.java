package com.example.blogapp;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FireBaseFriendHelper {
    FirebaseDatabase database;
    DatabaseReference reference;

    public interface IFireBaseFriendHelper
    {
        void onAdd();
        void onRead(List<String> keys);
        void onUpdate();
        void onDelete();
    }


    public FireBaseFriendHelper() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Friends");
    }

    public void addFriend(String userID, String friendID, final IFireBaseFriendHelper helper)
    {
        DatabaseReference friendPath = reference.child(userID).child(friendID);
        friendPath.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                helper.onAdd();
            }
        });
    }

    public void readFriend(String userID, final IFireBaseFriendHelper helper)
    {
        DatabaseReference friendPath = reference.child(userID);
        friendPath.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> keys = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    String key = data.getKey();
                    keys.add(key);
                }
                helper.onRead(keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
