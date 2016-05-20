package com.awalone.smartcity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import fr.ganfra.materialspinner.MaterialSpinner;


public class Register extends AppCompatActivity {
    EditText password, password_ulang, ktp, email, nama, alamat, telpon;

    Button Kirim, login;
    MaterialSpinner sp_jk;
    View parentLayout;

    String Spassword, SpasswordUlang, Sktp, Semail, Snama, Salamat, Stelpon, Sjk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        parentLayout = findViewById(R.id.root_view);
        setTitle("Daftar");

        password = (EditText) findViewById(R.id.r_pass);
        password_ulang = (EditText) findViewById(R.id.r_passulang);
        ktp = (EditText) findViewById(R.id.r_ktp);
        email = (EditText) findViewById(R.id.r_email);
        nama = (EditText) findViewById(R.id.r_nama);
        alamat = (EditText) findViewById(R.id.r_alamat);
        telpon = (EditText) findViewById(R.id.r_telpon);

        login = (Button) findViewById(R.id.masuk);

        Kirim = (Button) findViewById(R.id.daftar);
        sp_jk = (MaterialSpinner) findViewById(R.id.jk);
        String[] ITEMS = {"Laki-Laki", "Perempuan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_jk.setAdapter(adapter);


        sp_jk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Sjk = sp_jk.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                Sjk = "Jenis Kelamin";
            }
        });

        Kirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Spassword = password.getText().toString();
                SpasswordUlang = password_ulang.getText().toString();
                Sktp = ktp.getText().toString();
                Semail = email.getText().toString();
                Snama = nama.getText().toString();
                Salamat = alamat.getText().toString();
                Stelpon = telpon.getText().toString();

                if (password.getText().toString().trim().length() == 0 || ktp.getText().toString().trim().length() == 0 || email.getText().toString().trim().length() == 0 || nama.getText().toString().trim().length() == 0 || alamat.getText().toString().trim().length() == 0 || telpon.getText().toString().trim().length() == 0 || sp_jk.getSelectedItem().toString().trim().length() == 0) {
                    Snackbar snackbar = Snackbar
                            .make(v, "Data tidak boleh kosong", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (Sjk.equals("Jenis Kelamin")) {
                    //Register();
                    Snackbar snackbar = Snackbar
                            .make(v, "Pilih Jenis Kelamin", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (!(Spassword.equals(SpasswordUlang))) {
                    Snackbar snackbar = Snackbar
                            .make(v, "Kata sandi tidak sesuai", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Register();
                }

            }
        });

    }

    private void Register() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Mengirim data ke server", "Harap Tunggu...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Snackbar snackbar = Snackbar
                                .make(parentLayout, s, Snackbar.LENGTH_LONG);
                        snackbar.show();

                        if (s.equals("Berhasil membuat akun")) {
                            reset();

                            login.setVisibility(View.VISIBLE);
                            login.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Register.this, LoginActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            });
                        }


                        //Toast.makeText(MainActivity.this, s , Toast.LENGTH_LONG).show();
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
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String


                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();
                //  SharedPreferences prefProfil = getActivity().getSharedPreferences("Profil",
                //        Context.MODE_PRIVATE);
                //menyiapkan value untuk di kirim ke server


                //Adding parameters
                params.put("ktp", Sktp);
                params.put("email", Semail);
                params.put("nama", Snama);
                params.put("jk", Sjk);
                params.put("alamat", Salamat);
                params.put("telepon", Stelpon);
                params.put("password", SpasswordUlang);


                //returning parameters
                Log.d("cekibrot", Sktp);
                Log.d("cekibrot", Semail);
                Log.d("cekibrot", Snama);
                Log.d("cekibrot", Sjk);
                Log.d("cekibrot", Salamat);
                Log.d("cekibrot", Stelpon);
                Log.d("cekibrot", SpasswordUlang);


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

    public void reset() {
        password.setText("");
        password_ulang.setText("");
        ktp.setText("");
        email.setText("");
        nama.setText("");
        alamat.setText("");
        telpon.setText("");
        sp_jk.setSelection(0);
    }
}