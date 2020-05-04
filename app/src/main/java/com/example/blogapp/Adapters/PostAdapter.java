package com.example.blogapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogapp.Models.Post;
import com.example.blogapp.Activities.PostDetailActivity;
import com.example.blogapp.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    Context mContext;
    List<Post> posts;

    public PostAdapter(Context mContext, List<Post> posts) {
        this.mContext = mContext;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(posts.get(position).getTitle());
        Glide.with(mContext).load(posts.get(position).getPicture()).into(holder.postImg);
        Glide.with(mContext).load(posts.get(position).getUserPhoto()).into(holder.profileImg);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvTitle;
        ImageView profileImg;
        ImageView postImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.row_post_title);
            profileImg = itemView.findViewById(R.id.row_post_profile_img);
            postImg = itemView.findViewById(R.id.row_post_img);
            // setup post detail transaction
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostDetailActivity.class);
                    // send data to the detail activity
                    int postion = getAdapterPosition();

                    intent.putExtra("Title", posts.get(postion).getTitle());
                    intent.putExtra("Description", posts.get(postion).getDescription());
                    intent.putExtra("postImage", posts.get(postion).getPicture());
                    intent.putExtra("posterImage", posts.get(postion).getUserPhoto());
                    intent.putExtra("postKey", posts.get(postion).getKey());
                    intent.putExtra("posterName", posts.get(postion).getName());
                    long timeStamp = (long) posts.get(postion).getTimeStamp();
                    Log.d("MyHome", Long.toString(timeStamp));
                    intent.putExtra("postDate", timeStamp);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
