package com.vesko.android.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class PlayerActivity extends GenericActivity {

    @InjectView(R.id.player_btn_play) ImageButton mPlayPauseButton;
    @InjectView(R.id.player_btn_previous) ImageButton mPreviousButton;
    @InjectView(R.id.player_btn_next) ImageButton mNextButton;

    private PlayerService playerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ButterKnife.inject(this);

        bindMusicService();
    }

    @OnClick(R.id.player_btn_play)
    void handlePlayPauseButton() {
        if (playerService == null) {
            return;
        }

        if (playerService.isPlaying()) {
            playerService.pause();
        } else {
            String trackUri = getIntent().getStringExtra(Extras.TRACK_URI);
            playerService.play(trackUri);
        }
    }

    private void bindMusicService() {
        String trackUri = getIntent().getStringExtra(Extras.TRACK_URI);
        Intent i = new Intent(this, PlayerService.class);
        i.setAction(PlayerService.ACTION_PLAY);
        i.putExtra(Extras.TRACK_URI, trackUri);

        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder playerBinder = (PlayerService.PlayerBinder) service;
            playerService = playerBinder.getService();

            // TODO pass music to binder
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
