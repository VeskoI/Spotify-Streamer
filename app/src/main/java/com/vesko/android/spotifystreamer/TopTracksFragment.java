package com.vesko.android.spotifystreamer;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.vesko.android.spotifystreamer.adapters.SongsAdapter;
import com.vesko.android.spotifystreamer.model.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public class TopTracksFragment extends GenericFragment {

    public static final String FRAGMENT_TAG = "ttft";
    private static final String PARAM_COUNTRY = "country";

    private ListView mList;
    private boolean mTwoPane;

    public static TopTracksFragment get(String artistId, boolean twoPane) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(Extras.ARTIST_ID, artistId);
        args.putBoolean(Extras.TWO_PANE, twoPane);
        fragment.setArguments(args);
        return fragment;
    }

    public TopTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mList = (ListView) root.findViewById(R.id.listview_top_tracks);

        String artistId = getArguments().getString(Extras.ARTIST_ID);
        mTwoPane = getArguments().getBoolean(Extras.TWO_PANE);

        if (!TextUtils.isEmpty(artistId)) {
            new GetTopTracksAsyncTask().execute(artistId);
        }

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_now_playing:
                startPlayer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetTopTracksAsyncTask extends AsyncTask<String, Void, Tracks> {

        @Override
        protected Tracks doInBackground(String... params) {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put(PARAM_COUNTRY, Utils.getCountryCode(getActivity()));
            return spotify.getArtistTopTrack(params[0], queryParams);
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            boolean resultsFound = tracks.tracks.size() > 0;

            if (resultsFound) {
                populateList(tracks);
            } else {
                Toast.makeText(getActivity(), getString(R.string.no_tracks_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateList(final Tracks tracks) {
        ArrayList<Song> songs = extractSongs(tracks);

        // Save the song list globally in the Application
        SpotifyStreamerApp.getApp().setSongs(songs);

        SongsAdapter mAdapter = new SongsAdapter(getActivity(), -1, songs);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyStreamerApp.getApp().setCurrentSongIdx(position);

                startPlayer();
            }
        });
    }

    private void startPlayer() {
        if (mTwoPane) {
            PlayerFragment playerFragment = PlayerFragment.get();
            playerFragment.show(getFragmentManager(), PlayerFragment.TAG);
        } else {
            startActivity(new Intent(getActivity(), PlayerActivity.class));
        }
    }

    private ArrayList<Song> extractSongs(Tracks tracks) {
        ArrayList<Song> songs = new ArrayList<>(tracks.tracks.size());
        for (Track t : tracks.tracks) {
            Song s = new Song(t);
            songs.add(s);
        }
        return songs;
    }
}
