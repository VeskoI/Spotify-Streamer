package com.vesko.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends GenericActivity implements SearchFragment.Callback {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.top_tracks_container) != null) {
            // We're in two-pane mode
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_tracks_container, TopTracksFragment.get(null, mTwoPane), TopTracksFragment.FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onItemSelected(String artistId, String artistName) {
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, TopTracksFragment.get(artistId, mTwoPane), TopTracksFragment.FRAGMENT_TAG)
                    .commit();
        } else {
            Intent i = new Intent(this, TopTracksActivity.class);
            i.putExtra(Extras.ARTIST_ID, artistId);
            i.putExtra(Extras.ARTIST_NAME, artistName);
            startActivity(i);
        }
    }
}
