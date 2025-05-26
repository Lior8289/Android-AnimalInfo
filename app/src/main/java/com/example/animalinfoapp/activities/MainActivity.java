package com.example.animalinfoapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import com.example.animalinfoapp.R;
import com.example.animalinfoapp.utils.LocaleUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.setLocale(newBase, LocaleUtils.getSavedLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onLanguageSwitchClicked(View view) {
        String current = LocaleUtils.getSavedLanguage(this);
        String next = current.equals("en") ? "he" : "en";
        LocaleUtils.saveLanguage(this, next);
        recreate();
    }
}
