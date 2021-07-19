package com.example.filmoteca.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.filmoteca.model.TmdbMovie;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MovieContract {
    public static final String DB_NAME = "filmoteca.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "movie";
    public static final String DEFAULT_SORT = MovieContract.Column.ID + " ASC";
    public static final String CREATE_TABLE = "create table %s (%s integer primary key, %s text, %s text, %s text, %s text, %s text, %s text, %s text, %s text, %s float, %s bit, %s date)";

    public class Column {
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String YEAR = "year";
        public static final String OVERVIEW = "overview";
        public static final String DIRECTOR = "director";
        public static final String CASTING = "casting";
        public static final String POSTER = "poster";
        public static final String BACKDROP = "backdrop";
        public static final String RUNTIME = "runtime";
        public static final String RATING = "rating";
        public static final String OMDB = "omdb";
        public static final String INSERTED = "inserted";
    }

    public static void addMovieDb(TmdbMovie movie, SQLiteDatabase db){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        ContentValues values = new ContentValues();
        values.clear();
        values.put(MovieContract.Column.ID, movie.getId());
        values.put(MovieContract.Column.TITLE, movie.getTitle());
        values.put(MovieContract.Column.YEAR, movie.getReleaseDate());
        values.put(Column.OVERVIEW, movie.getOverview());
        if(movie.getOmdbMovie()!=null){
            values.put(MovieContract.Column.DIRECTOR, movie.getOmdbMovie().getDirector());
            values.put(MovieContract.Column.CASTING, movie.getOmdbMovie().getActor());
            values.put(MovieContract.Column.RUNTIME, movie.getOmdbMovie().getRuntime());
            values.put(MovieContract.Column.RATING, movie.getOmdbMovie().getImdbRating());
            if("N/A".equals(movie.getOmdbMovie().getPoster()) || movie.getOmdbMovie().getPoster().isEmpty()){
                values.put(MovieContract.Column.POSTER, movie.getPosterPath());
            }else{
                values.put(MovieContract.Column.POSTER, movie.getOmdbMovie().getPoster());
            }
            values.put(MovieContract.Column.OMDB, "1");
        }else{
            values.put(MovieContract.Column.POSTER, movie.getPosterPath());
            values.put(MovieContract.Column.OMDB, "0");
            values.put(MovieContract.Column.RATING, movie.getVoteAverage());
        }
        values.put(MovieContract.Column.BACKDROP, movie.getBackdropPath());
        values.put(Column.INSERTED, dateFormat.format(now));

        db.insertWithOnConflict(MovieContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static List<TmdbMovie> getMovies(SQLiteDatabase db) {
        List<TmdbMovie> movies = new ArrayList<>();
        int count = 10;

        String sqlmv = "select * from movie";
        Cursor c = db.rawQuery(sqlmv, null);
        if (c.moveToFirst()) {
            do {
                count--;
                TmdbMovie movie = TmdbMovie.movieQueryConverter(c);
                movies.add(movie);

            } while (c.moveToNext() && count!=0);
        }
        c.close();
        return movies;
    }
}
