package com.csl.macrologandroid.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesRepository {

    private static final String TOKEN = "TOKEN";
    private static final String AUTH = "AUTH";
    private static final String PREF_PORTION = "PREF_PORTION";
    private final SharedPreferences authPreferences;
    private final SharedPreferences portionPreferences;

    public PreferencesRepository(final Context context) {
        authPreferences = context.getSharedPreferences(AUTH, Context.MODE_PRIVATE);
        portionPreferences = context.getSharedPreferences(PREF_PORTION, Context.MODE_PRIVATE);
    }

    public String getToken() {
        return authPreferences.getString(TOKEN, null);
    }

    public void setToken(final String token) {
        authPreferences.edit().putString(TOKEN, token).apply();
    }
}
