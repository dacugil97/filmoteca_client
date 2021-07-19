package com.example.filmoteca;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.filmoteca.session.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpSession();

        fragment = new WatchListFragment();
        boolean fromRegister = getIntent().getBooleanExtra("register", false);
        if(fromRegister){
            Bundle b = new Bundle();
            b.putBoolean("register", true);
            fragment.setArguments(b);
        }

        BottomNavigationView navigation = findViewById(R.id.navigationView);
        navigation.setOnNavigationItemSelectedListener(toolbarListener);
        loadFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void setUpSession(){
        SessionManager sm = new SessionManager(getApplicationContext());
        if(!sm.checkLogin()){
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener toolbarListener
            = item -> {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.user_lists_nav:
                        fragment = new WatchListFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.search_nav:
                        fragment = new SearchFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.user_group_nav:
                        fragment = new SocialFragment();
                        loadFragment(fragment);
                        return true;
                }
                return false;
            };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}