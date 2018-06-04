package com.campbellyamane.musicplayer;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by campb on 7/11/2017.
 */

//Allows albums to be placed in gridview

public class AlbumAdapter extends ArrayAdapter<Album> {

    private ImageView imgView; //View for art

    public AlbumAdapter(Activity context, ArrayList<Album> albums) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, albums);
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.album_item, parent, false);
        }

        //Get the current album in arraylist
        Album currentAlbum = getItem(position);

        //Find the TextView for the album
        TextView albumTextView = (TextView) listItemView.findViewById(R.id.album);

        //Set the album name
        albumTextView.setText(currentAlbum.getAlbum());

        //Find the ImageView for the album cover
        imgView = listItemView.findViewById(R.id.albumcover);

        //Set the album cover
        Picasso.with(getContext()).load(currentAlbum.getArt()).placeholder(R.mipmap.empty_track)
                .fit().centerCrop()
                .into(imgView);
        imgView.setVisibility(View.VISIBLE);


        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        return listItemView;
    }

}
