package com.example.filmoteca.persistence;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.model.WLContentData;
import com.example.filmoteca.model.WatchList;

import java.util.List;

public class WLContentContract {
    public static final String DB_NAME = "filmoteca.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "wlcontent";
    public static final String DEFAULT_SORT = WatchListContract.Column.ID + " ASC";
    public static final String CREATE_TABLE = "create table %s (" +
            "%s integer not null, " +
            "%s integer not null, " +
            "primary key(%s, %s), " +
            "foreign key(%s) references %s(%s), " +
            "foreign key(%s) references %s(%s))";

    public class Column {
        public static final String IDWL = "id_wl";
        public static final String IDMV = "id_mv";
    }

    public static void addMovieToWl(WatchList wl, TmdbMovie mv, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.clear();
        values.put(WLContentContract.Column.IDWL, wl.getIdWl());
        values.put(WLContentContract.Column.IDMV, mv.getId());

        db.insertWithOnConflict(WLContentContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void insertData(List<WLContentData.WlContentDataDetail> data, SQLiteDatabase db){
        for(WLContentData.WlContentDataDetail dt : data){
            ContentValues values = new ContentValues();
            values.clear();
            values.put(WLContentContract.Column.IDWL, dt.getIdWl());
            values.put(WLContentContract.Column.IDMV, dt.getIdMv());

            db.insertWithOnConflict(WLContentContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }
/*
    public static void deleteContent(int idWl, int idMv, SQLiteDatabase db){
        String format = "delete from %s where %s = %s and %s = %s";

        String query = String.format(format,
                TABLE,
                Column.ID_WL, idWl,
                );
        db.
    }

 */
}
