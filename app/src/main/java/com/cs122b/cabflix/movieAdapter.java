package com.cs122b.cabflix;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class movieAdapter extends RecyclerView.Adapter<movieAdapter.ViewHolder>{
    private static final String TAG = "movieAdapter";
    private ArrayList<String> movieTitles = new ArrayList<>();
    private ArrayList<String> movieImages = new ArrayList<>();
    private ArrayList<String> movieIDs = new ArrayList<>();
    private Context context;

    public movieAdapter(ArrayList<String> movieImages, ArrayList<String> movieTitles, ArrayList<String> movieIDs, Context context) {
        this.movieImages = movieImages;
        this.movieTitles = movieTitles;
        this.movieIDs = movieIDs;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movies, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder is called");

        Glide.with(context).fromUri().asBitmap().load(Uri.parse(this.movieImages.get(position))).into(holder.movieImage);

        holder.title.setText(this.movieTitles.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick the view" + movieTitles.get(position));
                Intent intent = new Intent(holder.parentLayout.getContext(), movieDetail.class);
                intent.putExtra("movie_id", movieIDs.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.movieTitles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView movieImage;
        TextView title;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.movieImage = itemView.findViewById(R.id.theMovie);
            this.title = itemView.findViewById(R.id.movieTitle);
            this.parentLayout = itemView.findViewById(R.id.parent_layout);
        }

    }

}
