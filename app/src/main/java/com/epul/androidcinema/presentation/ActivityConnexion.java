package com.epul.androidcinema.presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.epul.androidcinema.MainActivity;
import com.epul.androidcinema.R;
import com.epul.androidcinema.domain.*;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import org.json.JSONException;
import org.json.JSONObject;

import com.epul.androidcinema.error.MonException;
import com.epul.androidcinema.service.RetrofitClient;
import com.epul.androidcinema.service.ServiceConnexion;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class ActivityConnexion extends AppCompatActivity implements View.OnClickListener {

    private EditText txtNom;
    private EditText txtPwd;
    private Button btValider;
    private Button btAnnuler;
    private String nom;
    private String pwd;
    private static final String TAG = "Connexion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // On se relie au design
        setContentView(R.layout.activity_connexion);
        // on récupère le nom et le mot de passe
        txtNom = (EditText) findViewById(R.id.edLogin);
        txtPwd = (EditText) findViewById(R.id.edPwd);
        btValider = (Button) findViewById(R.id.btSignIn);
        btValider.setOnClickListener(this);
        btAnnuler = (Button) findViewById(R.id.btAnnuler);
        btAnnuler.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        this.nom = txtNom.getText().toString();
        this.pwd = txtPwd.getText().toString();
        Intent intent = new Intent(ActivityConnexion.this, MainActivity.class);
        int retour;
        if (v == btValider) {
            // Contrôle  de l'utilisateur

            LoginParam unLogin = new LoginParam(nom, pwd);
            controleUtilisateur(unLogin);
        }

        if(v == btAnnuler) {
            setResult(Activity.RESULT_OK, intent);
            //super.finish();
        }

    }

    public void controleUtilisateur(LoginParam unLogin) {
        boolean retour = false;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        // On crée un adapteur rest sur l'url
        try {
            Retrofit retrofit = RetrofitClient.getClientRetrofit(this);
            ServiceConnexion uneConnexionService = retrofit.create(ServiceConnexion.class);
            // On appelle la méthode qui retourne les frais
            Call<Object> call = uneConnexionService.getConnexion(unLogin);

            // appel asynchrone
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> uneReponse) {
                    System.out.println("successful : " + uneReponse.isSuccessful());
                    System.out.println("headers : " + uneReponse.headers());
                    System.out.println("body : " + uneReponse.body());
                    if (uneReponse.isSuccessful()) {
                        //Recupérer le corps de la reponse que Retrofit s'est chargé de désérialiser à notre place l'aide du convertor Gson
                        if (uneReponse.body() != null) {
                            Object unObjet = uneReponse.body();
                            String jsonString = (new Gson().toJson(unObjet));
                            JSONObject unJSO = null;
                            try {
                                unJSO = new JSONObject(jsonString);
                                String unToken = unJSO.getString("accessToken");
                                Toast.makeText(ActivityConnexion.this, "Authentification réussie !!!", Toast.LENGTH_LONG).show();
                                stocke(unToken);
                            } catch (JSONException e) {
                                new MonException(e.getMessage(), "Erreur Appel WS Connexion");
                            }
                        } else {
                            Toast.makeText(ActivityConnexion.this, "Erreur d'appel!", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(ActivityConnexion.this, "Erreur rencontrée", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse =>>> code = " + uneReponse.code());
                    }
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    System.out.println("call : " + call);
                    System.out.println("t : " + t);
                    Toast.makeText(ActivityConnexion.this, "Erreur de connexion", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IllegalStateException | JsonSyntaxException exception) {
            new MonException(exception.getMessage(), "Erreur Appel WS Connexion");
        } catch (Exception e) {
            new MonException(e.getMessage(), "Erreur Appel WS Connexion");
        }
    }

    // retour vers  les informations à la fenêtre principale
    private void stocke(String unTk) {
        System.out.println("stocke() : " + unTk);
        int retour = 1;
        Intent intent = new Intent(ActivityConnexion.this, MainActivity.class);
        intent.putExtra("unToken", unTk);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }
}