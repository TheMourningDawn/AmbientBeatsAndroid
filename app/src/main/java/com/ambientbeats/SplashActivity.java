package com.ambientbeats;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.particle.android.sdk.cloud.ParticleCloudSDK;

public class SplashActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParticleCloudSDK.init(SplashActivity.this);
        setContentView(R.layout.activity_splash);

        Context ctx = getApplicationContext();

        SharedPreferences pref = getSharedPreferences("LoginPREF", Context.MODE_PRIVATE);
        if (pref.getBoolean("logged_in", false)) {
            Intent intent = new Intent(ctx, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(ctx, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
