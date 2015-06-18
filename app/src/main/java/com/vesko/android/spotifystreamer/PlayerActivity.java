package com.vesko.android.spotifystreamer;

import android.os.Bundle;


public class PlayerActivity extends GenericActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, PlayerFragment.get(), PlayerFragment.TAG)
                .commit();
    }

    @Override
    protected boolean canDisplayNowPlayingButton() {
        return false;
    }
}
