package com.vesko.android.spotifystreamer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.vesko.android.spotifystreamer.model.Song;

import java.util.ArrayList;


public class PlayerActivity extends GenericActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ArrayList<Song> mSongs = getIntent().getParcelableArrayListExtra(Extras.TRACKS_LIST);
        int mSongIndex = getIntent().getIntExtra(Extras.SELECTED_TRACK, -1);

        if (mSongs == null || mSongs.size() == 0 || mSongIndex == -1) {
            // Wrong input, nothing to do here
            Toast.makeText(this, getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("vesko", "adding fragment from activity");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, PlayerFragment.get(mSongs, mSongIndex), "tag")
                .commit();
    }
}
