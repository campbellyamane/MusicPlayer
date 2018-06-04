package com.campbellyamane.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.editable;
import static android.R.attr.type;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.campbellyamane.musicplayer.R.id.artistList;
import static com.campbellyamane.musicplayer.R.id.trackResults;
import static java.security.AccessController.getContext;

public class MainActivity extends General {

    public LinearLayout viewStuff;
    public LinearLayout results;
    public ListView searchTracks;
    public MyEditText search;

    public static ArrayList<Song> songDb;
    public static ArrayList<Artist> artistDb;
    public static String term;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onBackPressed() {
        MyEditText search = (MyEditText) findViewById(R.id.search);
        SlidingUpPanelLayout panel = (SlidingUpPanelLayout) findViewById(R.id.slider);
        Log.d("BackPresser", "Registered");
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else if (search.length() != 0){
            Log.d("BackPresser", "TextExists");
            search.setText("");
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyStoragePermissions(this);
        super.onCreate(savedInstanceState);
    }

    public void launch() {
        getNewest();
        setContentView(R.layout.activity_main);
        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);

        viewStuff = (LinearLayout) findViewById(R.id.viewStuff);
        results = (LinearLayout) findViewById(R.id.results);

        final float scale = getResources().getDisplayMetrics().density;
        final int rowHeight = (int) (52 * scale + 0.5);
        Log.d("RowHeight", Integer.toString(rowHeight));

        search = (MyEditText) findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            ArrayList<Song> trackResults = new ArrayList<Song>();

            final ListView tResults = (ListView) findViewById(R.id.trackResults);

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                trackResults = new ArrayList<Song>();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                term = charSequence.toString().toLowerCase();
                viewStuff.setVisibility(View.GONE);
                results.setVisibility(View.VISIBLE);
                for (int j = 0; j < songDb.size() - 1; j++) {
                    if (songDb.get(j).getTrack().toLowerCase().contains(term)) {
                        trackResults.add(songDb.get(j));
                    }
                    if (trackResults.size() == 20){
                        break;
                    }
                }
                if (charSequence.length() == 0) {
                    viewStuff.setVisibility(View.VISIBLE);
                    results.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                LinearLayout trackSearch = (LinearLayout) findViewById(R.id.songSearch);
                SongAdapter itemsAdapter1 = new SongAdapter(MainActivity.this, trackResults);

                tResults.setAdapter(itemsAdapter1);

                tResults.getLayoutParams().height = rowHeight * trackResults.size();


                if (trackResults.size() == 0) {
                    trackSearch.setVisibility(View.GONE);
                    Log.d("Texter", "Gone");
                } else {
                    trackSearch.setVisibility(View.VISIBLE);
                    tResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Uri trackUri = ContentUris.withAppendedId(
                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    trackResults.get(i).getId());
                            ArrayList<Song> oneSong = new ArrayList<Song>();
                            oneSong.add(trackResults.get(i));
                            InputMethodManager inputManager = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);

                            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                            startTrack(trackUri, oneSong, 0, true);
                            openPanel();
                        }
                    });
                }

            }
        });
        if (!goodToLoad){
            goodToLoad = true;
            //defineVariables();
        }

    }

    public  void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        else{
            launch();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PERMISSION_GRANTED){
            launch();
        }
        else{
            verifyStoragePermissions(this);
        }
    }

    public void getNewest(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int dateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            Long date = musicCursor.getLong(dateColumn);
            StorageUtils storage = new StorageUtils(getApplicationContext());
            if(date > storage.loadNewest()){
                Log.d("Reloaded","damn");
                reload = true;
                storage.storeNewest(date);
            }
        }
        musicCursor.close();
    }

}
