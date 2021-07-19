package com.example.filmoteca;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.adapter.MovieAdapter;
import com.example.filmoteca.adapter.RecyclerViewClickListener;
import com.example.filmoteca.model.WatchList;
import com.example.filmoteca.persistence.DbHelper;
import com.example.filmoteca.persistence.SubscriptionContract;
import com.example.filmoteca.persistence.WatchListContract;
import com.example.filmoteca.session.SessionManager;
import com.example.filmoteca.webservice.ServerClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PublicWLDetailFragment extends Fragment {

    private final static String SERVER_URL = "http://10.0.2.2:4000";

    private SessionManager sm;

    private boolean subscribed;
    private int idUser;

    private TextView wlName;
    private RecyclerView rv;
    private BottomNavigationView nav;
    private TextView autorTv;
    private TextView followingTv;
    private FloatingActionButton fabSub;

    private WatchList wl;
    private MovieAdapter adapter;

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private ServerClient client;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_wl_detail, container, false);
        setUpView(view);
        return view;
    }

    public void setUpView(View view){
        // Nos aseguramos que el botón de la NavigationBar está marcado correctamente.
        nav = getActivity().findViewById(R.id.navigationView);
        if(nav.getSelectedItemId() != R.id.user_group_nav){
            nav.setSelectedItemId(R.id.user_group_nav);
            getActivity().getSupportFragmentManager().popBackStack();
        }

        // Obtenemos la watchlist
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            wl = (WatchList) bundle.getSerializable("watchlist");
        }

        // Obtenemos los datos de la sesión
        sm = new SessionManager(getContext());
        HashMap<String, String> user = sm.getUserDetails();
        idUser = Integer.parseInt(user.get("id"));

        // Iniciamos SQLite
        dbHelper = new DbHelper(getContext(), WatchListContract.DB_NAME, null, WatchListContract.DB_VERSION);
        db = dbHelper.getWritableDatabase();
        
        subscribed = SubscriptionContract.isSubscribed(idUser, wl.getIdUser(), wl.getIdWl(), db);

        autorTv = view.findViewById(R.id.autorTv);
        autorTv.setText(wl.getAutor());
        followingTv = view.findViewById(R.id.followingTv);

        Collections.sort(wl.getMovies(), (m1, m2) -> m1.getReleaseDate().compareToIgnoreCase(m2.getReleaseDate()));
        adapter = new MovieAdapter(wl.getMovies());
        rv = view.findViewById(R.id.watchListDetailView);
        LinearLayoutManager lim = new LinearLayoutManager(getContext());
        lim.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(lim);
        rv.setAdapter(adapter);
        rv.addOnItemTouchListener(
                new RecyclerViewClickListener(getContext(), rv ,new RecyclerViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        getActivityDetail(position);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                    }
                })
        );

        wlName = view.findViewById(R.id.wlNameTv);
        wlName.setText(wl.getName());
        fabSub = view.findViewById(R.id.fabSubscribe);
        if(subscribed){
            fabSub.setVisibility(View.GONE);
            followingTv.setVisibility(View.VISIBLE);
        }else if(wl.getIdUser() == idUser){
            fabSub.setVisibility(View.GONE);
            followingTv.setVisibility(View.GONE);
            autorTv.setText("ti, " + wl.getAutor());
        }else{
            fabSub.setOnClickListener(v -> {
                showSubscribeDialog();
            });
        }
    }

    public void getActivityDetail(int position){
        Intent intent = new Intent (getContext(), MovieDetailActivity.class);
        intent.putExtra("movie", adapter.getMovie(position));
        intent.putExtra("WatchList", false);
        startActivity(intent);
    }

    private void showSubscribeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View titleView = inflater.inflate(R.layout.subscribe_wl_dialog_title, null);
        View custom = inflater.inflate(R.layout.dialog_name, null);
        TextView name = custom.findViewById(R.id.nameTv);
        name.setText(wl.getName());
        builder.setView(custom);
        builder.setCustomTitle(titleView);

        // Set up the buttons
        AlertDialog d = builder.setPositiveButton("Sí", (dialog, which) -> {
            dialog.dismiss();
            subscribeToWatchList();
            fabSub.setVisibility(View.GONE);
        }).setNegativeButton("No", (dialog, which) -> dialog.cancel()).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void subscribeToWatchList(){

        SubscriptionContract.addSubscription(idUser, wl.getIdUser(), wl.getIdWl(), db);

        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        client = retrofit.create(ServerClient.class);

        Call<ResponseBody> call = client.subscribeToWl(idUser, wl.getIdUser(), wl.getIdWl());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                followingTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("TAG1", "Error: " + t.getMessage());
            }
        });

    }

}