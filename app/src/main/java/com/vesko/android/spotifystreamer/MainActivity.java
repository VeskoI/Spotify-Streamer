package com.vesko.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vesko.android.spotifystreamer.adapters.ArtistsAdapter;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class MainActivity extends GenericActivity {

    private EditText mSearchField;
    private ListView mArtistsList;
    private ArtistsAdapter mArtistsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        if (BuildConfig.DEBUG) {
            searchForArtist("dre");
        }
    }

    private void initViews() {
        mSearchField = (EditText) findViewById(R.id.search_field_artist);
        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String searchText = mSearchField.getText().toString().trim();
                    if (!TextUtils.isEmpty(searchText)) {
                        searchForArtist(searchText);
                    }
                    return true;
                }

                return false;
            }
        });

        mArtistsAdapter = new ArtistsAdapter(this, -1, new ArrayList<Artist>());
        mArtistsList = (ListView) findViewById(R.id.listview_artists);
        mArtistsList.setAdapter(mArtistsAdapter);
        mArtistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistsAdapter.getItem(position);
                Intent i = new Intent(MainActivity.this, TopTracksActivity.class);
                i.putExtra(Extras.ARTIST_ID, artist.id);
                i.putExtra(Extras.ARTIST_NAME, artist.name);
                startActivity(i);
            }
        });
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

            boolean resultsFound = artistsPager.artists.total > 0;

            if (resultsFound) {
                mArtistsAdapter.clear();
                mArtistsAdapter.addAll(artistsPager.artists.items);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.artist_not_found_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
