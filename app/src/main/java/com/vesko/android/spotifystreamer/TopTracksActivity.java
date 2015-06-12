package com.vesko.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.vesko.android.spotifystreamer.adapters.SongsAdapter;
import com.vesko.android.spotifystreamer.model.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTracksActivity extends GenericActivity {

    private static final String PARAM_COUNTRY = "country";

    private ArrayList<Song> mSongs = new ArrayList<>();

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

        new GetTopTracksAsyncTask().execute(artistId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void populateList(final Tracks tracks) {
        mSongs = extractSongs(tracks);

        final SongsAdapter songsAdapter = new SongsAdapter(TopTracksActivity.this, -1, mSongs);
        ListView tracksList = (ListView) findViewById(R.id.listview_top_tracks);
        tracksList.setAdapter(songsAdapter);
        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = songsAdapter.getItem(position);
                Log.d(PlayerService.LOG_TAG, song.getName() + ", previewUrl: " + song.getPreviewUrl());
                Intent i = new Intent(TopTracksActivity.this, PlayerActivity.class);
                i.putExtra(Extras.TRACK_URI, song.getPreviewUrl());
                startActivity(i);
            }
        });
    }

    private ArrayList<Song> extractSongs(Tracks tracks) {
        ArrayList<Song> songs = new ArrayList<>(tracks.tracks.size());
        for (Track t : tracks.tracks) {
            Song s = new Song(t);
            songs.add(s);
        }
        return songs;
    }

    /**
     * @return 2-letter (ISO 3166-1 alpha-2) country code based on the current user settings.
     */
    private String getCountryCode() {
        return Locale.getDefault().getCountry();
    }
}
