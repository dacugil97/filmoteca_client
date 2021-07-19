package com.example.filmoteca.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WatchList implements Serializable {

    @SerializedName("user_id")
    @Expose
    private int idUser;

    @SerializedName("id_wl")
    @Expose
    private int idWl;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("autor")
    @Expose
    private String autor;

    @SerializedName("public")
    @Expose
    private boolean pub;

    private List<TmdbMovie> movies;

    public WatchList(int idWl, String name, String autor, boolean pub, List<TmdbMovie> movies){
        this.idWl = idWl;
        this.name = name;
        this.autor = autor;
        this.pub = pub;
        this.movies = movies;
    }

    public WatchList(int idUser, int idWl, String name, String autor, boolean pub, List<TmdbMovie> movies){
        this.idUser = idUser;
        this.idWl = idWl;
        this.name = name;
        this.autor = autor;
        this.pub = pub;
        this.movies = movies;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdWl() {
        return idWl;
    }

    public void setIdWl(int idWl) {
        this.idWl = idWl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TmdbMovie> getMovies() {
        return movies;
    }

    public void setMovies(List<TmdbMovie> movies) {
        this.movies = movies;
    }

    public void addMovie(TmdbMovie movie) {
        this.movies.add(movie);
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public boolean isPub() {
        return pub;
    }

    public void setPub(boolean pub) {
        this.pub = pub;
    }
}
