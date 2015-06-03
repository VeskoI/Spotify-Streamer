package com.vesko.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.vesko.android.spotifystreamer.adapters.TracksAdapter;

import kaaes.spotify.webapi.android.models.Tracks;

public class TopTracksActivity extends GenericActivity {

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
            return spotify.getArtistTopTrack(params[0]);
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            Log.d("vesko", "total: " + tracks.tracks.size());

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
}
