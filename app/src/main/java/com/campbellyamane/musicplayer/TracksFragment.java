package com.campbellyamane.musicplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.widget.TextView;
import android.widget.Toast;

import com.cesards.cropimageview.CropImageView;

import static android.R.attr.data;
import static com.campbellyamane.musicplayer.General.colorLayout;
import static com.campbellyamane.musicplayer.General.formerPlaylist;
import static com.campbellyamane.musicplayer.General.newPlaylist;
import static com.campbellyamane.musicplayer.General.nowPlaying;
import static com.campbellyamane.musicplayer.General.reload;
import static com.campbellyamane.musicplayer.MainActivity.songDb;
import static com.campbellyamane.musicplayer.R.id.album;
import static com.campbellyamane.musicplayer.R.id.artistList;
import static com.campbellyamane.musicplayer.R.id.cover;


public class TracksFragment extends Fragment {

    private ArrayList<Song> songList;

    public TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_tracks, container, false);

        songList = new ArrayList<Song>();
        getSongList();
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getSort().toLowerCase().compareTo(b.getSort().toLowerCase());
            }
        });
        songDb = (ArrayList<Song>) songList.clone();
        /*if (songList == null) {
            songList = new ArrayList<Song>();
            StorageUtils storage = new StorageUtils(getContext());
            if (reload){
                getSongList();
                Collections.sort(songList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getSort().toLowerCase().compareTo(b.getSort().toLowerCase());
                    }
                });
                storage.storeSongs(songList);
            }
            else{
                songList = storage.loadSongs();
            }
            songDb = (ArrayList<Song>) songList.clone();
        }*/

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

    public void getSongList() {
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
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
        musicCursor.close();
    }
}
