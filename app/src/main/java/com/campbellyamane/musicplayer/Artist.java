package com.campbellyamane.musicplayer;

/**
 * Created by campb on 7/10/2017.
 */

public class Artist {

    private String martist; //Artist name
    private long mid; //Artist Id

    public Artist(String artist, long id){
        martist = artist;
        mid = id;
    }

    //Return artist name
    public String getArtist(){
        return martist;
    }

    //return artist id
    public long getId(){
        return mid;
    }

    //Allow artist sorting without "the" and "a" prefixes
    public String getSort(){
        if (martist.length() > 4) {
            if (martist.substring(0, 4).equals("The ")) {
                return martist.substring(4);
            }
            else if (martist.substring(0,2).equals("A ")){
                return martist.substring(2);
            }
            else{
                return martist;
            }
        }
        else{
            return martist;
        }
    }

}
