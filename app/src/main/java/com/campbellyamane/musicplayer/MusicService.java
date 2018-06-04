package com.campbellyamane.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static android.R.attr.bitmap;
import static android.R.attr.rating;
import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.app.NotificationManager.IMPORTANCE_MAX;
import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
import static com.campbellyamane.musicplayer.General.currentPlaylist;
import static com.campbellyamane.musicplayer.General.queueCounter;
import static com.campbellyamane.musicplayer.R.id.play;
import static com.campbellyamane.musicplayer.R.id.track;
import static com.campbellyamane.musicplayer.R.layout.notification;

/**
 * Created by campb on 9/1/2017.
 */

public class MusicService extends Service{

    //Song and Playlist info
    private int position;
    private int shufflePosition;
    private ArrayList<Song> playlist;
    private ArrayList<Integer> qCounter;
    private Song np;

    private AudioManager am = null;
    private MediaPlayer mp = null;
    private final IBinder mBinder = new LocalBinder();
    private Boolean shouldResume;
    private ServiceCallbacks serviceCallbacks;
    private Boolean shuffled = false;

    //Notification Constants
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREV = "action_previous";


    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaControllerCompat.TransportControls mControls;

    private Boolean fromNotification = false;

    //Intents
    private Intent playAction;
    private Intent nextAction;
    private Intent prevAction;
    private Intent launch;
    private PendingIntent pendingPlay;
    private PendingIntent pendingNext;
    private PendingIntent pendingPrev;
    private PendingIntent launchActivity;

    private Boolean isBound = false;
    private Boolean completed = false;

