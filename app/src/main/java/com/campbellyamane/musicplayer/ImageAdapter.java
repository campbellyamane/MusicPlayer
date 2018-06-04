package com.campbellyamane.musicplayer;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import static com.campbellyamane.musicplayer.General.current;
import static com.campbellyamane.musicplayer.General.currentPlaylist;
import static com.campbellyamane.musicplayer.General.currentShuffle;
import static com.campbellyamane.musicplayer.General.isShuffled;
import static com.campbellyamane.musicplayer.General.nowPlaying;
import static com.campbellyamane.musicplayer.General.queueCounter;

/**
 * Created by campb on 9/5/2017.
 */


public class ImageAdapter extends PagerAdapter {
    Context context;
    private ArrayList<Uri> GalImages = new ArrayList<Uri>();

    ImageAdapter(Context context){
        this.context=context;
        if (isShuffled){
            for (int i = 0; i < currentPlaylist.size(); i++) {
                GalImages.add(currentPlaylist.get(queueCounter.get(i)).getArt());

            }
            Log.d("AdapterTime", "shuffle");
        }
        else {
            for (int i = 0; i < currentPlaylist.size(); i++) {
                GalImages.add(currentPlaylist.get(i).getArt());
            }
            Log.d("AdapterTime", "reg");
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        return GalImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageURI(GalImages.get(position));
        if(imageView.getDrawable() == null){
            imageView.setImageResource(R.mipmap.empty_track);
        }

        ((ViewPager) container).addView(imageView, 0);
        return imageView;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }
}
