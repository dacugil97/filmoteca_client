package com.example.filmoteca.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.filmoteca.LoginActivity;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences pref;

    SharedPreferences.Editor editor;

    Context _context;

    int Private_mode = 0;

    private static final String PREF_NAME = "FilmotecaPref";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_EMAIL = "email";

    public static final String KEY_ID = "id";

    public static final String KEY_NICKNAME = "nickname";

    public SessionManager (Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Private_mode);
        editor = pref.edit();
    }

    public void createLoginSession(String email, int id, String nick){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ID, String.valueOf(id));
        editor.putString(KEY_NICKNAME, nick);

        editor.apply();
        editor.commit();
    }

    public boolean checkLogin(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL,null));
        user.put(KEY_ID, pref.getString(KEY_ID,null));
        user.put(KEY_NICKNAME, pref.getString(KEY_NICKNAME,null));
        return user;
    }

    public void logoutUser(){
        editor.clear();
        editor.apply();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);
        _context.startActivity(i);
    }
}
