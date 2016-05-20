package com.awalone.smartcity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Splash extends AppCompatActivity {
    //waktu splash screen
    private static int splashInterval = 3000;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {

                                      @Override
                                      public void run() {
                                          Intent i = new Intent(Splash.this, LoginActivity.class);
                                          startActivity(i);
                                          Splash.this.finish();
                                      }
                                  },
                splashInterval);
    }
}
