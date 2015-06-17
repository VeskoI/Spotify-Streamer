package com.vesko.android.spotifystreamer;

import android.os.Bundle;
import android.util.Log;


public class PlayerActivity extends GenericActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        int mSongIndex = getIntent().getIntExtra(Extras.SELECTED_TRACK, -1);

        Log.d("vesko", "adding fragment from activity");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, PlayerFragment.get(mSongIndex), PlayerFragment.TAG)
                .commit();
    }
}
