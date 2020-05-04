package com.example.blogapp.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blogapp.Adapters.PostAdapter;
import com.example.blogapp.FireBaseFriendHelper;
import com.example.blogapp.Models.Post;
import com.example.blogapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    // init custom variable
    RecyclerView postRecyclerView;
    List<Post> posts = new ArrayList<>();
    PostAdapter adapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    LinearLayoutManager layoutPost;
    SwipeRefreshLayout swipeRefreshLayout;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewFragment = inflater.inflate(R.layout.fragment_home, container, false);
        Context context = getActivity();
        // init view
        layoutPost = new LinearLayoutManager(getActivity());
        postRecyclerView = viewFragment.findViewById(R.id.post_rv);
        postRecyclerView.setLayoutManager(layoutPost);
        // setup database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Posts").child(currentUser.getUid());
        swipeRefreshLayout = viewFragment.findViewById(R.id.post_swipe_refresh);
        // TODO: this test the friend add
        getFriendUID(currentUser.getUid());
        addRefreshListener();

        return viewFragment;
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    private void showMessage(String message)
    {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void getFriendUID(final String uid)
    {
        new FireBaseFriendHelper().readFriend(uid, new FireBaseFriendHelper.IFireBaseFriendHelper() {
            @Override
            public void onAdd() {

            }

            @Override
            public void onRead(List<String> keys) {
                // we will add current user id also to friends list to show
                // current user posts
                keys.add(currentUser.getUid());
                initFriendPost(keys);
            }

            @Override
            public void onUpdate() {

            }

            @Override
            public void onDelete() {

            }
        });
    }

    private void initFriendPost(List<String> friendsUID)
    {
        posts.clear();
        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
        for (String uid : friendsUID)
        {
            convertIDtoPost(uid);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void convertIDtoPost(String uid)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(uid);
        // we will sort the post from old to new
        Query query = reference.orderByChild("timeStamp");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotChildren : dataSnapshot.getChildren())
                {
                    Post newPost = dataSnapshotChildren.getValue(Post.class);
                    posts.add(newPost);
                }
                // we will sort the posts list to the order new to old
                Collections.sort(posts, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o2.getTimeStamp().toString().compareTo(o1.getTimeStamp().toString());
                    }
                });

                adapter = new PostAdapter(getActivity(), posts);
                postRecyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void refresh()
    {
        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
        getFriendUID(currentUser.getUid());
    }
    private void addRefreshListener()
    {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(true);

                // Refreshing the posts
            }
        });
    }

}
