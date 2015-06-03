package com.vesko.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vesko.android.spotifystreamer.adapters.ArtistsAdapter;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class MainActivity extends ActionBarActivity {

    private TextView mNoResultsText;
    private EditText mSearchField;
    private ListView mArtistsList;
    private ArtistsAdapter mArtistsAdapter;

    private SpotifyService spotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSpotify();
        initViews();
    }

    private void initSpotify() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
    }

    private void initViews() {
        mSearchField = (EditText) findViewById(R.id.search_field_artist);
        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchForArtist(mSearchField.getText().toString());
                    return true;
                }

                return false;
            }
        });
        mNoResultsText = (TextView) findViewById(R.id.textview_no_artists_found);

        mArtistsAdapter = new ArtistsAdapter(this, -1, new ArrayList<Artist>());
        mArtistsList = (ListView) findViewById(R.id.listview_artists);
        mArtistsList.setAdapter(mArtistsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchForArtist(String name) {
        new SearchForArtistsAsyncTask().execute(name);
    }

    private class SearchForArtistsAsyncTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {
            return spotify.searchArtists(params[0]);
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);

            Log.d("vesko", "total: " + artistsPager.artists.total + ", old-fashioned: " + artistsPager.artists.items.size());

            boolean resultsFound = artistsPager.artists.total > 0;

            mArtistsList.setVisibility(resultsFound ? View.VISIBLE : View.GONE);
            mNoResultsText.setVisibility(resultsFound ? View.GONE : View.VISIBLE);

            if (resultsFound) {
                mArtistsAdapter.clear();
                mArtistsAdapter.addAll(artistsPager.artists.items);
            }
        }
    }
}
