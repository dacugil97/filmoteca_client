package com.example.filmoteca.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WatchListSub {

    @SerializedName("id_user")
    @Expose
    private int idUser;

    @SerializedName("id_creator")
    @Expose
    private int idCreator;

    @SerializedName("id_wl")
    @Expose
    private int idWl;

    public WatchListSub(){}

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdCreator() {
        return idCreator;
    }

    public void setIdCreator(int idCreator) {
        this.idCreator = idCreator;
    }

    public int getIdWl() {
        return idWl;
    }

    public void setIdWl(int idWl) {
        this.idWl = idWl;
    }
}
