package com.example.filmoteca;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.adapter.RecyclerViewClickListener;
import com.example.filmoteca.adapter.WatchListAdapter;
import com.example.filmoteca.model.MovieData;
import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.model.WatchList;
import com.example.filmoteca.model.WatchListData;
import com.example.filmoteca.persistence.DbHelper;
import com.example.filmoteca.persistence.SubscriptionContract;
import com.example.filmoteca.session.SessionManager;
import com.example.filmoteca.webservice.ServerClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

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

public class SocialFragment extends Fragment implements SearchView.OnQueryTextListener {

    private final static String SERVER_URL = "http://10.0.2.2:4000";
    private boolean fromSearch;

    private ImageButton logoutBtn;

    private SessionManager sm;

    private RecyclerView rv;
    private RelativeLayout errorLayout;
    private BottomNavigationView nav;
    private SearchView svPublic;
    private TextView subsTv;
    private WatchListAdapter adapter;
    private List<WatchList> publicWls;

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
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        setUpView(view);
        getSubscriptions();
        return view;
    }

    public void setUpView(View view){
        // Nos aseguramos que el botón de la NavigationBar está marcado correctamente.
        nav = getActivity().findViewById(R.id.navigationView);
        if(nav.getSelectedItemId() != R.id.user_group_nav){
            nav.setSelectedItemId(R.id.user_group_nav);
            getActivity().getSupportFragmentManager().popBackStack();
        }

        fromSearch = false;
        // Iniciamos SQLite
        dbHelper = new DbHelper(getContext(), SubscriptionContract.DB_NAME, null, SubscriptionContract.DB_VERSION);
        db = dbHelper.getWritableDatabase();

        sm = new SessionManager(getContext());

        // Barra de búsqueda y su listener
        svPublic = view.findViewById(R.id.svPublic);
        svPublic.setOnQueryTextListener(this);
        subsTv = view.findViewById(R.id.subsTv);
        subsTv.setVisibility(View.VISIBLE);

        // Iniciamos vista
        publicWls = new ArrayList<>();
        rv = view.findViewById(R.id.socialView);
        errorLayout = view.findViewById(R.id.errorLayout);
        adapter = new WatchListAdapter(publicWls, true, getContext());
        LinearLayoutManager lim = new LinearLayoutManager(getActivity());
        lim.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(lim);
        rv.setAdapter(adapter);
        rv.addOnItemTouchListener(
                new RecyclerViewClickListener(getActivity(), rv ,new RecyclerViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Fragment fragment = new PublicWLDetailFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("watchlist", adapter.getWatchList(position));
                        fragment.setArguments(bundle);

                        transaction.add(R.id.frame_container, fragment);
                        transaction.addToBackStack("SocialFragment");
                        transaction.commit();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        if(!fromSearch){
                            showUnsubscribeDialog(position);
                        }
                    }
                })
        );

        // Iniciamos retrofit
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        client = retrofit.create(ServerClient.class);

        logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            showLogOutDialog();
        });
    }

    private void searchPublicWatchLists(String input){
        Call<WatchListData> call = client.getPublicWatchLists(input);
        call.enqueue(new Callback<WatchListData>() {
            @Override
            public void onResponse(Call<WatchListData> call, Response<WatchListData> response) {
                publicWls = response.body().getWatchLists();
                if(publicWls.size()!=0){
                    getPublicMovies(publicWls);
                }else{
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<WatchListData> call, Throwable t) {
                Log.d("TAG1", "Error: " + t.getMessage());
            }
        });
    }

    private void getSubscriptions(){
        HashMap<String, String> user = sm.getUserDetails();
        int idUser = Integer.parseInt(user.get("id"));
        Call<WatchListData> call4 = client.getSubscriptionWls(idUser);
        call4.enqueue(new Callback<WatchListData>() {
            @Override
            public void onResponse(Call<WatchListData> call, Response<WatchListData> response) {
                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code()==200) {
                    if(response.body().getWatchLists().size()!=0){
                        errorLayout.setVisibility(View.GONE);
                        List<WatchList> wls = response.body().getWatchLists();
                        if(wls.size()!=0){
                            getPublicMovies(wls);
                        }
                    }else{
                        errorLayout.setVisibility(View.VISIBLE);
                    }
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NotNull Call<WatchListData> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
                errorLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getPublicMovies(List<WatchList> publicWls){
        for(WatchList wl : publicWls){
            Call<MovieData> call2 = client.getPublicMovies(wl.getIdUser(), wl.getIdWl());
            call2.enqueue(new Callback<MovieData>() {
                @Override
                public void onResponse(Call<MovieData> call, Response<MovieData> response) {
                    Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                    if(response.code()==200) {
                        if(response.body().getMovies().size()!=0){
                            List<TmdbMovie> publicMvs = TmdbMovie.movieAdapter(response.body().getMovies());
                            //getPublicContents(wl, publicMvs);
                            wl.setMovies(publicMvs);
                            adapter.addData(wl);
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
        }
    }

    private void showUnsubscribeDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View titleView = inflater.inflate(R.layout.unsubscribe_dialog_title, null);
        View custom = inflater.inflate(R.layout.dialog_name, null);
        TextView name = custom.findViewById(R.id.nameTv);
        name.setText(adapter.getWatchList(position).getName());
        builder.setView(custom);
        builder.setCustomTitle(titleView);

        // Set up the buttons
        AlertDialog d = builder.setPositiveButton("Sí", (dialog, which) -> {
            dialog.dismiss();
            db.delete(SubscriptionContract.TABLE, SubscriptionContract.Column.IDCREATOR + "=" + adapter.getWatchList(position).getIdUser()+ " and "+ SubscriptionContract.Column.IDWL + "=" + adapter.getWatchList(position).getIdWl(), null);
            unsubscribe(adapter.getWatchList(position).getIdUser(), adapter.getWatchList(position).getIdWl());
            adapter.deleteWatchList(position);
            if(adapter.getItemCount() == 0){
                errorLayout.setVisibility(View.VISIBLE);
            }
        }).setNegativeButton("No", (dialog, which) -> dialog.cancel()).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void unsubscribe(int idCreator, int idWl){
        HashMap<String, String> user = sm.getUserDetails();
        Call<ResponseBody> call6 = client.unsubscribe(Integer.parseInt(user.get("id")), idCreator, idWl);
        call6.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("RESPONSE_CODE", String.valueOf(response.code()));
                if(response.code()==200) {
                }else if(response.code() == 401){
                    Log.d("ERROR 401 ", response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, Throwable t) {
                Log.d("CONNECTION_ERROR", t.getMessage());
            }
        });
    }

    private void showLogOutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View titleView = inflater.inflate(R.layout.logout_dialog_title, null);
        builder.setCustomTitle(titleView);

        // Set up the buttons
        AlertDialog d = builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss();
            sm.logoutUser();
            getContext().deleteDatabase("filmoteca.db");
            getActivity().finish();
        }).setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel()).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    @Override
    public boolean onQueryTextSubmit(String input) {
        if(input.length()>=2){
            errorLayout.setVisibility(View.GONE);
            adapter.clearData();
            fromSearch = true;
            subsTv.setVisibility(View.GONE);
            svPublic.clearFocus();
            searchPublicWatchLists(input);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

}