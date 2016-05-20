package com.awalone.smartcity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Hashtable;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;


public class MainActivity extends AppCompatActivity {
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    boolean doubleback;
    View parentLayout;

    View vAkun, vArtikel, vBantuan, vTentang;

    MaterialDialog mAkun, mArtikel, mBantuan, mTentang, mKeluar;
    LayoutInflater inflater;

    Button btUpdate, btArtikel, btBantuan, btTentang_;

    EditText ETuser, ETpass;
    String user, pass;
    SessionManager session;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        parentLayout = findViewById(R.id.root_view);
        inflater = getLayoutInflater();

        //view pengaturan akun
        vAkun = inflater.inflate(R.layout.m_akun, null);
        vArtikel = inflater.inflate(R.layout.m_artikel, null);
        vBantuan = inflater.inflate(R.layout.m_bantuan, null);
        vTentang = inflater.inflate(R.layout.m_tentang, null);

        btUpdate = (Button) vAkun.findViewById(R.id.update_akun);
        btArtikel = (Button) vArtikel.findViewById(R.id.ok);
        btBantuan = (Button) vBantuan.findViewById(R.id.ok);
        btTentang_ = (Button) vTentang.findViewById(R.id.ok);

        ETuser = (EditText) vAkun.findViewById(R.id.m_user);
        ETpass = (EditText) vAkun.findViewById(R.id.m_password);

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, new HomeFragment()).commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.akun:
                Akun();
                return true;
            case R.id.artikel:
                Artikel();
                return true;
            case R.id.bantuan:
                Bantuan();
                return true;
            case R.id.tentang:
                Tentang();
                return true;
            case R.id.keluar:
                Keluar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void Artikel() {
        if (vArtikel.getParent() != null) {
            ((ViewGroup) vArtikel.getParent()).removeView(vAkun);
        }
        mArtikel = new MaterialDialog(this)
                .setView(vArtikel);
        mArtikel.show();

        btArtikel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mArtikel.dismiss();
            }
        });
    }

    public void Bantuan() {
        if (vBantuan.getParent() != null) {
            ((ViewGroup) vBantuan.getParent()).removeView(vAkun);
        }
        mBantuan = new MaterialDialog(this)
                .setView(vBantuan);
        mBantuan.show();

        btBantuan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBantuan.dismiss();
            }
        });
    }

    public void Tentang() {
        if (vTentang.getParent() != null) {
            ((ViewGroup) vTentang.getParent()).removeView(vTentang);
        }
        mTentang = new MaterialDialog(this)
                .setView(vTentang);
        mTentang.show();

        btTentang_.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mTentang.dismiss();
            }
        });
    }

    public void Keluar() {
        mKeluar = new MaterialDialog(this).setTitle("Peringatan")
                .setMessage("Anda yakin ingin keluar ?")
                .setPositiveButton("Ya", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        session.logoutUser();
                        startActivity(intent1);
                        finish();
                        mKeluar.dismiss();

                    }
                })
                .setNegativeButton("Batal", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mKeluar.dismiss();
                    }
                });
        mKeluar.show();
    }


    public void Akun() {
        //Clear Previous view

        if (vAkun.getParent() != null) {
            ((ViewGroup) vAkun.getParent()).removeView(vAkun);
        }
        mAkun = new MaterialDialog(this)
                .setView(vAkun);
        mAkun.show();

        btUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                user = ETuser.getText().toString();
                pass = ETpass.getText().toString();
                Log.d("cek",user);
                Log.d("cek",pass);

                updateAkun();
                mAkun.dismiss();
            }
        });
    }

    private void updateAkun() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Mengirim data ke server", "Harap Tunggu...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.AKUN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Snackbar snackbar = Snackbar
                                .make(parentLayout, s, Snackbar.LENGTH_LONG);

                        //reset field
                        ETuser.setText("");
                        ETpass.setText("");

                        snackbar.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Snackbar snackbar = Snackbar
                                .make(parentLayout, volleyError.getMessage().toString(), Snackbar.LENGTH_LONG);
                        snackbar.show();
                        //Showing toast
                        //Toast.makeText(MainActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String


                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();
                SharedPreferences prefProfil = getSharedPreferences("Profil",
                        Context.MODE_PRIVATE);
                //menyiapkan value untuk di kirim ke server
                String ktp = prefProfil.getString("ktp", null);
                //Adding parameters
                params.put("ktp", ktp);
                params.put("user", user);
                params.put("pass", pass);
                //returning parameters
                Log.d("cekibrot1", ktp);
                Log.d("cekibrot1", user);
                Log.d("cekibrot1", pass);


                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Try to fix Double data
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);


        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void onBackPressed() {
        // cek jika drawer layout aktif dan tombol back di tekan maka drawer di
        // tutup

        if (doubleback) {
            super.onBackPressed();
            return;
        }
        this.doubleback = true;
        Snackbar snackbar = Snackbar
                .make(parentLayout, "Tekan kembali sekali lagi untuk keluar", Snackbar.LENGTH_LONG);
        snackbar.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleback = false;
            }
        }, 2000); //delay 2 detik

    }
}