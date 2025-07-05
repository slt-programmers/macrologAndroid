package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.csl.macrologandroid.cache.ActivityCache;
import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.databinding.ActivityMainBinding;
import com.csl.macrologandroid.services.SyncService;
import com.csl.macrologandroid.ui.diary.DiaryFragment;
import com.csl.macrologandroid.fragments.DishFragment;
import com.csl.macrologandroid.ui.food.FoodFragment;
import com.csl.macrologandroid.fragments.UserFragment;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.notifications.NotificationSender;
import com.csl.macrologandroid.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity implements UserFragment.OnLogoutPressedListener {

    private BottomNavigationView navigation;

    private ActivityMainBinding binding;
    private SyncService syncService;

    private final ActivityResultLauncher<Intent> loginRegisterForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            syncService.syncNetworkWithLocalData().observe(this, synced -> {
                                if (synced) navigation.setSelectedItemId(R.id.navigation_diary);
                            });
                        }
                    });

    private final NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        final var itemId = item.getItemId();
        if (R.id.navigation_diary == itemId) {
            setFragment(new DiaryFragment());
            return true;
        } else if (R.id.navigation_food == itemId) {
            setFragment(new FoodFragment());
            return true;
        } else if (R.id.navigation_dish == itemId) {
            setFragment(new DishFragment());
            return true;
        } else if (R.id.navigation_user == itemId) {
            UserFragment userFragment = new UserFragment();
            userFragment.setOnLogoutPressedListener(this::logout);
            setFragment(userFragment);
            return true;
        } else {
            setFragment(new DiaryFragment());
            return false;
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        syncService = new SyncService(getApplication());
        setContentView(R.layout.activity_main);
        NotificationSender.initNotificationSending(getApplicationContext());
        navigation = findViewById(R.id.navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        if (!isLoggedIn()) {
            loginRegisterForResult.launch(new Intent(this, LoginActivity.class));
        } else {
            syncService.syncNetworkWithLocalData().observe(this, synced -> {
                if (synced) setFragment(new DiaryFragment());
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(MainActivity.this, StartupActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    private void logout() {
        getSharedPreferences("AUTH", MODE_PRIVATE).edit().remove("TOKEN").remove("USER").apply();
        UserSettingsCache.getInstance().clearCache();
        FoodCache.getInstance().clearCache();
        DiaryLogCache.getInstance().clearCache();
        ActivityCache.getInstance().clearCache();
        loginRegisterForResult.launch(new Intent(this, LoginActivity.class));
        navigation.callOnClick();
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment);
        ft.commit();
    }

    private boolean isLoggedIn() {
        boolean tokenExpired = getIntent().getBooleanExtra("TOKEN_EXPIRED", false);
        String token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);
        return token != null && !tokenExpired;
    }

    @Override
    public void onLogoutPressed() {
        logout();
    }

}
