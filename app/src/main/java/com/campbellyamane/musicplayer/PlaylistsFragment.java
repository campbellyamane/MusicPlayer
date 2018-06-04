package com.campbellyamane.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.campbellyamane.musicplayer.General.reload;
import static com.campbellyamane.musicplayer.R.id.artistList;

/**
 * Created by campb on 7/11/2017.
 */

public class PlaylistsFragment extends Fragment{

    private ArrayList<Playlist> playlists;

    public PlaylistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activivty_playlists, container, false);

        playlists = new ArrayList<Playlist>();
        getPlaylists();
        Collections.sort(playlists, new Comparator<Playlist>(){
            public int compare(Playlist a, Playlist b){
                return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
            }
        });

        StorageUtils storage = new StorageUtils(getContext());
        storage.storePlaylists(playlists);

        PlaylistsAdapter itemsAdapter = new PlaylistsAdapter(getActivity(), playlists);
        final ListView listView = (ListView) rootView.findViewById(R.id.playlistList);

        listView.setAdapter(itemsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Intent intent = new Intent(getContext(), SinglePlaylist.class);

                Bundle bundle = new Bundle();
                bundle.putString("Playlist", playlists.get(i).getName());
                bundle.putLong("ID", playlists.get(i).getId());
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });

        return rootView;
    }

    public void getPlaylists() {
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.NAME);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists._ID);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisName = musicCursor.getString(titleColumn);
                if (!thisName.equals("BlackPlayer Favorites")) {
                    playlists.add(new Playlist(thisName, thisId));
                }
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

}
