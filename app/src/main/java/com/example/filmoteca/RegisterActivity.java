package com.example.filmoteca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filmoteca.model.UserData;
import com.example.filmoteca.session.SessionManager;
import com.example.filmoteca.webservice.ServerClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    private SessionManager sm;

    private ServerClient client;

    private TextInputEditText emailEt;
    private TextInputEditText nickEt;
    private TextInputEditText passwordEt;
    private TextInputEditText passwordConfirmEt;
    private MaterialButton registerBtn;
    private TextView errorTv;

    private String email;
    private String nick;
    private int id;
    private String password;
    private String rePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sm = new SessionManager(getApplicationContext());
        setUpView();
    }

    private void setUpView(){
        setResult(LoginActivity.RESULT_OK);
        Bundle extras = getIntent().getExtras();
        String emailLogin = "";
        String passwordLogin = "";
        if(extras!=null){
            emailLogin = extras.getString("EMAIL");
            passwordLogin = extras.getString("PASSWORD");
        }

        emailEt = findViewById(R.id.emailEt);
        if(!emailLogin.isEmpty()){
            emailEt.setText(emailLogin);
        }
        nickEt = findViewById(R.id.nickEt);
        passwordEt = findViewById(R.id.passwordEt);
        if(!passwordLogin.isEmpty()){
            passwordEt.setText(passwordLogin);
        }
        passwordConfirmEt = findViewById(R.id.passwordConfirmEt);

        registerBtn = findViewById(R.id.registerBtn);
        errorTv = findViewById(R.id.errorTv);

        registerBtn.setOnClickListener(v -> {
            email = emailEt.getText().toString();
            nick = nickEt.getText().toString();
            password = passwordEt.getText().toString();
            rePassword = passwordConfirmEt.getText().toString();

            if(email.isEmpty() || nick.isEmpty() || password.isEmpty() || rePassword.isEmpty()){
                errorTv.setText(R.string.empty_error);
            }else if(password.length() < 8){
                errorTv.setText(R.string.password_length_error);
            }else{
                if(password.equals(rePassword)){
                    sendRegisterPetition();
                }else{
                    errorTv.setText(R.string.match_error);
                }
            }
        });

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:4000")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        client = retrofit.create(ServerClient.class);
    }

    public void sendRegisterPetition(){
        Call<ResponseBody> call = client.register(email, nick, password, rePassword);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("CONNECTION_SUCCESS", "Se ha establecido conexión con el servidor (registro)");
                if(response.code()==200) {
                    getUserData();
                }else if(response.code() == 500){
                    Log.d("ERROR 500 ", response.message());
                    errorTv.setText(R.string.register_error);
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
                if(response.code() == 200){
                    id = response.body().getId()[0];
                    Log.d("USER_ID", String.valueOf(id));
                    Log.d("USER_NICKNAME", nick);
                    manageSuccessfulRegister();

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

    private void manageSuccessfulRegister(){
        // Creamos la sesión
        sm.createLoginSession(email, id, nick);

        // Cerramos la actividad del login
        Intent intentReturn = new Intent();
        setResult(LoginActivity.RESULT_CANCELED, intentReturn);

        // Iniciamos MainActivity
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.putExtra("register", true);
        startActivity(i);
        finish();
    }
}