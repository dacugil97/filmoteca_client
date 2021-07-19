package com.example.filmoteca.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieData {

    @SerializedName("movies")
    @Expose
    private List<MovieDataDetail> movies;

    public class MovieDataDetail {

        @SerializedName("id")
        @Expose
        private int id;

        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("year")
        @Expose
        private String year;

        @SerializedName("overview")
        @Expose
        private String overview;

        @SerializedName("director")
        @Expose
        private String director;

        @SerializedName("casting")
        @Expose
        private String casting;

        @SerializedName("poster")
        @Expose
        private String poster;

        @SerializedName("backdrop")
        @Expose
        private String backdrop;

        @SerializedName("runtime")
        @Expose
        private String runtime;

        @SerializedName("rating")
        @Expose
        private String rating;

        @SerializedName("omdb")
        @Expose
        private boolean omdb;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getOverview() {
            return overview;
        }

        public void setOverview(String overview) {
            this.overview = overview;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public String getCasting() {
            return casting;
        }

        public void setCasting(String casting) {
            this.casting = casting;
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }

        public String getBackdrop() {
            return backdrop;
        }

        public void setBackdrop(String backdrop) {
            this.backdrop = backdrop;
        }

        public String getRuntime() {
            return runtime;
        }

        public void setRuntime(String runtime) {
            this.runtime = runtime;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public boolean isOmdb() {
            return omdb;
        }

        public void setOmdb(boolean omdb) {
            this.omdb = omdb;
        }
    }

    public List<MovieDataDetail> getMovies() {
        return movies;
    }

    public void setMovies(List<MovieDataDetail> movies) {
        this.movies = movies;
    }
}
