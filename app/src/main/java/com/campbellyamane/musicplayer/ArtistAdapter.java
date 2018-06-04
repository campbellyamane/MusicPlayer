package com.campbellyamane.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import static com.campbellyamane.musicplayer.R.id.img;

/**
 * Created by campb on 7/10/2017.
 */

public class ArtistAdapter extends ArrayAdapter<Artist> {

    private ImageView imgView; //Artist image
    private TextView artistView; //Artist name

    public ArtistAdapter(Activity context, ArrayList<Artist> artists) {

        super(context, 0, artists);
    }
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.artist_item, parent, false);
        }

        //Get the current artist in arraylist
        Artist currentArtist = getItem(position);

        //Find the TextView for the artist
        artistView = listItemView.findViewById(R.id.artisto);


        //Set the artist name
        artistView.setText(currentArtist.getArtist());

        //Don't need unless artist image in listview exists
        imgView = listItemView.findViewById(img);
        imgView.setVisibility(View.GONE);

        return listItemView;
    }

}
