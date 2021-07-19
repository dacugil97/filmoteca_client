package com.example.filmoteca.webservice;

import com.example.filmoteca.model.OmdbMovie;
import com.example.filmoteca.model.TmdbData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIClient {

    @GET("search/movie")
    Call<TmdbData> getTmdbMovie(
            @Query("api_key") String api,
            @Query("language") String lang,
            @Query("query") String query,
            @Query("page") int page,
            @Query("include_adult") boolean adult
    );

    @GET(".")
    Call<OmdbMovie> getOmdbMovie(
            @Query("t") String title,
            @Query("y") String year,
            @Query("apikey") String api,
            @Query("type") String type
    );
}
