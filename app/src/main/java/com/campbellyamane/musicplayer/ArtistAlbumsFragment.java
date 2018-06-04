package com.campbellyamane.musicplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.campbellyamane.musicplayer.General.bitmap;
import static com.campbellyamane.musicplayer.General.color;
import static com.campbellyamane.musicplayer.General.formerPlaylist;
import static com.campbellyamane.musicplayer.General.newPlaylist;
import static com.campbellyamane.musicplayer.General.nowPlaying;


/**
 * Created by campb on 7/11/2017.
 */

public class ArtistAlbumsFragment extends Fragment{

    private ArrayList<Album> albumList; //List of albums by artist
    private ArrayList<Song> songList; //List of songs by artist

    public ArtistAlbumsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_albums, container, false);


        //Get and sort albums by artist
        albumList = new ArrayList<Album>();
        getAlbumList(((SingleArtist)this.getActivity()).clickedArtist);
        Collections.sort(albumList, new Comparator<Album>(){
            public int compare(Album a, Album b){
                return a.getAlbum().toLowerCase().compareTo(b.getAlbum().toLowerCase());
            }
        });

        //Put albums in gridview
        AlbumAdapter itemsAdapter = new AlbumAdapter(getActivity(), albumList);

        final GridView gridView = (GridView) rootView.findViewById(R.id.albumlist);

        gridView.setAdapter(itemsAdapter);

        //Create popup dialog with album tracks onclick
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {

                final Dialog albumDialog = new Dialog(getActivity());
                albumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                albumDialog.setContentView(R.layout.activity_single_album);

                //Set size of dialog
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                int height = Resources.getSystem().getDisplayMetrics().heightPixels;
                albumDialog.getWindow().setLayout((int)(width*.9),(int)(height*.9));

                //Set album art and name
                ImageView cover = (ImageView) albumDialog.findViewById(R.id.dialogArt);
                TextView album = (TextView) albumDialog.findViewById(R.id.dialogAlbum);
                Album currentAlbum = albumList.get(i);
                cover.setImageURI(albumList.get(i).getArt());
                if(cover.getDrawable() == null){
                    cover.setImageResource(R.mipmap.empty_track);
                }
                album.setText(currentAlbum.getAlbum());

                //Putting album songs in album
                songList = new ArrayList<Song>();

                ((General)(getActivity())).getAlbumTracks(currentAlbum.getAlbum(), songList);

                SongAdapter itemsAdapter = new SongAdapter(getActivity(), songList);

                final ListView listView = (ListView) albumDialog.findViewById(R.id.songlist);
                listView.setAdapter(itemsAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        Uri trackUri = ContentUris.withAppendedId(
                                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                songList.get(i).getId());
                        ((General)getActivity()).startTrack(trackUri, songList, i, true);
                        ((General)getActivity()).openPanel();
                        albumDialog.dismiss();
                    }
                });

                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                        ((General)getActivity()).showMenu(songList, i);
                        return true;
                    }
                });

                //Use album bitmap to style color
                int sure = 0x000000;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),albumList.get(i).getArt());
                    Palette p = Palette.from(bitmap).generate();
                    color = p.getDarkMutedColor(sure);
                } catch (IOException e) {
                    color = sure;
                    e.printStackTrace();
                }
                listView.setBackgroundColor(color);
                albumDialog.show();
            }
        });
        return rootView;
    }

    public void getAlbumList(String artist) { //Method to retreive all albums by artist
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, new String[] { "DISTINCT " + MediaStore.Audio.Artists.Albums.ALBUM + ", " + MediaStore.Audio.Artists.Albums.ALBUM_ID}, MediaStore.Audio.Media.ARTIST + "=?", new String[]{artist}, null);
        String prevAlbum = "fdhfajslkdakjshf";
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.Albums.ALBUM);
            int artColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.Albums.ALBUM_ID);
            do {
                String albumTitle = musicCursor.getString(albumColumn);
                int thisAlbum = musicCursor.getInt(artColumn);
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri thisArt = ContentUris.withAppendedId(sArtworkUri, thisAlbum);
                if (!prevAlbum.equals(albumTitle)) {
                    albumList.add(new Album(albumTitle, thisArt));
                }
                prevAlbum = albumTitle;

            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }
}
