package com.awalone.smartcity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    private AutoCompleteTextView mUser;
    private EditText mPassword;
    Button btnLogin, btnDaftar;
    SessionManager session;
    public static final String USER_ID = "USERID";
    String username, pass,ktp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Masuk");

        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn() == true) {
            Intent a = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(a);
            finish();
        }
        // Set up the login form.
        mUser = (AutoCompleteTextView) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.login);
        btnDaftar = (Button) findViewById(R.id.daftar);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                username= mUser.getText().toString();
                pass= mPassword.getText().toString();
                login(username, pass);
            }
        });

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, Register.class);
                startActivity(i);
            }
        });
    }

    private void login(final String username, final String password) {
        class LoginAsync extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingDialog = ProgressDialog.show(LoginActivity.this, "Harap Tunggu", "Menghubungkan ke server...");
            }

            @Override
            protected String doInBackground(String... params) {
                String uname = params[0];
                String pass = params[1];

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("id_user", uname));
                nameValuePairs.add(new BasicNameValuePair("password", pass));
                String result = null;

                StringBuilder sb = null;
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(Config.LOGIN);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return String.valueOf(sb);
            }

            @Override
            protected void onPostExecute(String result) {
                String s = result.trim();
                Log.d("login", s);
                try {
                    JSONObject json = new JSONObject(s);
                    JSONArray hasil = json.getJSONArray("login");


                    //Cek status sukses atau tidak
                    String success = json.getString("success");

                    loadingDialog.dismiss();
                    if (success.equals("1")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(USER_ID, username);
                        session.createLoginSession(username, password);
                        //Mengambil data pengguna hasil dari json encode
                        for (int i = 0; i < hasil.length(); i++) {
                            JSONObject jsonobj = hasil.getJSONObject(i);
                            ktp = jsonobj.get("ktp").toString();
//
//                            //Menyimpan temporari data di sharedpreference
                            SharedPreferences prefProfil = getSharedPreferences("Profil",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor mEditorProfil = prefProfil.edit();
                            mEditorProfil.putString("ktp", ktp);
                            mEditorProfil.commit();
                        }
                        finish();
                        startActivity(intent);
                    } else {

                        Toast.makeText(getApplicationContext(), "Nama pengguna atau kata sandi salah", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    loadingDialog.dismiss();
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        LoginAsync la = new LoginAsync();
        la.execute(username, password);

    }
}

