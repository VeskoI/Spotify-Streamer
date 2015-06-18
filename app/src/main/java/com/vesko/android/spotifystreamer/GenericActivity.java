package com.vesko.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public abstract class GenericActivity extends ActionBarActivity {

    protected boolean isMusicPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_MUSIC_STATUS_CHANGED);
        intentFilter.addAction(PlayerService.ACTION_MUSIC_PREPARE_STARTED);
        registerReceiver(playerBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_now_playing);
        item.setVisible(isMusicPlaying);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(playerBroadcastReceiver);
        } catch (Exception ignored) {}
    }

    /**
     * Indicate whether this Activity can display the "Now Playing" action button or not.
     */
    protected boolean canDisplayNowPlayingButton() {
        return true;
    }

    private BroadcastReceiver playerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PlayerService.ACTION_MUSIC_STATUS_CHANGED)) {
                boolean newStatus = intent.getBooleanExtra(Extras.STARTED, false);
                if (newStatus != isMusicPlaying && canDisplayNowPlayingButton()) {
                    invalidateOptionsMenu();
                }

                isMusicPlaying = newStatus;
            }
        }
    };
}
