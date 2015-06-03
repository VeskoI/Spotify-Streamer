package com.vesko.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.vesko.android.spotifystreamer.adapters.TracksAdapter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Tracks;

public class TopTracksActivity extends GenericActivity {

    private static final String PARAM_COUNTRY = "country";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        String artistId = getIntent().getStringExtra(Extras.ARTIST_ID);
        String artistName = getIntent().getStringExtra(Extras.ARTIST_NAME);
        if (TextUtils.isEmpty(artistId)) {
            // Nothing to do here without artist id
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(artistName);
        }

        Log.d("vesko", "id: " + artistId);
        new GetTopTracksAsyncTask().execute(artistId);
    }

    private class GetTopTracksAsyncTask extends AsyncTask<String, Void, Tracks> {

        @Override
        protected Tracks doInBackground(String... params) {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put(PARAM_COUNTRY, getCountryCode());
            return spotify.getArtistTopTrack(params[0], queryParams);
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            boolean resultsFound = tracks.tracks.size() > 0;

            if (resultsFound) {
                populateList(tracks);
            } else {
                Toast.makeText(TopTracksActivity.this, getString(R.string.no_tracks_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateList(Tracks tracks) {
        TracksAdapter tracksAdapter = new TracksAdapter(TopTracksActivity.this, -1, tracks.tracks);
        ListView tracksList = (ListView) findViewById(R.id.listview_top_tracks);
        tracksList.setAdapter(tracksAdapter);
    }

    /**
     * @return 2-letter (ISO 3166-1 alpha-2) country code based on the current user settings.
     */
    private String getCountryCode() {
        return Locale.getDefault().getCountry();
    }
}
