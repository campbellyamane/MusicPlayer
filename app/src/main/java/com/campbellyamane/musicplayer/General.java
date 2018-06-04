package com.campbellyamane.musicplayer;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.Layout;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cesards.cropimageview.CropImageView;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.R.attr.bitmap;
import static android.R.attr.button;
import static android.R.attr.id;
import static android.R.attr.targetActivity;
import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.os.Build.VERSION_CODES.M;
import static android.support.v7.graphics.Palette.from;
import static com.campbellyamane.musicplayer.R.id.artistList;
import static com.campbellyamane.musicplayer.R.id.image;
import static com.campbellyamane.musicplayer.R.id.seekbar;

public class General extends AppCompatActivity implements ServiceCallbacks{

    //Panel
    protected SlidingUpPanelLayout activityPanel;

    //Panel color
    protected static int color;


    protected static Boolean goodToLoad = false; //Has Main activity been set
    protected static Boolean reload = false; //Are there new tracks

    //Panel UI
    protected static ImageView slideNext; //Next button
    protected static ImageView slidePrev; //Previous button
    protected static ImageView slidePlay; //Play/pause button
    protected static TextView slideAlbum; //Album title
    protected static TextView slideArtist; //Artist name
    protected static TextView slideTrack; //Track title
    protected static ImageAdapter imageAdapter; //Cover art holder
    protected static ViewPager slideArt; //Cover art loader
    protected static SeekBar activitySeekBar; //Seekbar
    protected static Button repeat; //Repeat button
    protected static Button shuffle; //Shuffle button
    protected static TextView startTime; //Start time
    protected static TextView endTime; //End time
    protected static LinearLayout colorLayout; //Entire layout, to be colored
    protected static Bitmap bitmap; //Album art image holder

    //Longpress UI
    private TextView lpTrack;
    private TextView lpArtist;
    private TextView lpPlay;
    private TextView lpPlayNext;
    private TextView lpGoArtist;
    private TextView lpGoAlbum;
    private TextView lpVideo;
    private CropImageView albumCover;
    private TextView albumTitle;
    private TextView lpAddToPlaylist;
    private Song selectedTrack;

    //Song Preview UI
    protected static ImageView npControl;
    protected static TextView npArtist;
    protected static TextView npTrack;
    protected static ImageView npCover;

    //Seekbar Background
    protected static Handler mHandler;
    protected static Runnable seekChange;
    protected static Boolean sbRunning = false;

    //Song Info Fader
    protected static LinearLayout previewTitle;
    protected static LinearLayout slideTitle;

    //Playlist Stuff
    protected static ArrayList<Song> currentPlaylist;
    protected static ArrayList<Song> formerPlaylist;
    protected static ArrayList<Integer> queueCounter;
    protected static ArrayList<Song> albumSongList;

    private int defaultArt; //Default image for no art

    //Current Information
    protected static int current;
    protected static int currentShuffle;
    protected static Boolean isShuffled = false;
    protected static Song nowPlaying;
    protected static Boolean newPlaylist = false;
    protected static int imagePosition;
    protected static int prevPosition = -1;

    protected MusicService musicService;
    protected boolean mBound = false;

    //Connects UI to service
    protected ServiceConnection serviceConnector = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            mBound = true;
            musicService.setCallbacks(General.this);

