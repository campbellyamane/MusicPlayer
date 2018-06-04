package com.campbellyamane.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by campb on 9/5/2017.
 */

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.ExampleViewHolder> {


    private ArrayList<Song> playlist;
    private Context mContext;


    public PlaylistSongAdapter(ArrayList<Song> arrayList, Context c) {
        playlist = arrayList;
        mContext = c;
    }

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView artistTextView;
        TextView songTextView;

        ExampleViewHolder(View itemView) {
            super(itemView);
            imgView = (ImageView) itemView.findViewById(R.id.img);
            artistTextView = (TextView) itemView.findViewById(R.id.artist);
            songTextView = (TextView) itemView.findViewById(R.id.track);
        }
    }


    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new ExampleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(ExampleViewHolder holder, final int position) {
        final Song song = playlist.get(position);

        holder.artistTextView.setText(song.getArtist());
        holder.songTextView.setText(song.getTrack());
        int dp = General.dipToPixels(mContext, 48);
        Picasso.with(mContext).load(song.getArt()).placeholder(R.mipmap.empty_track).resize(dp, dp).centerCrop().into(holder.imgView);
        holder.imgView.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        song.getId());
                ((General)mContext).startTrack(trackUri, playlist, position, true);
                ((General)mContext).openPanel();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((General)mContext).showMenu(playlist, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
