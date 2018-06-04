package com.campbellyamane.musicplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by campb on 7/11/2017.
 */

public class CategoryAdapter extends FragmentStatePagerAdapter {

    //Declaring main fragment variables
    private Fragment tFrag;
    private Fragment aFrag;
    private Fragment pFrag;

    public CategoryAdapter(FragmentManager fm) {
        super(fm);
    }

    //Set order of fragments
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            if (tFrag == null){
                tFrag = new TracksFragment();
            }
            return tFrag;
        }
        else if (position == 1){
            if (aFrag == null){
                aFrag = new ArtistsFragment();
            }
            return aFrag;
        }
        else{
            if (pFrag == null){
                pFrag = new PlaylistsFragment();
            }
            return pFrag;
        }
    }

    //Set number of fragments
    @Override
    public int getCount() {
        return 3;
    }

    //Set titles of fragments
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Tracks";
        }
        else if (position == 1){
            return "Artists";
        }
        else {
            return "Playlists";
        }
    }
}
