package com.example.filmoteca.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.model.WatchList;

import java.util.ArrayList;
import java.util.List;

public class WatchListContract {
    public static final String DB_NAME = "filmoteca.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "watchlist";
    public static final String DEFAULT_SORT = Column.ID + " ASC";
    public static final String CREATE_TABLE = "create table %s (%s integer primary key autoincrement, %s text not null, %s text not null, %s bit not null)";

    public WatchListContract() {
    }

    public class Column {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String AUTOR = "autor";
        public static final String PUBLIC = "public";
    }

    public static void insertWatchLists(List<WatchList> wls, SQLiteDatabase db){
        for(WatchList wl : wls){
            ContentValues values = new ContentValues();
            values.clear();
            values.put(Column.ID, wl.getIdWl());
            values.put(Column.NAME, wl.getName());
            values.put(Column.AUTOR, wl.getAutor());
            values.put(Column.PUBLIC, wl.isPub());

            long id = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            Log.d(String.valueOf(id), wl.getName());
        }

    }

    public static List<WatchList> getWatchLists(SQLiteDatabase db) {
        List<WatchList> wlists = new ArrayList<>();

        String sqlwl = "select * from watchlist";
        Cursor c = db.rawQuery(sqlwl, null);
        if (c.moveToFirst()) {
            do {
                // Passing values
                String idwl = c.getString(0);
                String name = c.getString(1);
                String autor = c.getString(2);
                String pubString = c.getString(3);
                boolean pub = false;
                if ("1".equals(pubString)) {
                    pub = true;
                }
                    Log.d("ID", idwl);
                    Log.d("NAME", name);
                    Log.d("autor", autor);
                    WatchList wl = new WatchList(Integer.parseInt(c.getString(0)), c.getString(1), autor, pub, new ArrayList<TmdbMovie>());
                    wlists.add(wl);

                    String sqlin = String.format("select id_mv from wlcontent where id_wl = %s", idwl);
                    Cursor k = db.rawQuery(sqlin, null);
                    if (k.moveToFirst()) {
                        do {
                            String idmv = k.getString(0);
                            String sqlmv = String.format("select * from movie where id = %s order by inserted asc", idmv);
                            Cursor j = db.rawQuery(sqlmv, null);
                            j.moveToFirst();
                            TmdbMovie movie = TmdbMovie.movieQueryConverter(j);
                            wl.addMovie(movie);
                        } while (k.moveToNext());
                    }
            } while (c.moveToNext());
        }
        c.close();
        return wlists;
    }
}
