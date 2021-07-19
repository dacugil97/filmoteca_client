package com.example.filmoteca.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = DbHelper.class.getSimpleName();

    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Borramos la vieja base de datos
        db.execSQL("drop table if exists " + MovieContract.TABLE);
        db.execSQL("drop table if exists " + WatchListContract.TABLE);
        db.execSQL("drop table if exists " + WLContentContract.TABLE);
        db.execSQL("drop table if exists " + SubscriptionContract.TABLE);
        // Creamos una base de datos nueva
        onCreate(db);
        Log.d(TAG, "onUpgrade");
    }

    private void createTables(SQLiteDatabase db){
        String createMovie = String.format(MovieContract.CREATE_TABLE,
                MovieContract.TABLE,
                MovieContract.Column.ID,
                MovieContract.Column.TITLE,
                MovieContract.Column.YEAR,
                MovieContract.Column.OVERVIEW,
                MovieContract.Column.DIRECTOR,
                MovieContract.Column.CASTING,
                MovieContract.Column.POSTER,
                MovieContract.Column.BACKDROP,
                MovieContract.Column.RUNTIME,
                MovieContract.Column.RATING,
                MovieContract.Column.OMDB,
                MovieContract.Column.INSERTED);
        String createWatchList = String.format(WatchListContract.CREATE_TABLE,
                WatchListContract.TABLE,
                WatchListContract.Column.ID,
                WatchListContract.Column.NAME,
                WatchListContract.Column.AUTOR,
                WatchListContract.Column.PUBLIC);
        String createWlContent = String.format(WLContentContract.CREATE_TABLE,
                WLContentContract.TABLE,
                WLContentContract.Column.IDWL,
                WLContentContract.Column.IDMV,
                WLContentContract.Column.IDWL, WLContentContract.Column.IDMV,
                WLContentContract.Column.IDWL, WatchListContract.TABLE, WatchListContract.Column.ID,
                WLContentContract.Column.IDMV, MovieContract.TABLE, MovieContract.Column.ID);
        String createSubscription = String.format(SubscriptionContract.CREATE_TABLE,
                SubscriptionContract.TABLE,
                SubscriptionContract.Column.IDUSER,
                SubscriptionContract.Column.IDCREATOR,
                SubscriptionContract.Column.IDWL,
                SubscriptionContract.Column.IDUSER, SubscriptionContract.Column.IDCREATOR, SubscriptionContract.Column.IDWL);
        db.execSQL(createMovie);
        db.execSQL(createWatchList);
        db.execSQL(createWlContent);
        db.execSQL(createSubscription);
    }
}