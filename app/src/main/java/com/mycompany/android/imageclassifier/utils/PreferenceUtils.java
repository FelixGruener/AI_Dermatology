package com.mycompany.android.imageclassifier.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * Created by delaroy on 3/26/18.
 */

public class PreferenceUtils {

    public PreferenceUtils(){

    }
    public static <T> boolean save(T object, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        prefsEditor.putString(object.getClass().getSimpleName(), json);
        prefsEditor.apply();
        return true;
    }

    public static <T> T read(Class<T> oClass, Context context) {
        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(oClass.getSimpleName(), "");
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, oClass);
    }

    public static boolean saveToken(String token, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.TOKEN, token);
        prefsEditor.apply();
        return true;
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Constants.TOKEN, "");
    }
}
