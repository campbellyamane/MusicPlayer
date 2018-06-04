package com.campbellyamane.musicplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cesards.cropimageview.CropImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;



public class SinglePlaylist extends General {

    private String playlistString;
    private long playlistId;
    private ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_single_playlist);
        super.onCreate(savedInstanceState);


        Bundle bundle = getIntent().getExtras();
        playlistString = bundle.getString("Playlist");
        playlistId = bundle.getLong("ID");

        TextView playlistName = (TextView) findViewById(R.id.playlistName);
        playlistName.setText(playlistString);

        //Setting Up ListView
        songList = new ArrayList<Song>();
        getSongList();

        final PlaylistSongAdapter adapter = new PlaylistSongAdapter(songList, SinglePlaylist.this);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tracklist);
        recyclerView.setLayoutManager(new GridLayoutManager(SinglePlaylist.this, 1));

        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if (currentPlaylist != null && currentPlaylist.equals(songList)){
                                    if(currentPlaylist.indexOf(nowPlaying) == position){
                                        musicService.stop();
                                        hidePanel();
                                    }
                                    currentPlaylist.remove(position);
                                    Log.d("Removal","yessir");

                                    if (isShuffled){
                                        removeFromQueue(position);
                                    }
                                    imageAdapter = new ImageAdapter(SinglePlaylist.this);
                                    slideArt.setAdapter(imageAdapter);
                                    if (isShuffled) {
                                        slideArt.setCurrentItem(currentShuffle);
                                    }
                                    else{
                                        slideArt.setCurrentItem((current));
                                    }
                                }
                                songList.remove(position);
                                adapter.notifyDataSetChanged();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                adapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };


                //Remove swiped item from list and notify the RecyclerView
                AlertDialog.Builder builder = new AlertDialog.Builder(SinglePlaylist.this);
                builder.setMessage("Are you sure you want to remove '" + songList.get(position).getTrack() + "' from " + playlistString + "?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


    }

    public void getSongList() {
        ContentResolver musicResolver = this.getContentResolver();
        Uri musicUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.AUDIO_ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.ALBUM);

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

    public void removeFromQueue(int pos){
        int remove = queueCounter.indexOf(pos);
        if (remove < currentShuffle){
            currentShuffle--;
        }
        Log.d("Remover", Integer.toString(queueCounter.size()));
        for (int i = 0; i < queueCounter.size(); i++){
            if (queueCounter.get(i) > pos){
                queueCounter.set(i, queueCounter.get(i)-1);
            }
        }
        queueCounter.remove(remove);
        imageAdapter = new ImageAdapter(SinglePlaylist.this);
        slideArt.setAdapter(imageAdapter);
        if (isShuffled) {
            slideArt.setCurrentItem(currentShuffle);
        }
        else{
            slideArt.setCurrentItem((current));
        }

        musicService.updatePlaylist(currentPlaylist, queueCounter, current, currentShuffle);
    }

}


