package com.campbellyamane.musicplayer;

/**
 * Created by campb on 8/10/2017.
 */

public class Playlist {

    private String mname;
    private long mid;

    public Playlist(String name, long id){
        mname = name;
        mid = id;
    }

    public String getName(){
        return mname;
    }
    public long getId(){ return mid; }
}

