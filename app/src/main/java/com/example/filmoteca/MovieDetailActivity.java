package com.example.filmoteca;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filmoteca.model.TmdbMovie;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String IMG_URL = "https://image.tmdb.org/t/p/original";

    private TmdbMovie movie;

    private ImageView imgBackdrop;
    private TextView tvTitle;
    private TextView tvYear;
    private TextView tvRating;
    private ImageView imgRating;
    private TextView tvOverview;
    private FloatingActionButton fabMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        setUpView();
    }

    private void setUpView() {
        movie = (TmdbMovie) getIntent().getSerializableExtra("movie");
        fabMovie = findViewById(R.id.fabMovie);

        imgBackdrop = findViewById(R.id.imgBackdrop);
        imgBackdrop.getLayoutParams().height = 610;
        tvTitle = findViewById(R.id.tvTitle);
        tvYear = findViewById(R.id.tvYear);
        imgRating = findViewById(R.id.imgRating);
        tvRating = findViewById(R.id.tvRating);
        tvOverview = findViewById(R.id.tvOverview);

        // Obtenemos la imagen de fondo de la película
        Picasso.get().load(IMG_URL + movie.getBackdropPath()).into(imgBackdrop);

        tvTitle.setText(movie.getTitle());
        tvYear.setText(movie.getReleaseDate());
        if (movie.getOmdbMovie() == null) {
            setUpTmdb();
        }else{
            setupOmdb();
        }
        tvOverview.setText(movie.getOverview());

        boolean fromWl = getIntent().getBooleanExtra("WatchList", false);
        if(!fromWl) {
            fabMovie.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddMovieActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            });
        }else{
            fabMovie.setVisibility(View.GONE);
        }

    }

    private void setUpTmdb(){
        TextView tvOverviewTitle = findViewById(R.id.tvOverviewTitle);
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.tvYear);
        tvOverviewTitle.setLayoutParams(params);
        imgRating.setImageResource(R.drawable.ic_tmdb_logo_large);
        tvRating.setText(movie.getVoteAverage());
    }

    private void setupOmdb(){
        TextView tvDurationTitle = findViewById(R.id.tvDurationTitle);
        TextView tvDirectorTitle = findViewById(R.id.tvDirectorTitle);
        TextView tvCastingTitle = findViewById(R.id.tvCastingTitle);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvDirector = findViewById(R.id.tvDirector);
        TextView tvCasting = findViewById(R.id.tvCasting);

        imgRating.setImageResource(R.drawable.ic_imdb_logo_large);
        tvRating.setText(movie.getOmdbMovie().getImdbRating());

        tvDurationTitle.setText("Duración: ");
        tvDirectorTitle.setText("Director: ");
        tvCastingTitle.setText("Casting: ");

        tvDuration.setText(movie.getOmdbMovie().getRuntime());
        tvDirector.setText(movie.getOmdbMovie().getDirector());
        tvCasting.setText(movie.getOmdbMovie().getActor());
    }

}