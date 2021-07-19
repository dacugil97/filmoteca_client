package com.example.filmoteca;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.adapter.WatchListAdapter;
import com.example.filmoteca.adapter.RecyclerViewClickListener;
import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.model.WatchList;
import com.example.filmoteca.persistence.DbHelper;
import com.example.filmoteca.persistence.MovieContract;
import com.example.filmoteca.persistence.WLContentContract;
import com.example.filmoteca.persistence.WatchListContract;
import com.example.filmoteca.session.SessionManager;
import com.example.filmoteca.webservice.ServerClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddMovieActivity extends AppCompatActivity {

    private final static String SERVER_URL = "http://10.0.2.2:4000";

    private SessionManager sm;
    private int id_user;
    private int id_wl;
    private String nickname;

    private TmdbMovie movie;
    private List<WatchList> wlists;
    private WatchListAdapter adapter;
    private WatchListContract wlcontract;

    private RecyclerView rv;
    private RelativeLayout errorLayout;
    private FloatingActionButton fab;

    private String wlname;
    private boolean pub;

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private ServerClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        setUpView();
    }

    private void setUpView(){
        // Obtenemos la película
        movie = (TmdbMovie) getIntent().getSerializableExtra("movie");
        sm = new SessionManager(this);
        HashMap<String, String> user = sm.getUserDetails();
        id_user = Integer.parseInt(user.get("id"));
        nickname = user.get("nickname");
        // Iniciamos vista y adaptador
        rv = findViewById(R.id.addMovieView);
        fab = findViewById(R.id.fabWatchList);
        errorLayout = findViewById(R.id.errorLayout);
        wlists = new ArrayList<>();

        // Iniciamos SQLite
        dbHelper = new DbHelper(this, WatchListContract.DB_NAME, null, WatchListContract.DB_VERSION);
        db = dbHelper.getWritableDatabase();

        // Iniciamos retrofit para la consulta al servidor
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        client = retrofit.create(ServerClient.class);

        wlcontract = new WatchListContract();
        wlists = wlcontract.getWatchLists(db);

        adapter = new WatchListAdapter(wlists, false, this);
        LinearLayoutManager lim = new LinearLayoutManager(this);
        lim.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(lim);
        rv.setAdapter(adapter);

        if(adapter.getItemCount()==0){
            errorLayout.setVisibility(View.VISIBLE);
        }else{
            errorLayout.setVisibility(View.GONE);
        }

        fab.setOnClickListener(v -> {
            showAddDialog();
        });

        rv.addOnItemTouchListener(
                new RecyclerViewClickListener(this, rv ,new RecyclerViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        WatchList selected = adapter.getWatchList(position);
                        //  Añadimos la película y su relación con la WatchList a la base de datos interna (SQLite)
                        MovieContract.addMovieDb(movie, db);
                        WLContentContract.addMovieToWl(selected, movie, db);

                        //  Añadimos la película a la BD del servidor
                        addMovieServer();
                        addMovieToWatchListServer(position);
                        finish();
                    }

                    @Override public void onLongItemClick(View view, int position) { }
                })
        );
    }

    private void showAddDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        LayoutInflater inflater = this.getLayoutInflater();
        View titleView = inflater.inflate(R.layout.watchlist_dialog_title, null);
        builder.setCustomTitle(titleView);
        View view = inflater.inflate(R.layout.dialog_watchlist, null);
        builder.setView(view);

        // Set up the buttons
        AlertDialog d = builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss();
            EditText wlnameEt = view.findViewById(R.id.wlNameEt);
            wlname = wlnameEt.getText().toString();
            CheckBox pubcb = view.findViewById(R.id.pubCb);
            pub = pubcb.isChecked();
            if(pub){
                Log.d("PUB", "true");
            }else{
                Log.d("PUB", "false");
            }
            createWatchList();
            addWatchListDB();
        }).setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel()).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.white));
    }
    private void createWatchList(){
        ContentValues values = new ContentValues();
        values.clear();
        values.put(WatchListContract.Column.NAME, wlname);
        values.put(WatchListContract.Column.AUTOR, nickname);
        values.put(WatchListContract.Column.PUBLIC, pub);

        id_wl = (int) db.insertWithOnConflict(WatchListContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        wlists = wlcontract.getWatchLists(db);
        adapter.setWlists(wlists);
        if(adapter.getItemCount()==0){
            errorLayout.setVisibility(View.VISIBLE);
        }else{
            errorLayout.setVisibility(View.GONE);
        }
    }

    private void addWatchListDB(){
        Call<ResponseBody> call = client.addWatchList(id_user, id_wl, wlname, nickname, pub);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("WATCHLIST", "INSERTADA");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("TAG1", "Error: " + t.getMessage());
            }
        });
    }

    private void addMovieServer(){
        Call<ResponseBody> call = callMovieConstructor();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ERROR", t.getMessage());
            }
        });
    }

    private Call<ResponseBody> callMovieConstructor(){
        Call<ResponseBody> call;
        if(movie.getOmdbMovie()==null){
            call = client.addMovie(movie.getId(), movie.getTitle(), movie.getReleaseDate(), movie.getOverview(), "", "",
                    movie.getPosterPath(), movie.getBackdropPath(), "", movie.getVoteAverage(), false);
        }else{
            String poster = "";
            if("N/A".equals(movie.getOmdbMovie().getPoster()) || movie.getOmdbMovie().getPoster().isEmpty()){
                poster = movie.getPosterPath();
            }else{
                poster = movie.getOmdbMovie().getPoster();
            }
            call = client.addMovie(movie.getId(), movie.getTitle(), movie.getReleaseDate(), movie.getOverview(), movie.getOmdbMovie().getDirector(), movie.getOmdbMovie().getActor(),
                    poster, movie.getBackdropPath(), movie.getOmdbMovie().getRuntime(), movie.getOmdbMovie().getImdbRating(), true);
        }
        return call;
    }

    public void addMovieToWatchListServer(int wl){
        HashMap<String, String> user = sm.getUserDetails();
        int userId = Integer.parseInt(user.get("id"));
        Call<ResponseBody> call = client.addMovieToWl(userId, wlists.get(wl).getIdWl(), movie.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("TAG1", "Error: " + t.getMessage());
            }
        });
    }
    
}