package com.vesko.android.spotifystreamer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

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
}
