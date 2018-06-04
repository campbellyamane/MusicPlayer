package com.campbellyamane.musicplayer;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cesards.cropimageview.CropImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import jp.wasabeef.picasso.transformations.CropTransformation;

import static com.campbellyamane.musicplayer.R.id.artist;

public class SingleArtist extends General {

    private String url1 = "http://ws.audioscrobbler.com/2.0/?method=artist.search&artist=";
    private String url2 = "&api_key=63eeb5bf68691f81444f58056a178623&format=json";
    public String clickedArtist;
    public long clickedArtistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_single_artist);
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        clickedArtist = bundle.getString("Artist");
        clickedArtistId = bundle.getLong("ID");
        Log.d("ThisID", Long.toString(clickedArtistId));
        TextView artist = (TextView) this.findViewById(R.id.artist);
        artist.setText(clickedArtist);


        new getArtistImage().execute(clickedArtist);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        ArtistCategoryAdapter adapter = new ArtistCategoryAdapter(getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);


    }


    public class getArtistImage extends AsyncTask<String, String, String> {

        private String thisArtist;
        private String imgUrl = null;

        @Override
        protected String doInBackground(String... artist) {
            thisArtist = artist[0].toLowerCase();
            URL url = null;
            try {
                url = new URL(url1 + thisArtist + url2);
                InputStream in = url.openStream();
                InputStreamReader reader = new InputStreamReader(in);
                JSONObject json = getObject(reader);
                imgUrl = json.getJSONObject("results").getJSONObject("artistmatches")
                        .getJSONArray("artist").getJSONObject(0).getJSONArray("image")
                        .getJSONObject(3).getString("#text");
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return imgUrl;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //Update the progress of current task
        }

        @Override
        protected void onPostExecute(String url) {
            try{
                final ImageView imageView = (ImageView) findViewById(R.id.artistImage);

                if (url.length() > 0) {
                    Picasso.with(getApplicationContext())
                            .load(url)
                            .placeholder(R.mipmap.empty_artist)
                            .fit().centerCrop()
                            .into(imageView);
                }
                else{
                    Picasso.with(getApplicationContext())
                            .load(R.mipmap.empty_artist)
                            .fit().centerCrop()
                            .into(imageView);
                }
            }
            catch (IllegalArgumentException e){

            }
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
}


