package com.example.filmoteca.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class TmdbData implements Serializable {


    @SerializedName("results")
    private List<TmdbMovie> results;

    @SerializedName("total_pages")
    private int totalPages;

    public List<TmdbMovie> getResults() {
        return results;
    }

    public void setResults(List<TmdbMovie> results) {
        this.results = results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
