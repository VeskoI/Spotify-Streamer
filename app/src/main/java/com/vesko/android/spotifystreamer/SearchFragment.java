package com.vesko.android.spotifystreamer;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class SearchFragment extends GenericFragment {

    private EditText mSearchField;
    private ListView mArtistsList;
    private ArtistsAdapter mArtistsAdapter;

    private Callback mCallback;

    public SearchFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callback)) {
            throw new RuntimeException("SearchFragment attached to the wrong Activity!");
        }

        mCallback = (Callback) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("vesko", "SearchFragment, onCreateView");
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        mSearchField = (EditText) root.findViewById(R.id.search_field_artist);
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

        mArtistsAdapter = new ArtistsAdapter(getActivity(), -1, new ArrayList<Artist>());
        mArtistsList = (ListView) root.findViewById(R.id.listview_artists);
        mArtistsList.setAdapter(mArtistsAdapter);
        mArtistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistsAdapter.getItem(position);
                mCallback.onItemSelected(artist.id, artist.name);
            }
        });

        if (BuildConfig.DEBUG) {
            searchForArtist("dre");
        }

        return root;
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
                Toast.makeText(getActivity(), getString(R.string.artist_not_found_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface Callback {
        void onItemSelected(String artistId, String artistName);
    }

}
