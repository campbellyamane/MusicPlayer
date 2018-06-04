package com.campbellyamane.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.campbellyamane.musicplayer.General.formerPlaylist;
import static com.campbellyamane.musicplayer.General.newPlaylist;


/**
 * Created by campb on 7/11/2017.
 */

public class ArtistTracksFragment extends Fragment{

    private ArrayList<Song> songList;


    public ArtistTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_tracks, container, false);

        songList = new ArrayList<Song>();
        TextView artistName = getActivity().findViewById(R.id.artist);
        String artist = artistName.getText().toString();
        getSongList(artist);

        SongAdapter itemsAdapter = new SongAdapter(getActivity(), songList);

        final ListView listView = (ListView) rootView.findViewById(R.id.tracklist);

        listView.setAdapter(itemsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        songList.get(i).getId());
                ((General)getActivity()).startTrack(trackUri, songList, i, true);
                ((General)getActivity()).openPanel();

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                ((General)getActivity()).showMenu(songList, i);
                return true;
            }
        });

        return rootView;
    }

    public void getSongList(String artist) {
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, MediaStore.Audio.Media.ARTIST + "=?", new String[]{artist}, null);
        if(musicCursor!=null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long thisAlbumId = musicCursor.getLong(albumIdColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri thisArt = ContentUris.withAppendedId(sArtworkUri, thisAlbumId);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbumId, thisArt, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }
    }
}
