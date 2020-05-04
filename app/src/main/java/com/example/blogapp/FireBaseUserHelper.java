package com.example.blogapp;

import android.widget.Toast;

import com.example.blogapp.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FireBaseUserHelper {

    FirebaseDatabase database;
    DatabaseReference reference;

    public interface IFireBaseHelper {
        void onAdd();
        void onRead(List<User> users, List<String> keys);
        void onUpdate();
        void onDelete();
    }


    public FireBaseUserHelper() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");
    }

    public void addUser(User user, final IFireBaseHelper helper)
    {
        DatabaseReference userPath = reference.child(user.getUserId());
        userPath.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                helper.onAdd();
            }
        });
    }

    public void updateUser(String key, User user, final IFireBaseHelper helper)
    {
        DatabaseReference userPath = reference.child(key);
        userPath.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                helper.onUpdate();
            }
        });
    }

}
