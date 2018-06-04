package com.campbellyamane.musicplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by campb on 7/28/2017.
 */

public class ArtistCategoryAdapter extends FragmentStatePagerAdapter{

    //Declare fragment variables
    private Fragment aFrag;
    private Fragment tFrag;

    public ArtistCategoryAdapter(FragmentManager fm) {
        super(fm);
    }

    //Setting fragments within artist activity
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            if (aFrag == null){
                aFrag = new ArtistAlbumsFragment();
            }
            return aFrag;
        }
        else /*if (position == 1) */ {
            if (tFrag == null){
                tFrag = new ArtistTracksFragment();
            }
            return tFrag;
        }
    }

    //Telling fragmentmanager how many fragments
    @Override
    public int getCount() {
        return 2;
    }

    //Naming the fragments
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Albums";
        } else {
            return "Tracks";
        }
    }

}
