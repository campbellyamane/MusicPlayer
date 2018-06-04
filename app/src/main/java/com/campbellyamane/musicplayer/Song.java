package com.campbellyamane.musicplayer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import static android.R.attr.id;
import static com.campbellyamane.musicplayer.R.id.img;
import static com.campbellyamane.musicplayer.R.id.media_actions;

/**
 * Created by campb on 7/10/2017.
 */

public class Song {

    private String mtrack;
    private String martist;
    private long mId;
    private long malbumId;
    private String malbum;
    private String msort;
    private Uri mArt = null;

    public Song (long id, String track, String artist, long albumid, Uri art){
        mId = id;
        mtrack = track;
        martist = artist;
        malbumId = albumid;
        mArt = art;
    }

    public Song (long id, String track, String artist, long albumid, Uri art, String album){
        mId = id;
        mtrack = track;
        martist = artist;
        malbumId = albumid;
        mArt = art;
        malbum = album;
    }


    public String getTrack(){
        return mtrack;
    }

    public String getArtist(){
        return martist;
    }

    public long getId(){
        return mId;
    }

    public String getAlbum(){
        return malbum;
    }

    public long getAlbumId(){
        return malbumId;
    }

    public String getSort(){
        if (mtrack.length() > 4) {
            if (mtrack.substring(0, 4).equals("The ")) {
                return mtrack.substring(4);
            }
            else if (mtrack.substring(0,2).equals("A ")){
                return mtrack.substring(2);
            }
            else{
                return mtrack;
            }
        }
        else{
            return mtrack;
        }
    }

    public Uri getArt() { return mArt; }
    public boolean hasCover(){
        if (mArt != null){
            return true;
        }
        else{
            return false;
        }
    }


}
