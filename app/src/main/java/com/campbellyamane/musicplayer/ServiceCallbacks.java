package com.campbellyamane.musicplayer;

import java.util.ArrayList;

/**
 * Created by campb on 9/2/2017.
 */

public interface ServiceCallbacks {

    void cbSetPlay(Boolean play);
    void cbSetPlayingInfoReg(Song currentSong, int pos, ArrayList<Song> p, Boolean n, Boolean c);
    void cbSetPlayingInfoShuff(Song currentSong, int pos, ArrayList<Song> p, int cs, Boolean n, Boolean c);
    void cbSetQueue(ArrayList<Integer> q);

}
