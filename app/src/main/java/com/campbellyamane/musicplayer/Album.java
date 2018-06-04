package com.campbellyamane.musicplayer;

import android.net.Uri;

import static com.campbellyamane.musicplayer.R.id.artist;
import static com.campbellyamane.musicplayer.R.id.img;

/**
 * Created by campb on 7/10/2017.
 */

public class Album {

    private String malbum; //Album name
    private Uri mart; //Album art path

    public Album(String album, Uri art){
        malbum = album;
        mart = art;
    }

    //Return the name of the album
    public String getAlbum(){
        return malbum;
    }

    //Return the path to the album artwork
    public Uri getArt() { return mart;}


}
