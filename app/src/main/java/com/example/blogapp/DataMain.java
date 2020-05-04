package com.example.blogapp;

import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;

import com.example.blogapp.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DataMain {
    public static DataMain instance = new DataMain();

    private DataMain()
    {

    }

    public static DataMain getInstance()
    {
        return DataMain.instance;
    }

    public void setFriend(String uidCurrent, String uidFriend)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uidCurrent)
                .child("Friends").child(uidFriend);

        reference.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }
}
