package com.example.filmoteca.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SubscriptionContract {
    public static final String DB_NAME = "filmoteca.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "subscription";
    public static final String CREATE_TABLE = "create table %s (" +
            "%s integer not null, " +
            "%s integer not null, " +
            "%s integer not null, " +
            "primary key(%s, %s, %s))";


    public class Column {
        public static final String IDUSER = "id_user";
        public static final String IDCREATOR = "id_creator";
        public static final String IDWL = "id_wl";
    }

    public static void addSubscription(int idUser, int idCreator, int idWl, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.clear();
        values.put(Column.IDUSER, idUser);
        values.put(Column.IDCREATOR, idCreator);
        values.put(Column.IDWL, idWl);

        db.insertWithOnConflict(SubscriptionContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static boolean isSubscribed(int idUser, int idCreator, int idWl, SQLiteDatabase db){
        boolean sub = false;
        String query = String.format("select * from subscription where id_user = %s and id_creator = %s and id_wl = %s", idUser, idCreator, idWl);
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            sub = true;
        }
        return sub;
    }
}