            //If the music player has been initialized, set UI
            if (musicService.exists()){
                if (goodToLoad) {
                    setColor();
                    setPreviewInfo();
                    setPanelInfo(true);
                }
            }
            else{
                if (goodToLoad) {
                    hidePanel();
                }
            }
            musicService.set();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    //If panel is expanded, collapse on backpress
    @Override
    public void onBackPressed() {
        if (activityPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            activityPanel.setPanelHeight(dipToPixels(getApplicationContext(),62f));
            activityPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else{
            super.onBackPressed();
        }

    }

    //Kill service and notification on destroy
    @Override
    protected void onDestroy() {
        if( musicService.exists() && !musicService.isPlaying()) {
            musicService.removeNotification();
        }
        super.onDestroy();
    }

    //Start the service and bind it
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Intent svc = new Intent(this, MusicService.class);
        bindService(svc, serviceConnector, 0);
        startService(svc);

        defaultArt = R.mipmap.empty_track;


        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {

        //Assign variable values for the current Activity
        if(goodToLoad) {
            defineVariables();
        }

        //Setup panel stuff if music exists
        if (musicService != null){
            musicService.set();
            if (musicService.exists()) {
                setColor();
                if (activityPanel.getPanelHeight() == 0){
                    activityPanel.setPanelHeight(dipToPixels(General.this, 62f));
                }
                setPreviewInfo();
                setPanelInfo(true);
                if (musicService.isPlaying() && activityPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                    seekBarRun();
                }
            }
            else{
                hidePanel();
            }
        }
        else{
            Intent svc = new Intent(this, MusicService.class);
            bindService(svc, serviceConnector, 0);
        }


        super.onResume();
    }

    //Define Variables Method
    public void defineVariables(){
        activityPanel = (SlidingUpPanelLayout) findViewById(R.id.slider);
        slideNext = (ImageView) findViewById(R.id.next);
        slidePrev = (ImageView) findViewById(R.id.prev);
        slidePlay = (ImageView) findViewById(R.id.slidePlay);
        slideAlbum = (TextView) findViewById(R.id.slideAlbum);
        slideArtist = (TextView) findViewById(R.id.slideArtist);
        slideTrack = (TextView) findViewById(R.id.slideTrack);
        previewTitle = (LinearLayout) findViewById(R.id.previewTitle);
        slideTitle = (LinearLayout) findViewById(R.id.slideTitle);
        slideArt = (ViewPager) findViewById(R.id.slideArt);
        activitySeekBar = (SeekBar) findViewById(R.id.seekbar);
        repeat = (Button) findViewById(R.id.repeat);
        shuffle = (Button) findViewById(R.id.shuffle);
        if (isShuffled){
            shuffle.setTextColor(getResources().getColor(R.color.blue));
        }
        startTime = (TextView) findViewById(R.id.start_time);
        endTime = (TextView) findViewById(R.id.end_time);
        npControl = (ImageView) findViewById(R.id.home_control);
        npArtist = (TextView) findViewById(R.id.home_artist);
        npTrack = (TextView) findViewById(R.id.home_track);
        npCover = (ImageView) findViewById(R.id.cover);
        colorLayout = (LinearLayout) findViewById(R.id.setColor);
        previewTitle.setSelected(true);
        slideTitle.setSelected(true);
        setListeners();
    }

    //Set Listeners
    public void setListeners(){

        //Call play/pause
        slidePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });

        //Setup panel listener
        activityPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                //Fade in/out topbar information, set time info
                if (musicService.exists()) {
                    setSeekBarTimes();
                    activitySeekBar.setProgress(musicService.getCurrentPosition());
                    activitySeekBar.setMax(musicService.getDuration());
                }

