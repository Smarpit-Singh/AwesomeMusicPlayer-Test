package com.example.android.awesomemusicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.awesomemusicplayer.model.AlbumObj;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class StorageUtil {

    private static final String STORAGE = " com.example.android.awesomemusicplayer.STORAGE";
    private static SharedPreferences preferences;


    public static void storeAudio(ArrayList<TrackObj> arrayList, Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public static ArrayList<TrackObj> loadAudio(Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<TrackObj>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static void storeAudioIndex(String path, Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("path", path);
        editor.apply();
    }

    public static String loadAudioIndex(Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("path",null);
    }

    public static void storeAlbums(ArrayList<AlbumObj> arrayList, Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("albumArrayList", json);
        editor.apply();
    }

    public static ArrayList<AlbumObj> loadAlbums(Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("albumArrayList", null);
        Type type = new TypeToken<ArrayList<AlbumObj>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static void clearCachedAudioPlaylist(Context context) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
