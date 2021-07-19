package com.example.filmoteca.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.R;
import com.example.filmoteca.model.TmdbMovie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterHolder> {

    private List<TmdbMovie> tmdbMovies;

    private static final String IMG_URL = "https://image.tmdb.org/t/p/original";

    public MovieAdapter(List<TmdbMovie> tmdbMovies){
        this.tmdbMovies = tmdbMovies;
    }

    @NonNull
    @Override
    public MovieAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_movie, parent, false);

        return new MovieAdapterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterHolder holder, int position) {

        TmdbMovie tmdbMovie = tmdbMovies.get(position);
        holder.tvTitle.setText(tmdbMovie.getTitle());
        holder.tvYear.setText(tmdbMovie.getReleaseDate());
        if(tmdbMovie.getOmdbMovie() != null){
            setUpOmdb(holder, tmdbMovie);
        }else{
            setUpTmdb(holder, tmdbMovie);
        }
    }

    private void setUpOmdb(@NonNull MovieAdapterHolder holder, TmdbMovie movie) {

        if("N/A".equals(movie.getOmdbMovie().getPoster()) || movie.getOmdbMovie().getPoster().isEmpty()){
            Picasso.get().load(IMG_URL + movie.getPosterPath()).into(holder.imgPoster);
        }else{
            Picasso.get().load(movie.getOmdbMovie().getPoster()).into(holder.imgPoster);
        }
        holder.imgRating.setImageResource(R.drawable.ic_imdb_logo_short);
        holder.tvRating.setText(movie.getOmdbMovie().getImdbRating());
    }

    private void setUpTmdb(@NonNull MovieAdapterHolder holder, TmdbMovie movie) {
        Picasso.get().load(IMG_URL + movie.getPosterPath()).into(holder.imgPoster);
        holder.imgRating.setImageResource(R.drawable.ic_tmdb_logo_short);
        holder.tvRating.setText(movie.getVoteAverage());
    }

    @Override
    public int getItemCount() {
        return tmdbMovies.size();
    }

    public void addData(TmdbMovie tmdbMovie){
        this.tmdbMovies.add(tmdbMovie);
        Collections.sort(this.tmdbMovies, (m1, m2) -> Double.compare(m2.getPopularity(), m1.getPopularity()));
        notifyDataSetChanged();
    }

    public void clearData(){
        this.tmdbMovies = new ArrayList<>();
        notifyDataSetChanged();
    }

    public TmdbMovie getMovie(int position){
        return tmdbMovies.get(position);
    }

    public void deleteMovie(int position){
        this.tmdbMovies.remove(position);
        notifyDataSetChanged();
    }

    public class MovieAdapterHolder extends RecyclerView.ViewHolder{
        private TextView tvTitle;
        private TextView tvYear;
        private ImageView imgPoster;
        private ImageView imgRating;
        private TextView tvRating;

        public MovieAdapterHolder (@NonNull View itemView){
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvYear = itemView.findViewById(R.id.tvYear);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            imgRating = itemView.findViewById(R.id.imgRating);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
