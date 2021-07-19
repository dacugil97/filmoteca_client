package com.example.filmoteca.webservice;

import com.example.filmoteca.model.MovieData;
import com.example.filmoteca.model.SubscriptionData;
import com.example.filmoteca.model.UserData;
import com.example.filmoteca.model.WLContentData;
import com.example.filmoteca.model.WatchListData;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerClient {

    @POST("api/v1/registration")
    Call<ResponseBody> register(
            @Query("user[email]") String email,
            @Query("user[nickname]") String nick,
            @Query("user[password]") String password,
            @Query("user[password_confirmation]") String rePassword
    );

    @POST("api/v1/session")
    Call<ResponseBody> login(
            @Query("user[email]") String email,
            @Query("user[password]") String password
    );

    @GET("api/v1/getUserData")
    Call<UserData> getUserData(
            @Query("email") String email
    );

    @GET("api/v1/watchlist/getWatchlists")
    Call<WatchListData> getUserWatchlists(
            @Query("id_user") int idUser
    );

    @POST("api/v1/movies")
    Call<ResponseBody> addMovie(
            @Query("movie[id]") int id,
            @Query("movie[title]") String title,
            @Query("movie[year]") String year,
            @Query("movie[overview]") String overview,
            @Query("movie[director]") String director,
            @Query("movie[casting]") String casting,
            @Query("movie[poster]") String poster,
            @Query("movie[backdrop]") String backdrop,
            @Query("movie[runtime]") String runtime,
            @Query("movie[rating]") String rating,
            @Query("movie[omdb]") boolean omdb
    );

    @POST("api/v1/watchlists")
    Call<ResponseBody> addWatchList(
            @Query("watchlist[user_id]") int idUser,
            @Query("watchlist[id_wl]") int idWl,
            @Query("watchlist[name]") String name,
            @Query("watchlist[autor]") String autor,
            @Query("watchlist[public]") boolean pub
    );

    @POST("api/v1/watchlist/addMovie")
    Call<ResponseBody> addMovieToWl(
            @Query("watchlistcontent[id_user]") int idUser,
            @Query("watchlistcontent[id_wl]") int idWl,
            @Query("watchlistcontent[id_mv]") int idMv
    );

    @GET("api/v1/watchlist/getUserMovies")
    Call<MovieData> getUserMovies(
            @Query("id_user") int idUser
    );

    @GET("api/v1/watchlist/getWatchlistcontents")
    Call<WLContentData> getUserWLContents(
            @Query("id_user") int idUser
    );

    @DELETE("api/v1/watchlist/deleteMovie")
    Call<ResponseBody> deleteMovie(
            @Query("id_user") int idUser,
            @Query("id_wl") int idWl,
            @Query("id_mv") int idMv
    );

    @DELETE("api/v1/watchlist/deleteWatchlist")
    Call<ResponseBody> deleteWatchList(
            @Query("id_user") int idUser,
            @Query("id_wl") int idWl
    );

    @GET("api/v1/watchlist/public")
    Call<WatchListData> getPublicWatchLists(
            @Query("input") String input
    );

    @GET("api/v1/watchlist/getPublicMovies")
    Call<MovieData> getPublicMovies(
            @Query("id_creator") int idCreator,
            @Query("id_wl") int idWl
    );

    @POST("api/v1/watchlist/subscribe")
    Call<ResponseBody> subscribeToWl(
            @Query("subscription[id_user]") int idUser,
            @Query("subscription[id_creator]") int idWl,
            @Query("subscription[id_wl]") int idMv
    );

    @GET("api/v1/watchlist/getSubscriptions")
    Call<SubscriptionData> getSubscriptions(
            @Query("id_user") int idUser
    );

    @GET("api/v1/watchlist/getSubscriptionWls")
    Call<WatchListData> getSubscriptionWls(
            @Query("id_user") int idUser
    );

    @DELETE("api/v1/watchlist/unsubscribe")
    Call<ResponseBody> unsubscribe(
            @Query("id_user") int idUser,
            @Query("id_creator") int idCreator,
            @Query("id_wl") int idWl
    );

}