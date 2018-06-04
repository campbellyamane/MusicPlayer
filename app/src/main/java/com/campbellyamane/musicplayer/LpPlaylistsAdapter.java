package com.campbellyamane.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by campb on 9/4/2017.
 */

public class LpPlaylistsAdapter extends ArrayAdapter<Playlist> {

    private TextView playlistTextView;

    public LpPlaylistsAdapter(Activity context, ArrayList<Playlist> playlists) {

        super(context, 0 , playlists);
    }
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.playlist_option, parent, false);
        }

        // Get the {@link AndroidFlavor} object located at this position in the list
        Playlist currentPlaylist = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID version_name
        playlistTextView = listItemView.findViewById(R.id.playlistName);
        // Get the version name from the current AndroidFlavor object and
        // set this text on the name TextView
        playlistTextView.setText(currentPlaylist.getName());

        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        return listItemView;
    }
}