    private Notification notification;
    private AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AUDIOFOCUS_LOSS) {
                        if (mp.isPlaying()){
                            shouldResume = true;
                        }
                        mp.pause();
                        serviceCallbacks.cbSetPlay(false);
                    }
                    else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
                        mp.pause();
                        serviceCallbacks.cbSetPlay(false);
                    }
                    else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        mp.setVolume(0.5f,0.5f);
                    }
                    else if (focusChange == AUDIOFOCUS_GAIN) {
                        if (shouldResume == true){
                            mp.setVolume(1.0f,1.0f);
                            mp.start();
                            serviceCallbacks.cbSetPlay(true);
                        }
                    }
                    showNotification();
                }
            };

    private boolean requestAudioFocus() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                am.abandonAudioFocus(afChangeListener);
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }


    public void set(){
        isBound = true;
    }

    @Override
    public void onCreate() {
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        defineIntents();
        mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSession = new MediaSessionCompat(getApplicationContext(), "Player");
        mControls = mSession.getController().getTransportControls();
        mSession.setActive(true);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionControls();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIncomingActions(intent);
        Log.d("Incoming","handled");
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public Boolean exists(){
        if (mp == null){
            return false;
        }
        else{
            return true;
        }
    }

    public void setShuffle(Boolean shuff){
        shuffled = shuff;
        if (shuffled) {
            position = playlist.indexOf(np);
        }
        else{
            shufflePosition = 0;
        }
        showNotification();
    }

    public void start(Uri track, ArrayList<Song> currentPlaylist, int i, Boolean newPlaylist){
        //Set Current Variables
        position = i;

        if (playlist != currentPlaylist){
            playlist = currentPlaylist;
        }

        np = currentPlaylist.get(i);


        //Create current queue of music
        if (newPlaylist && shuffled){
            int count = playlist.size();
            setShuffleOrder(count, true);
            Log.d("queuer", "from service start");
            shufflePosition = 0;
            Log.d("queuer", "isnew");
        }

        if (mp != null){
            mp.stop();
            mp.release();
        }

        if (requestAudioFocus()) {
            mp = MediaPlayer.create(MusicService.this, track);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        completed = true;
                        next();
                    }
                });
            }
        }
        if(shuffled){
            if (completed) {
                serviceCallbacks.cbSetPlayingInfoShuff(np, position, playlist, shufflePosition, newPlaylist, true);
            }
            else{
                serviceCallbacks.cbSetPlayingInfoShuff(np, position, playlist, shufflePosition, newPlaylist, false);
            }
        }
        else {
            if (completed) {
                serviceCallbacks.cbSetPlayingInfoReg(np, position, playlist, newPlaylist, true);
            }
            else{
                serviceCallbacks.cbSetPlayingInfoReg(np, position, playlist, newPlaylist, false);
            }
        }
        completed = false;
        showNotification();
    }

    public void stop(){
        mp.stop();
        mp.release();
        mp = null;
        removeNotification();
    }

    public void next(){
        if (shuffled) {
            if (shufflePosition == playlist.size() - 1) {
                int count = playlist.size();
                setShuffleOrder(count, false);
                Log.d("queuer", "from service next");
                Log.d("queuer", "isend");
                shufflePosition = 0;
            } else {
                shufflePosition++;
            }
            position = qCounter.get(shufflePosition);
        } else {
            if (position < playlist.size() - 1) {
                position++;
            } else {
                position = 0;
            }
        }
        np = playlist.get(position);
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                np.getId());
        start(trackUri, playlist, position, false);
    }

    public void prev(){
        if (shuffled) {
            if (shufflePosition == 0) {
                qCounter = new ArrayList<Integer>();
                int count = playlist.size();
                setShuffleOrder(count, false);
                Log.d("queuer", "from service previous");
                shufflePosition = playlist.size() - 1;
            } else {
                shufflePosition--;
            }
            position = qCounter.get(shufflePosition);
        } else {
            if (position != 0) {
                position--;
            } else {
                position = playlist.size() - 1;
            }
        }
        np = playlist.get(position);
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                np.getId());
        start(trackUri, playlist, position, false);
    }

    public void play(){
        if (mp.isPlaying()) {
            mp.pause();
            removeAudioFocus();
            shouldResume = false;
            showNotification();
            if (fromNotification) {
                serviceCallbacks.cbSetPlay(false);
                stopForeground(false);
            }
            fromNotification = false;
        } else {
            if (requestAudioFocus()) {
                mp.start();
            }
            shouldResume = true;
            if (fromNotification) {
                serviceCallbacks.cbSetPlay(true);
            }
            fromNotification = false;
            showNotification();
        }
    }

    public void seekTo(int progress){
        mp.seekTo(progress);
    }

    public void updatePlaylist(ArrayList<Song> list, ArrayList<Integer> q, int c, int cs){
        playlist = list;
        qCounter = q;
        position = c;
        shufflePosition = cs;
    }
    public Boolean isPlaying(){
        if (mp.isPlaying()){
            return true;
        }
        else{
            return false;
        }
    }

    public int getCurrentPosition(){
        return mp.getCurrentPosition();
    }
    public int getDuration(){
        return mp.getDuration();
    }

    public void setShuffleOrder(int counter, Boolean init) {
        qCounter = new ArrayList<Integer>();
        int remove = position;

        if (init) {
            for (int i = 0; i < counter - 1; i++) {
                if (i < remove) {
                    qCounter.add(i, i);
                } else {
                    qCounter.add(i, i + 1);
                }
            }
        } else {
            for (int i = 0; i < counter; i++) {
                qCounter.add(i, i);
            }
        }
        long seed = System.nanoTime();
        Collections.shuffle(qCounter, new Random(seed));
        if (init) {
            qCounter.add(0, remove);
        }
        serviceCallbacks.cbSetQueue(qCounter);
        Log.d("queuer", Integer.toString(qCounter.get(0)));
    }

    public void playNext(Song nextTrack){
        if (exists()) {
            int placement = playlist.indexOf(nextTrack);
            if (shuffled) {
                qCounter.add(shufflePosition + 1, placement);
            } else {
                playlist.add(position + 1, nextTrack);
            }
            showNotification();
        }
    }
    @Override
    public void onDestroy() {
        //player.stop();
        //player.release();
    }

    @Override
    public void onLowMemory() {

    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public void setSessionControls() {
        mSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                play();
            }

            @Override
            public void onSkipToNext() {
                next();
                showNotification();
            }

            @Override
            public void onSkipToPrevious() {
                prev();
                showNotification();
            }

        });
    }

    public void defineIntents(){
        playAction = new Intent(this, MusicService.class);
        playAction.setAction(ACTION_PLAY);

        nextAction = new Intent(this, MusicService.class);
        nextAction.setAction(ACTION_NEXT);

        prevAction = new Intent(this, MusicService.class);
        prevAction.setAction(ACTION_PREV);

        launch = new Intent(this, MainActivity.class);
        launchActivity = PendingIntent.getActivity(this, 0, launch, 0);

        pendingPlay = PendingIntent.getService(this, 0, playAction, 0);
        pendingNext = PendingIntent.getService(this, 0, nextAction, 0);
        pendingPrev = PendingIntent.getService(this, 0, prevAction, 0);

    }

    public void showNotification(){

        String more;
        if (shuffled) {
            more = Integer.toString(shufflePosition+1) + "/" + Integer.toString(playlist.size());
        }
        else{
            more = Integer.toString(position+1) + "/" + Integer.toString(playlist.size());
        }

        int notificationAction = R.mipmap.pause;//needs to be initialized

        //Build a new notification according to the current state of the MediaPlayer
        PlaybackStateCompat.Builder state = new PlaybackStateCompat.Builder();
        if (mp.isPlaying()) {
            notificationAction = R.mipmap.pause;
            state.setState(PlaybackState.STATE_PLAYING, mp.getCurrentPosition(), 1.0f);
        } else{
            notificationAction = R.mipmap.play;
            state.setState(PlaybackState.STATE_PAUSED, mp.getCurrentPosition(), 1.0f);
        }
        mSession.setPlaybackState(state.build());

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),np.getArt());
        } catch (IOException e) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.empty_track);
            e.printStackTrace();
        }

        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, np.getArtist())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, np.getAlbum())
                .putString(MediaMetadata.METADATA_KEY_TITLE, np.getTrack())
                .build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notification = new android.support.v7.app.NotificationCompat.Builder(this)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.mipmap.notification)
                    .setShowWhen(false)
                    .addAction(new NotificationCompat.Action.Builder(R.mipmap.prev, "Previous", pendingPrev).build())
                    .addAction(new NotificationCompat.Action.Builder(notificationAction, "Pause", pendingPlay).build())
                    .addAction(new NotificationCompat.Action.Builder(R.mipmap.next, "Next", pendingNext).build())
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(np.getTrack())
                    .setContentText(np.getArtist())
                    .setSubText(more)
                    .setPriority(IMPORTANCE_MAX)
                    .setLargeIcon(bitmap)
                    .setContentIntent(launchActivity)
                    .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mSession.getSessionToken()).setShowActionsInCompactView(0,1,2))
                    .build();
        } else {
            notification = new android.support.v7.app.NotificationCompat.Builder(this)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.mipmap.notification)
                    .setShowWhen(false)
                    .addAction(new NotificationCompat.Action.Builder(android.R.drawable.ic_media_previous, "Previous", pendingPrev).build())
                    .addAction(new NotificationCompat.Action.Builder(notificationAction, "Pause", pendingPlay).build())
                    .addAction(new NotificationCompat.Action.Builder(android.R.drawable.ic_media_next, "Next", pendingNext).build())
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(np.getTrack())
                    .setContentText(np.getArtist())
                    .setSubText(more)
                    .setPriority(IMPORTANCE_MAX)
                    .setLargeIcon(bitmap)
                    .setContentIntent(launchActivity)
                    .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mSession.getSessionToken())
                            .setShowActionsInCompactView(0,1,2))
                    .build();
        }
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001, notification);
        startForeground(001, notification);
    }

    public void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            fromNotification = true;
            mControls.play();
        }
        else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            mControls.skipToNext();
        }
        else if (actionString.equalsIgnoreCase(ACTION_PREV)) {
            mControls.skipToPrevious();
        }
    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(001);
        stopForeground(true);
    }
}
