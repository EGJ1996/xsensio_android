package com.xsensio.nfcsensorcomm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.xsensio.nfcsensorcomm.mainactivity.MainActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainActivityIntent=new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        },1000);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
