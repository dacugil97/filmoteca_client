package com.example.filmoteca;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.adapter.MovieAdapter;
import com.example.filmoteca.adapter.RecyclerViewClickListener;
import com.example.filmoteca.model.OmdbMovie;
import com.example.filmoteca.model.TmdbData;
import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.persistence.DbHelper;
import com.example.filmoteca.persistence.MovieContract;
import com.example.filmoteca.webservice.APIClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener {

    private final static String TMDB_API_KEY = "7466e42e4627912cf940011a6b3d2991";
    private final static String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    private final static String OMDB_API_KEY = "5fe45b6d";
    private final static String OMDB_BASE_URL = "https://www.omdbapi.com/";

    private RelativeLayout errorLayout;
    private SearchView svSearch;
    private TextView recentTv;
    private MovieAdapter adapter;
    private List<TmdbMovie> tmdbMovies;

    private APIClient client; 

    private final String language = "es-ES";
    private final boolean adult = false;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        setUpView(view);
        return view;
    }

    private void setUpView(View view){
        // Nos aseguramos que el botón de la NavigationBar está marcado correctamente.
        BottomNavigationView nav = getActivity().findViewById(R.id.navigationView);
        if(nav.getSelectedItemId() != R.id.search_nav){
            nav.setSelectedItemId(R.id.search_nav);
            getActivity().getSupportFragmentManager().popBackStack();
        }
        // Iniciamos SQLite
        DbHelper dbHelper = new DbHelper(getContext(), MovieContract.DB_NAME, null, MovieContract.DB_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Iniciamos vista y adaptador
        tmdbMovies = MovieContract.getMovies(db);
        adapter = new MovieAdapter(tmdbMovies);
        RecyclerView recyclerView = view.findViewById(R.id.searchView);
        errorLayout = view.findViewById(R.id.errorLayout);
        recentTv = view.findViewById(R.id.recentTv);
        recentTv.setVisibility(View.VISIBLE);
        if(adapter.getItemCount()==0){
            errorLayout.setVisibility(View.VISIBLE);
        }else{
            errorLayout.setVisibility(View.GONE);
        }

        // Barra de búsqueda y su listener
        svSearch = view.findViewById(R.id.svSearch);
        svSearch.setOnQueryTextListener(this);

        LinearLayoutManager lim = new LinearLayoutManager(getActivity());
        lim.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(lim);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerViewClickListener(getActivity(), recyclerView,new RecyclerViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Intent intent = new Intent (getActivity(), MovieDetailActivity.class);
                        intent.putExtra("movie", adapter.getMovie(position));
                        startActivity(intent);
                    }

                    @Override public void onLongItemClick(View view, int position) { }
                })
        );
    }

    private void tmdbPetition(String input){
        int page = 1;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        client = retrofit.create(APIClient.class);
        Call<TmdbData> call = client.getTmdbMovie(TMDB_API_KEY, language, input, page, adult);
        call.enqueue(new Callback<TmdbData>() {
            @Override
            public void onResponse(Call<TmdbData> call, Response<TmdbData> response) {
                tmdbMovies = response.body().getResults();
                int totalPages = response.body().getTotalPages();
                if(tmdbMovies.size()!=0){
                    for(int page = 2; page <= totalPages && page <= 3; page++){
                        newTmdbPetition(page, input);
                    }
                    omdbPetition(movieFilter(tmdbMovies, input));
                }else{
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<TmdbData> call, Throwable t) {
                Log.d("TAG1", "Error: " + t.getMessage());
            }
        });
    }

    private void newTmdbPetition(int page, String input){
        Call<TmdbData> call = client.getTmdbMovie(TMDB_API_KEY, language, input, page, adult);
        call.enqueue(new Callback<TmdbData>() {
            @Override
            public void onResponse(Call<TmdbData> call, Response<TmdbData> response) {
                List<TmdbMovie> moviesNext = response.body().getResults();
                omdbPetition(movieFilter(moviesNext, input));
            }

            @Override
            public void onFailure(Call<TmdbData> call, Throwable t) {
                Log.d("TAG1", "Error: " + t.getMessage());
            }
        });
    }

    private void omdbPetition(@NotNull List<TmdbMovie> movies1){
        Retrofit retrofit;
        HttpLoggingInterceptor loggingInterceptor;
        OkHttpClient.Builder httpClientBuilder;
        APIClient client;

        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        retrofit = new Retrofit.Builder()
                .baseUrl(OMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        client = retrofit.create(APIClient.class);
        movies1.forEach((TmdbMovie tmdbMovie) -> {
            tmdbMovie.setReleaseDate(tmdbMovie.getReleaseDate().split("-")[0]);
            String year = tmdbMovie.getReleaseDate();
            Call<OmdbMovie> call = client.getOmdbMovie(tmdbMovie.getOriginalTitle(), year, OMDB_API_KEY, "movie");

            call.enqueue(new Callback<OmdbMovie>() {
                @Override
                public void onResponse(Call<OmdbMovie> call, Response<OmdbMovie> response) {
                    if(response.body().getPoster() != null && response.body().getTitle().equals(tmdbMovie.getOriginalTitle())){
                        tmdbMovie.setOmdbMovie(response.body());
                    }
                    adapter.addData(tmdbMovie);
                }

                @Override
                public void onFailure(@NotNull Call<OmdbMovie> call, Throwable t) {
                    Log.d("TAG1", "Error: " + t.getMessage());
                    adapter.addData(tmdbMovie);
                }
            });
        });
    }

    private List<TmdbMovie> movieFilter(List<TmdbMovie> tmdbMovies, String input){

        List<TmdbMovie> filteredMovies = new ArrayList<>();

        tmdbMovies.forEach((TmdbMovie movie) -> {
            String poster = movie.getPosterPath();
            String backdrop = movie.getBackdropPath();
            String overview = movie.getOverview();
            Double popularity = movie.getPopularity();
            Boolean contained = movie.getTitle().toLowerCase().contains(input.toLowerCase()) || movie.getOriginalTitle().toLowerCase().contains(input.toLowerCase());
            if(poster != null && backdrop != null && overview != null && popularity>10 && contained) {
                filteredMovies.add(movie);
            }
        });
        return filteredMovies;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(query.length()>=2){
            errorLayout.setVisibility(View.GONE);
            adapter.clearData();
            svSearch.clearFocus();
            recentTv.setVisibility(View.GONE);
            tmdbPetition(query);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (adapter.getItemCount() == 0){
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }, 1500);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}