                previewTitle.setAlpha(1 - slideOffset);
                slideTitle.setAlpha(slideOffset);
                if (slideOffset == 0){
                    slideTitle.setVisibility(View.GONE);
                    previewTitle.setVisibility(View.VISIBLE);
                }
                else if (slideOffset == 1){
                    previewTitle.setVisibility(View.GONE);
                    slideTitle.setVisibility(View.VISIBLE);
                }
                else{
                    slideTitle.setVisibility(View.VISIBLE);
                    previewTitle.setVisibility(View.VISIBLE);
                }
            }

            //Start seekbar on expand, stop on collapse, set panel height if music exists
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    seekBarRun();
                }
                else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    seekBarStop();
                }
                if (activityPanel.getPanelHeight() == 0 && musicService.exists()){
                    activityPanel.setPanelHeight(dipToPixels(General.this, 62f));
                }
            }
        });

        slideArt.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Set color of layout, track details on imageload
                imagePosition = position;
                int sure = 0x000000;
                Song currentSong;
                if (isShuffled) {
                    currentSong = currentPlaylist.get(queueCounter.get(imagePosition));
                }
                else {
                    currentSong = currentPlaylist.get(imagePosition);
                }
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentSong.getArt());
                    Palette p = Palette.from(bitmap).generate();
                    color = p.getDarkVibrantColor(sure);
                } catch (IOException e) {
                    color = sure;
                    e.printStackTrace();
                }
                int colorFrom = ((ColorDrawable) colorLayout.getBackground()).getColor();
                int colorTo = color;
                Log.d("Colorer", Integer.toString(color));
                int duration = 500;
                ObjectAnimator anim = ObjectAnimator.ofObject(colorLayout, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo);

                anim.setDuration(duration).start();
                slideTrack.setText(currentSong.getTrack());
                slideTrack.setSelected(false);
                slideArtist.setText(currentSong.getArtist());
                slideAlbum.setText(currentSong.getAlbum());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Wait until image stops moving to play next/prev song
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (isShuffled) {
                        if (imagePosition > currentShuffle) {
                            musicService.next();
                        } else if (imagePosition < currentShuffle) {
                            musicService.prev();
                            Log.d("queuer", "from shuffle prev");
                        }
                    } else {
                        if (imagePosition > current) {
                            musicService.next();
                        } else if (imagePosition < current) {
                            musicService.prev();
                            Log.d("queuer", "from reg prev");
                        }
                    }
                }
                slideTrack.setSelected(true);
            }
        });

        //Allow seekbar drag to change music time
        activitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar thisBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && musicService.exists() && fromUser) {
                    musicService.seekTo(progress);
                    setSeekBarTimes();
                }
            }
        });

        //Start artist activity on topbar press
        slideTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBarStop();
                Intent intent = new Intent(getApplicationContext(), SingleArtist.class);
                Bundle bundle = new Bundle();
                bundle.putString("Artist", nowPlaying.getArtist());
                bundle.putLong("ID", nowPlaying.getId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //Album click starts album dialog
        slideAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog albumDialog = new Dialog(General.this);
                albumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                albumDialog.setContentView(R.layout.activity_single_album);
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                int height = Resources.getSystem().getDisplayMetrics().heightPixels;
                albumDialog.getWindow().setLayout((int)(width*.9),(int)(height*.9));
                ImageView cover = (ImageView) albumDialog.findViewById(R.id.dialogArt);
                TextView album = (TextView) albumDialog.findViewById(R.id.dialogAlbum);
                cover.setImageURI(nowPlaying.getArt());
                if(cover.getDrawable() == null){
                    cover.setImageResource(R.mipmap.empty_track);
                }

                album.setText(nowPlaying.getAlbum());

                albumSongList = new ArrayList<Song>();

                getAlbumTracks(nowPlaying.getAlbum(), albumSongList);

                SongAdapter itemsAdapter = new SongAdapter(General.this, albumSongList);

                final ListView listView = (ListView) albumDialog.findViewById(R.id.songlist);
                listView.setAdapter(itemsAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        nowPlaying = albumSongList.get(i);
                        Uri trackUri = ContentUris.withAppendedId(
                                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                nowPlaying.getId());
                        startTrack(trackUri, albumSongList, i, true);
                        openPanel();
                        albumDialog.dismiss();
                    }
                });

                listView.setBackgroundColor(color);
                albumDialog.show();
            }
        });

        slideNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.next();
            }
        });

        slidePrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.prev();
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffle();
            }
        });

    }

    //Opens panel
    public void openPanel(){

        //Expand panel
        activityPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

    }

    //Change panel color
    public void setColor(){
        int sure = 0x000000;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),nowPlaying.getArt());
            Palette p = Palette.from(bitmap).generate();
            color = p.getDarkVibrantColor(sure);
        } catch (IOException e) {
            color = sure;
            e.printStackTrace();
        }
        colorLayout.setBackgroundColor(color);
    }

    public void hidePanel(){
        activityPanel.setPanelHeight(0);
    }

    //Setup panel info
    public void setPanelInfo(Boolean newPlaylist){

        if (!slideTrack.getText().equals(nowPlaying.getTrack())) {
            setColor();
            slideTrack.setText(nowPlaying.getTrack());
            slideArtist.setText(nowPlaying.getArtist());
            slideAlbum.setText(nowPlaying.getAlbum());
        }
        if (musicService.isPlaying()) {
            slidePlay.setImageResource(R.mipmap.pause);
        } else {
            slidePlay.setImageResource(R.mipmap.play);
        }
        imageAdapter = new ImageAdapter(General.this);
        slideArt.setAdapter(imageAdapter);
        if (isShuffled) {
            slideArt.setCurrentItem(currentShuffle);
        }
        else{
            slideArt.setCurrentItem((current));
        }
    }

    //Set preview details (preview occupies the same space as panel topbar)
    public void setPreviewInfo(){
        npTrack.setText(nowPlaying.getTrack());
        npArtist.setText(nowPlaying.getArtist());
        if (musicService.isPlaying()){
            npControl.setImageResource(R.mipmap.pause);
        }
        else{
            npControl.setImageResource(R.mipmap.play);
        }

        npControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });

        npCover.setImageURI(nowPlaying.getArt());
        if(npCover.getDrawable() == null){
            npCover.setImageResource(defaultArt);
        }
        else{
            npCover.setImageURI(nowPlaying.getArt());
        }

    }

    //Music Control Methods

    //Start track, calls service start
    public void startTrack(Uri song, ArrayList<Song> list, int i, Boolean newPlaylist){
        currentPlaylist = list;
        musicService.start(song, list, i, newPlaylist);
    }

    //Callbacks to set UI info based on service actions

    @Override
    public void cbSetPlayingInfoReg(Song currentSong, int pos, ArrayList<Song> p, Boolean newPlaylist, Boolean completed) {
        activitySeekBar.setMax(musicService.getDuration());
        nowPlaying = currentSong;
        currentPlaylist = (ArrayList<Song>) p.clone();
        current = pos;
        setPanelInfo(newPlaylist);
        setPreviewInfo();
    }

    @Override
    public void cbSetPlayingInfoShuff(Song currentSong, int pos, ArrayList<Song> p, int cs, Boolean newPlaylist, Boolean completed) {
        activitySeekBar.setMax(musicService.getDuration());
        nowPlaying = currentSong;
        Log.d("positioner", nowPlaying.getTrack());
        currentPlaylist = (ArrayList<Song>) p.clone();
        current = pos;
        currentShuffle = cs;
        setPanelInfo(newPlaylist);
        setPreviewInfo();
    }

    @Override
    public void cbSetQueue(ArrayList<Integer> q) {
        queueCounter = (ArrayList<Integer>) q.clone();
        currentShuffle = 0;
    }

    public void shuffle(){
        if (isShuffled){
            isShuffled = false;
            shuffle.setTextColor(getResources().getColor(R.color.white));
            musicService.setShuffle(false);
        }
        else{
            int count = currentPlaylist.size();
            isShuffled = true;
            musicService.setShuffle(true);
            musicService.setShuffleOrder(count, true);
            Log.d("queuer", "from shuffle button");
            shuffle.setTextColor(getResources().getColor(R.color.blue));
        }
        imageAdapter = new ImageAdapter(General.this);
        slideArt.setAdapter(imageAdapter);
        if (isShuffled) {
            slideArt.setCurrentItem(currentShuffle);
        }
        else{
            slideArt.setCurrentItem((current));
        }
    }

    public void playPause(){
        if (musicService.exists()) {
            if (musicService.isPlaying()) {
                setPlayButton(false);
                seekBarStop();
            }
            else{
                if (activityPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                    seekBarRun();
                }
                setPlayButton(true);
            }
            musicService.play();
        }
    }

    public void setPlayButton(Boolean b){
        if (b){
            slidePlay.setImageResource(R.mipmap.pause);
            npControl.setImageResource(R.mipmap.pause);
        }
        else{
            slidePlay.setImageResource(R.mipmap.play);
            npControl.setImageResource(R.mipmap.play);
        }

    }

    public void seekBarRun(){
        if (!sbRunning) {
            sbRunning = true;
            //Make sure you update Seekbar on UI thread
            mHandler = new Handler();
            runOnUiThread(seekChange = new Runnable() {
                @Override
                public void run() {
                    setSeekBarTimes();
                    activitySeekBar.setProgress(musicService.getCurrentPosition());
                    mHandler.postDelayed(this, 15);
                }

            });
        }
    }

    public void seekBarStop(){
        sbRunning = false;
        if (mHandler != null){
            mHandler.removeCallbacks(seekChange);
            mHandler = null;
            seekChange = null;
        }
    }

    public void setSeekBarTimes(){
        Date date = new Date(musicService.getCurrentPosition());
        SimpleDateFormat formatter = new SimpleDateFormat("m:ss");
        startTime.setText(formatter.format(date));
        date = new Date(musicService.getDuration());
        endTime.setText(formatter.format(date));
    }


    public void showMenu(final ArrayList<Song> songList,final int i){

        selectedTrack = songList.get(i);
        final Dialog lpMenu = new Dialog(this);
        lpMenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lpMenu.setContentView(R.layout.activity_longpress_track);
        final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        final int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        lpMenu.getWindow().setLayout((int)(width*.9),(int)(height*.9));

        setLpVariables(lpMenu);

        lpTrack.setText(selectedTrack.getTrack());
        lpArtist.setText(selectedTrack.getArtist());

        lpPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        selectedTrack.getId());
                startTrack(trackUri, songList, i, true);
                openPanel();
                lpMenu.dismiss();
            }
        });

        lpPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.playNext(selectedTrack);
                if (musicService.exists()){
                    Toast.makeText(General.this, selectedTrack.getTrack() + " has been added to the current queue", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(General.this, "You can't play next if nothing is playing", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lpGoArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SingleArtist.class);

                Bundle bundle = new Bundle();
                bundle.putString("Artist", selectedTrack.getArtist());
                bundle.putLong("ID", selectedTrack.getId());
                intent.putExtras(bundle);
                startActivity(intent);
                lpMenu.dismiss();
            }
        });

        lpGoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog albumDialog = new Dialog(General.this);
                albumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                albumDialog.setContentView(R.layout.activity_single_album);
                albumDialog.getWindow().setLayout((int)(width*.9),(int)(height*.9));

                albumCover = (CropImageView) albumDialog.findViewById(R.id.dialogArt);
                albumTitle = (TextView) albumDialog.findViewById(R.id.dialogAlbum);

                albumCover.setImageURI(selectedTrack.getArt());
                if(albumCover.getDrawable() == null){
                    albumCover.setImageResource(R.mipmap.empty_track);
                }

                albumTitle.setText(selectedTrack.getAlbum());

                albumSongList = new ArrayList<Song>();

                getAlbumTracks(selectedTrack.getAlbum(), albumSongList);

                SongAdapter itemsAdapter = new SongAdapter(General.this, albumSongList);

                final ListView listView = (ListView) albumDialog.findViewById(R.id.songlist);
                listView.setAdapter(itemsAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        nowPlaying = albumSongList.get(i);
                        Uri trackUri = ContentUris.withAppendedId(
                                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                nowPlaying.getId());
                        startTrack(trackUri, albumSongList, i, true);
                        openPanel();
                        albumDialog.dismiss();
                        lpMenu.dismiss();
                    }
                });

                int color;
                int sure = 0x000000;
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedTrack.getArt());
                    Palette p = Palette.from(bitmap).generate();
                    color = p.getDarkVibrantColor(sure);
                } catch (IOException e) {
                    color = sure;
                    e.printStackTrace();
                }

                listView.setBackgroundColor(color);
                albumDialog.show();

            }
        });

        lpAddToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog playlistDialog = new Dialog(General.this);
                playlistDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                playlistDialog.setContentView(R.layout.activity_add_to_playlist);
                playlistDialog.getWindow().setLayout((int)(width*.9),(int)(height*.9));
                StorageUtils storage = new StorageUtils(getApplicationContext());
                final ArrayList<Playlist> playlists = storage.loadPlaylists();
                LpPlaylistsAdapter itemsAdapter = new LpPlaylistsAdapter(General.this, playlists);

                final ListView listView = (ListView) playlistDialog.findViewById(R.id.playlist_list);
                listView.setAdapter(itemsAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                        addToPlaylist(playlists.get(i).getId(), selectedTrack.getId(), playlists.get(i).getName(), selectedTrack.getTrack());
                    }
                });
                playlistDialog.show();
                lpMenu.dismiss();
            }
        });
        lpVideo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new ytSearch().execute(selectedTrack.getTrack() + " " + selectedTrack.getArtist() + " music video");
                lpMenu.dismiss();
            }

        });
        lpMenu.show();
    }


    public void setLpVariables(Dialog menu){
        lpTrack = (TextView) menu.findViewById(R.id.lpSong);
        lpArtist = (TextView) menu.findViewById(R.id.lpArtist);
        lpPlay = (TextView) menu.findViewById(R.id.play);
        lpPlayNext = (TextView) menu.findViewById(R.id.playNext);
        lpGoArtist = (TextView) menu.findViewById(R.id.go_to_artist);
        lpGoAlbum = (TextView) menu.findViewById(R.id.go_to_album);
        lpAddToPlaylist = (TextView) menu.findViewById(R.id.add_to_playlist);
        lpVideo = (TextView) menu.findViewById(R.id.watch_video);
    }

    public Boolean addToPlaylist(long playListId, long songId, String plName, String trackName){
        ContentResolver musicResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playListId);
        String[] projection = new String[] { MediaStore.Audio.Playlists.Members.PLAY_ORDER, MediaStore.Audio.Playlists.Members.AUDIO_ID };
        Cursor cursor = musicResolver.query(uri, projection, null, null, null);
        int base = 0;
        if (cursor.moveToLast())
            base = cursor.getInt(0) + 1;

        if(cursor!=null && cursor.moveToFirst()){
            int idColumn = cursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.AUDIO_ID);

            do {
                long thisId = cursor.getLong(idColumn);
                if (songId == thisId){
                    Toast.makeText(General.this, trackName + " is a duplicate track and cannot be added", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        ContentValues[] values = new ContentValues[1];
        ContentValues value = new ContentValues(2);
        value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base));
        value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
        values[0] = value;
        musicResolver.bulkInsert(uri, values);
        Toast.makeText(General.this, trackName + " has been added to " + plName, Toast.LENGTH_SHORT).show();
        return true;
    }

    public void getAlbumTracks(String album, ArrayList<Song> albumSongList) {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, MediaStore.Audio.Media.ALBUM + "=?", new String[]{album}, null);
        if(musicCursor!=null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long thisAlbumId = musicCursor.getLong(albumIdColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri thisArt = ContentUris.withAppendedId(sArtworkUri, thisAlbumId);
                albumSongList.add(new Song(thisId, thisTitle, thisArtist, thisAlbumId, thisArt, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
    }

    public class ytSearch extends AsyncTask<String, String, String> {
        String url1 = "https://www.googleapis.com/youtube/v3/search?q=";
        String url2 = "&maxResults=1&part=snippet&key=AIzaSyB3xpWnx74bTykKitfsOH3PjCwhEAkdDEg";
        String id = "";

        @Override
        protected String doInBackground(String... query) {
            URL url = null;
            String search = query[0].replace("&", "");
            try {
                url = new URL(url1 + search + url2);
                InputStream in = url.openStream();
                InputStreamReader reader = new InputStreamReader(in);
                JSONObject json = getObject(reader);
                id = json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return id;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //Update the progress of current task
        }

        @Override
        protected void onPostExecute(String url) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + url)));
        }

        public JSONObject getObject(InputStreamReader in) throws IOException, JSONException {
            BufferedReader bR = new BufferedReader(in);
            String line = "";

            StringBuilder responseStrBuilder = new StringBuilder();
            while((line =  bR.readLine()) != null){

                responseStrBuilder.append(line);
            }

            JSONObject result= new JSONObject(responseStrBuilder.toString());
            return result;
        }
    }

    public static int dipToPixels(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void cbSetPlay(Boolean play) {
        setPlayButton(play);
    }
}
