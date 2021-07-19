package com.example.filmoteca;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.adapter.RecyclerViewClickListener;
import com.example.filmoteca.adapter.WatchListAdapter;
import com.example.filmoteca.model.WatchList;
import com.example.filmoteca.persistence.DbHelper;
import com.example.filmoteca.persistence.WLContentContract;
import com.example.filmoteca.persistence.WatchListContract;
import com.example.filmoteca.session.SessionManager;
import com.example.filmoteca.webservice.ServerClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class WatchListFragment extends Fragment {

    private final static String SERVER_URL = "http://10.0.2.2:4000";

    private RecyclerView rv;
    private RelativeLayout errorLayout;
    private BottomNavigationView nav;
    private TextView nickTv;
    private List<WatchList> wlists;
    private WatchListAdapter adapter;
    private FloatingActionButton fab;

    private SessionManager sm;
    private String wlname;
    private boolean pub = false;
    private int id_wl;
    private int idUser;
    private String nickname;

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private Retrofit retrofit;
    private HttpLoggingInterceptor loggingInterceptor;
    private OkHttpClient.Builder httpClientBuilder;
    private ServerClient client;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_list, container, false);
        setUpView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0){
            getActivity().finish();
        }
    }

    private void setUpView(View view){
        // Nos aseguramos que el botón de la NavigationBar está marcado correctamente.
        nav = getActivity().findViewById(R.id.navigationView);
        if(nav.getSelectedItemId() != R.id.user_lists_nav){
            nav.setSelectedItemId(R.id.user_lists_nav);
            getActivity().getSupportFragmentManager().popBackStack();
        }

        // Obtenemos datos de la sesión
        sm = new SessionManager(getContext());
        HashMap<String, String> user = sm.getUserDetails();
        idUser = Integer.parseInt(user.get("id"));
        nickname = user.get("nickname");

        // Iniciamos vista
        rv = view.findViewById(R.id.watchListView);
        errorLayout = view.findViewById(R.id.errorLayout);
        nickTv = view.findViewById(R.id.nickTv);
        nickTv.setText(nickname);
        wlists = new ArrayList<>();
        fab = view.findViewById(R.id.fabWl);

        //Comprobamos si el fragment se carga desde el registro
        Bundle b = this.getArguments();
        if (b != null) {
            boolean fromRegister = b.getBoolean("register", false);
            if(fromRegister){
                showWelcomeDialog();
            }
        }

        // Iniciamos SQLite
        dbHelper = new DbHelper(getContext(), WatchListContract.DB_NAME, null, WatchListContract.DB_VERSION);
        db = dbHelper.getWritableDatabase();

        // Iniciamos retrofit
        loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
        client = retrofit.create(ServerClient.class);

        wlists = WatchListContract.getWatchLists(db);
        adapter = new WatchListAdapter(wlists, false, getContext());
        if (adapter.getItemCount() == 0){
            errorLayout.setVisibility(View.VISIBLE);
        }else{
            errorLayout.setVisibility(View.GONE);
        }

        fab.setOnClickListener(v -> {
            showAddDialog();
        });

        LinearLayoutManager lim = new LinearLayoutManager(getActivity());
        lim.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(lim);
        rv.setAdapter(adapter);
        rv.addOnItemTouchListener(
                new RecyclerViewClickListener(getActivity(), rv ,new RecyclerViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Fragment fragment = new WatchListDetailFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("watchlist", adapter.getWatchList(position));
                        fragment.setArguments(bundle);

                        transaction.replace(R.id.frame_container, fragment);
                        transaction.addToBackStack(null);

                        transaction.commit();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        showDeleteDialog(position);
                    }
                })
        );
    }

    private void showAddDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
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
            Log.d("NAME", wlname);
            if(pub){
                Log.d("PUB", "true");
            }else{
                Log.d("PUB", "false");
            }
            createWatchList();
            addWatchListServer();
        }).setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel()).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void showDeleteDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View titleView = inflater.inflate(R.layout.delete_watchlist_dialog_title, null);
        View custom = inflater.inflate(R.layout.dialog_name, null);
        TextView name = custom.findViewById(R.id.nameTv);
        name.setText(wlists.get(position).getName());
        builder.setView(custom);
        builder.setCustomTitle(titleView);

        // Configuramos los botones
        AlertDialog d = builder.setPositiveButton("Sí", (dialog, which) -> {
            dialog.dismiss();
            db.delete(WLContentContract.TABLE, WLContentContract.Column.IDWL + "=" + wlists.get(position).getIdWl(), null);
            db.delete(WatchListContract.TABLE, WatchListContract.Column.ID + "=" + wlists.get(position).getIdWl(), null);
            deleteWatchListServer(wlists.get(position).getIdWl());
            adapter.deleteWatchList(position);
            if (adapter.getItemCount() == 0){
                errorLayout.setVisibility(View.VISIBLE);
            }else{
                errorLayout.setVisibility(View.GONE);
            }
        }).setNegativeButton("No", (dialog, which) -> dialog.cancel()).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void showWelcomeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View titleView = inflater.inflate(R.layout.welcome_dialog_title, null);
        View guide = inflater.inflate(R.layout.dialog_welcome_guide, null);
        TextView user = titleView.findViewById(R.id.userTv);
        user.setText(nickname);
        builder.setView(guide);
        builder.setCustomTitle(titleView);

        // Set up the buttons
        AlertDialog d = builder.setPositiveButton("¡A tope!", (dialog, which) -> {
            dialog.dismiss();
        }).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void createWatchList(){
        ContentValues values = new ContentValues();
        values.clear();
        values.put(WatchListContract.Column.NAME, wlname);
        values.put(WatchListContract.Column.AUTOR, nickname);
        values.put(WatchListContract.Column.PUBLIC, pub);

        id_wl = (int) db.insertWithOnConflict(WatchListContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        wlists = WatchListContract.getWatchLists(db);
        adapter.setWlists(wlists);

        if (adapter.getItemCount() == 0){
            errorLayout.setVisibility(View.VISIBLE);
        }else{
            errorLayout.setVisibility(View.GONE);
        }
    }

    private void addWatchListServer(){
        Call<ResponseBody> call = client.addWatchList(idUser, id_wl, wlname, nickname, pub);
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

    private void deleteWatchListServer(int idWl){
        Call<ResponseBody> call = client.deleteWatchList(idUser, idWl);
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