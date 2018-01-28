package com.doneu.backgammoncounter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.doneu.backgammoncounter.utils.Constants;


public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "onCreate: SplashActivity //" + super.theme);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

}
