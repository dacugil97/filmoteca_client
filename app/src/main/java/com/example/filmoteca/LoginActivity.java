package com.example.filmoteca;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filmoteca.model.MovieData;
import com.example.filmoteca.model.SubscriptionData;
import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.model.UserData;
import com.example.filmoteca.model.WLContentData;
import com.example.filmoteca.model.WatchList;
import com.example.filmoteca.model.WatchListSub;
import com.example.filmoteca.model.WatchListData;
import com.example.filmoteca.persistence.DbHelper;
import com.example.filmoteca.persistence.MovieContract;
import com.example.filmoteca.persistence.SubscriptionContract;
import com.example.filmoteca.persistence.WLContentContract;
import com.example.filmoteca.persistence.WatchListContract;
import com.example.filmoteca.session.SessionManager;
import com.example.filmoteca.webservice.ServerClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://10.0.2.2:4000";

    private final int REQUEST_EXIT = 1;
    private SessionManager sm;

    private ServerClient client;

    private SQLiteDatabase db;

    private TextInputEditText emailEt;
    private TextInputEditText passwordEt;
    private MaterialButton signInBtn;
    private TextView registerTv;
    private TextView errorTv;

    private String email, password, nick;
    private int idUser;

    private List<WatchList> userWls;
    private List<MovieData.MovieDataDetail> userMvs;
    private List<WLContentData.WlContentDataDetail> wlContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sm = new SessionManager(getApplicationContext());
        setUpView();
    }

    private void setUpView() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        client = retrofit.create(ServerClient.class);

        // Iniciamos SQLite
        DbHelper dbHelper = new DbHelper(this, WatchListContract.DB_NAME, null, WatchListContract.DB_VERSION);
        db = dbHelper.getWritableDatabase();

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        signInBtn = findViewById(R.id.signInBtn);
        registerTv = findViewById(R.id.registerTv);
        errorTv = findViewById(R.id.errorTv);

        signInBtn.setOnClickListener(v -> {
            email = emailEt.getText().toString();
            password = passwordEt.getText().toString();
            sendLogInPetition();
        });

        registerTv.setOnClickListener(v -> {
            email = emailEt.getText().toString();
            password = passwordEt.getText().toString();
            Intent i = new Intent(this, RegisterActivity.class);
            i.putExtra("EMAIL", email);
            i.putExtra("PASSWORD", password);
            startActivityForResult(i, REQUEST_EXIT);
            this.overridePendingTransition(0, 0);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EXIT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }

    private void sendLogInPetition(){
        Call<ResponseBody> call = client.login(email, password);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("CONNECTION_SUCCESS", "Se ha establecido conexión con el servidor (login)");
                if(response.code()==200) {
                    getUserData();
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                    errorTv.setText(R.string.login_user_error);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });
    }

    private void getUserData(){
        Call<UserData> call = client.getUserData(email);
        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                Log.d("CONNECTION_SUCCESS", "Se ha establecido conexión con el servidor (getData)");

                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code() == 200){
                    idUser = response.body().getId()[0];
                    nick = response.body().getNickname()[0];
                    Log.d("USER_ID", String.valueOf(idUser));
                    Log.d("USER_NICKNAME", nick);
                    getUserWatchLists();
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                }

            }

            @Override
            public void onFailure(@NotNull Call<UserData> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });
    }
    
    private void getUserWatchLists(){
        Call<WatchListData> call2 = client.getUserWatchlists(idUser);
        call2.enqueue(new Callback<WatchListData>() {
            @Override
            public void onResponse(Call<WatchListData> call, Response<WatchListData> response) {

                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code()==200) {
                    userWls = response.body().getWatchLists();
                    WatchListContract.insertWatchLists(userWls, db);
                    for(WatchList wl : userWls){
                        Log.d(wl.getName(), "cargada de BD");
                    }
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                }

            }

            @Override
            public void onFailure(@NotNull Call<WatchListData> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });

        Call<MovieData> call3 = client.getUserMovies(idUser);
        call3.enqueue(new Callback<MovieData>() {
            @Override
            public void onResponse(Call<MovieData> call, Response<MovieData> response) {
                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code()==200) {
                    userMvs = response.body().getMovies();
                    List<TmdbMovie> movies = TmdbMovie.movieAdapter(userMvs);
                    for(TmdbMovie m : movies){
                        MovieContract.addMovieDb(m, db);
                    }
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<MovieData> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });

        Call<WLContentData> call4 = client.getUserWLContents(idUser);
        call4.enqueue(new Callback<WLContentData>() {
            @Override
            public void onResponse(Call<WLContentData> call, Response<WLContentData> response) {
                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code()==200) {
                    wlContents = response.body().getContents();
                    WLContentContract.insertData(wlContents, db);
                    manageSuccessfulLogin();
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<WLContentData> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });

        Call<SubscriptionData> call5 = client.getSubscriptions(idUser);
        call5.enqueue(new Callback<SubscriptionData>() {
            @Override
            public void onResponse(Call<SubscriptionData> call, Response<SubscriptionData> response) {
                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code()==200) {
                    List<WatchListSub> subs = response.body().getSubs();
                    for(WatchListSub sub : subs){
                        SubscriptionContract.addSubscription(sub.getIdUser(), sub.getIdCreator(), sub.getIdWl(), db);
                    }
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<SubscriptionData> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });
    }

    private void manageSuccessfulLogin(){
        // Creamos la sesión
        Log.d("USER_LOGIN", String.valueOf(idUser));
        sm.createLoginSession(email, idUser, nick);

        // Iniciamos MainActivity
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }
}