package com.campbellyamane.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.type;

/**
 * Created by campb on 9/4/2017.
 */

public class StorageUtils {

    private final String STORAGE = " com.campbellyamane.musicplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtils(Context context) {
        this.context = context;
    }

    public void storeSongs(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        editor.putString("songDatabase",gson.toJson(arrayList));
        editor.apply();
    }

    public void storeArtists(ArrayList<Artist> arrayList){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        editor.putString("artistDatabase",gson.toJson(arrayList));
        editor.apply();
    }

    public void storePlaylists(ArrayList<Playlist> arrayList){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        editor.putString("playlistDatabase",gson.toJson(arrayList));
        editor.apply();
    }

    public ArrayList<Song> loadSongs() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        String json  = preferences.getString("songDatabase", null);
        return gson.fromJson(json, new TypeToken<ArrayList<Song>>(){}.getType());
    }

    public ArrayList<Artist> loadArtists() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        String json  = preferences.getString("artistDatabase", null);
        return gson.fromJson(json, new TypeToken<ArrayList<Artist>>(){}.getType());
    }

    public ArrayList<Playlist> loadPlaylists() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        String json  = preferences.getString("playlistDatabase", null);
        return gson.fromJson(json, new TypeToken<ArrayList<Playlist>>(){}.getType());
    }

    public void storeNewest(long date){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(date);
        editor.putString("Newest", json);
        editor.apply();
    }

    public long loadNewest(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json;
        try {
            json = preferences.getString("Newest", null);
            Type type = new TypeToken<Long>() {
            }.getType();
            return gson.fromJson(json, type);
        }
        catch (Exception e){
            return 0;
        }
    }

    public class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                               final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }

}