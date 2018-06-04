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
import static com.campbellyamane.musicplayer.MainActivity.artistDb;

/**
 * Created by campb on 7/11/2017.
 */

public class ArtistsFragment extends Fragment{

    private ArrayList<Artist> artistList;

    //Setting last.fm url for artist image retrieval
    private String url1 = "http://ws.audioscrobbler.com/2.0/?method=artist.search&artist=";
    private String url2 = "&api_key=24a265468d98ddad28dbd4d33d875b32&format=json";

    public ArtistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_artists, container, false);

        //If new songs have not been added (set in Main Activity), get artistlist from storage, otherwise regenerate
        if (artistList == null) {
            artistList = new ArrayList<Artist>();
            StorageUtils storage = new StorageUtils(getContext());
            if (reload){
                getArtistList();
                Collections.sort(artistList, new Comparator<Artist>(){
                    public int compare(Artist a, Artist b){
                        return a.getSort().toLowerCase().compareTo(b.getSort().toLowerCase());
                    }
                });
                storage.storeArtists(artistList);
            }
            else{
                artistList = storage.loadArtists();
            }
            artistDb = (ArrayList<Artist>) artistList.clone();
        }

        //Putting artistlist in listview
        ArtistAdapter itemsAdapter = new ArtistAdapter(getActivity(), artistList);

        final ListView listView = (ListView) rootView.findViewById(R.id.artistList);

        listView.setAdapter(itemsAdapter);

        //Open artist activity onclick, send info about artist
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Intent intent = new Intent(getContext(), SingleArtist.class);

                Bundle bundle = new Bundle();
                bundle.putString("Artist", artistList.get(i).getArtist());
                bundle.putLong("ID", artistList.get(i).getId());
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });

        return rootView;
    }

    public void getArtistList(){ //Method to retrieve list of artists
        ArrayList<String> list = new ArrayList<String>();
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,  new String[] { "DISTINCT " + MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists._ID} , null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);
            int artistIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);

            do {
                String thisArtist = musicCursor.getString(artistColumn);
                long thisArtistId = musicCursor.getLong(artistIdColumn);
                artistList.add(new Artist(thisArtist, thisArtistId));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }
}
