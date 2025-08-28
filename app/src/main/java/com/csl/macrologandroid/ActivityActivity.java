package com.csl.macrologandroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.csl.macrologandroid.ui.diary.EditActivityFragment;

public class ActivityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_fragment_host);
        if (savedInstanceState == null) {
            final var fragment = new EditActivityFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_content, fragment)
                    .commit();
        }
    }

}
