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

import static android.R.attr.src;
import static android.graphics.Bitmap.createScaledBitmap;
import static com.campbellyamane.musicplayer.R.id.img;

/**
 * Created by campb on 7/10/2017.
 */

public class SongAdapter extends ArrayAdapter<Song> {
    private ImageView imgView;
    private TextView artistTextView;
    private TextView songTextView;

    public SongAdapter(Activity context, ArrayList<Song> songs) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, songs);
    }
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.song_item, parent, false);
        }

        // Get the {@link AndroidFlavor} object located at this position in the list
        Song currentSong = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID version_name
        songTextView = listItemView.findViewById(R.id.track);
        // Get the version name from the current AndroidFlavor object and
        // set this text on the name TextView
        songTextView.setText(currentSong.getTrack());

        // Find the TextView in the list_item.xml layout with the ID version_number
        artistTextView = listItemView.findViewById(R.id.artist);
        // Get the version number from the current AndroidFlavor object and
        // set this text on the number TextView
        artistTextView.setText(currentSong.getArtist());

        imgView = listItemView.findViewById(img);

        int dp = General.dipToPixels(getContext(), 48);
        Picasso.with(getContext()).load(currentSong.getArt()).placeholder(R.mipmap.empty_track).resize(dp, dp).centerCrop().into(imgView);
        imgView.setVisibility(View.VISIBLE);

        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        return listItemView;
    }

}
