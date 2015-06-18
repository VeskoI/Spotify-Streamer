package com.vesko.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("vesko", "menu MainActivity");
        int id = item.getItemId();

        // In case of twoPane, the NowPlaying button will be handled by TopTracksFragment,
        // which will surely be on the screen, thus we care only about standard layout here.
        if (id == R.id.action_now_playing && !mTwoPane) {
            startActivity(new Intent(this, PlayerActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void startPlayer() {
//        if (mTwoPane) {
//            Log.d("vesko", "twoPane, starting dialogFragment");
//            PlayerFragment playerFragment = PlayerFragment.get();
//            playerFragment.show(getFragmentManager(), PlayerFragment.TAG);
//        } else {
//            Log.d("vesko", "standard, calling Activity");
//            startActivity(new Intent(getActivity(), PlayerActivity.class));
//        }
//    }
}
