package com.csl.macrologandroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.csl.macrologandroid.ui.diary.EditDiaryFragment;

public class EditEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_diary_host);
        if (savedInstanceState == null) {
            final var fragment = new EditDiaryFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_content, fragment)
                    .commit();
        }
    }

    // TODO is this still needed?
//    @Override
//    public void onPause() {
//        super.onPause();
//        Session.resetTimestamp();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (Session.getInstance().isExpired()) {
//            var intent = new Intent(EditEntryActivity.this, StartupActivity.class);
//            intent.putExtra("SESSION_EXPIRED", true);
//            startActivity(intent);
//        }
//    }

}
