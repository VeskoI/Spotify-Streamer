package com.vesko.android.spotifystreamer;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class TopTracksFragment extends GenericFragment {

    public static final String FRAGMENT_TAG = "ttft";
    private static final String PARAM_COUNTRY = "country";

    private ArrayList<Song> mSongs = new ArrayList<>();
    private ListView mList;
    private SongsAdapter mAdapter;
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
                Toast.makeText(getActivity(), getString(R.string.no_tracks_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateList(final Tracks tracks) {
        mSongs = extractSongs(tracks);
        mAdapter = new SongsAdapter(getActivity(), -1, mSongs);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = mAdapter.getItem(position);
                Log.d("vesko", song.getName() + ", previewUrl: " + song.getPreviewUrl() + ", position: " + position + ", id: " + id + ", mSongsName: " + mSongs.get(position).getName());

                if (mTwoPane) {
                    Log.d("vesko", "twoPane, starting dialogFragment");
                    PlayerFragment playerFragment = PlayerFragment.get(mSongs, position);
                    playerFragment.show(getFragmentManager(), PlayerFragment.TAG);
                } else {
                    Log.d("vesko", "standard, calling Activity");
                    Intent i = new Intent(getActivity(), PlayerActivity.class);
                    i.putParcelableArrayListExtra(Extras.TRACKS_LIST, mSongs);
                    i.putExtra(Extras.SELECTED_TRACK, position);
                    startActivity(i);
                }
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
