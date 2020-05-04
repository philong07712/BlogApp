package com.example.blogapp.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.R;

import java.util.Calendar;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    Context mContext;
    List<Comment> comments;

    public CommentAdapter(Context mContext, List<Comment> comments) {
        this.mContext = mContext;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        if (comments.get(position).getImg() != null)
        {
            Glide.with(mContext).load(comments.get(position).getImg()).into(holder.image);
        }
        else
        {
            holder.image.setImageResource(R.drawable.userphoto);
        }
        holder.name.setText(comments.get(position).getName());
        holder.content.setText(comments.get(position).getContent());
        String date = convertTimeStamp((long) comments.get(position).getTimeStamp());
        holder.date.setText(date);
    }

    private String convertTimeStamp(long time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm", calendar).toString();
        return date;
    }
    @Override
    public int getItemCount() {
        return comments.size();
    }


    class CommentViewHolder extends RecyclerView.ViewHolder
    {
        ImageView image;
        TextView name, content, date;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.comment_item_image);
            name = itemView.findViewById(R.id.comment_item_name);
            content = itemView.findViewById(R.id.comment_item_content);
            date = itemView.findViewById(R.id.comment_item_date);
        }
    }
}
