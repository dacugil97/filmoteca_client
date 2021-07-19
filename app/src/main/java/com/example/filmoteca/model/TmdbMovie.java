package com.example.filmoteca.model;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TmdbMovie implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("original_title")
    private String originalTitle;

    @SerializedName("overview")
    private String overview;

    @SerializedName("popularity")
    private double popularity;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("title")
    private String title;

    @SerializedName("vote_average")
    private String voteAverage;

    private OmdbMovie omdbMovie;

    public TmdbMovie(){

    }

    public static TmdbMovie movieQueryConverter(Cursor c){
        TmdbMovie movie = new TmdbMovie();
        OmdbMovie extra = new OmdbMovie();
        Boolean omdb = true;
        if(c.getCount()!=0 && c.getColumnCount()>9){

            if("0".equals(c.getString(10))){
                omdb = false;
            }

            movie.setId(c.getInt(0));
            movie.setTitle(c.getString(1));
            movie.setReleaseDate(c.getString(2));
            movie.setOverview(c.getString(3));

            if(omdb){
                extra.setYear(c.getString(2));
                extra.setDirector(c.getString(4));
                extra.setActor(c.getString(5));
                extra.setPoster(c.getString(6));
                extra.setRuntime(c.getString(8));
                extra.setImdbRating(c.getString(9));
                movie.setOmdbMovie(extra);
            }
            movie.setPosterPath(c.getString(6));
            movie.setBackdropPath(c.getString(7));
            movie.setVoteAverage(c.getString(9));
        }

        /**
         * 0                MovieContract.Column.ID,
         *  1               MovieContract.Column.TITLE,
         *   2              MovieContract.Column.YEAR,
         *    3             MovieContract.Column.OVERVIEW,
         *     4            MovieContract.Column.DIRECTOR,
         *      5           MovieContract.Column.CASTING,
         *       6          MovieContract.Column.POSTER,
         *        7         MovieContract.Column.BACKDROP,
         *         8        MovieContract.Column.RUNTIME,
         *          9       MovieContract.Column.RATING,
         *           10     MovieContract.Column.OMDB);
         */


        return movie;
    }

    public static List<TmdbMovie> movieAdapter(List<MovieData.MovieDataDetail> rawMovies){
        List<TmdbMovie> movies = new ArrayList<>();

        for(MovieData.MovieDataDetail m : rawMovies){
            TmdbMovie movie = new TmdbMovie();
            OmdbMovie extra = new OmdbMovie();

            movie.setId(m.getId());
            movie.setTitle(m.getTitle());
            movie.setReleaseDate(m.getYear());
            movie.setOverview(m.getOverview());
            movie.setPosterPath(m.getPoster());
            movie.setBackdropPath(m.getBackdrop());
            movie.setVoteAverage(m.getRating());
            if(m.isOmdb()){
                extra.setPoster(m.getPoster());
                extra.setDirector(m.getDirector());
                extra.setActor(m.getCasting());
                extra.setRuntime(m.getRuntime());
                extra.setImdbRating(m.getRating());
                movie.setOmdbMovie(extra);
            }
            movies.add(movie);
        }

        return movies;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public OmdbMovie getOmdbMovie() {
        return omdbMovie;
    }

    public void setOmdbMovie(OmdbMovie omdbMovie) {
        this.omdbMovie = omdbMovie;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }
}
