package com.vesko.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.model.Song;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class PlayerActivity extends GenericActivity {

    @InjectView(R.id.player_artist_name)TextView mArtistName;
    @InjectView(R.id.player_album_name) TextView mAlbumName;
    @InjectView(R.id.player_album_artwork) ImageView mAlbumPic;
    @InjectView(R.id.player_track_name) TextView mTrackName;
    @InjectView(R.id.player_btn_play) ImageButton mPlayPauseButton;
    @InjectView(R.id.player_track_seekbar) SeekBar mSeekBar;
    @InjectView(R.id.player_track_progress) TextView mTrackProgress;
    @InjectView(R.id.player_track_duration) TextView mTrackDuration;

    private static final long ONE_SECOND = 1000;

    private Handler mHandler = new Handler();
    private PlayerService mPlayerService;
    private ArrayList<Song> mSongs;
    private int mSongIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.inject(this);

        mSongs = getIntent().getParcelableArrayListExtra(Extras.TRACKS_LIST);
        mSongIndex = getIntent().getIntExtra(Extras.SELECTED_TRACK, -1);

        if (mSongs == null || mSongs.size() == 0 || mSongIndex == -1) {
            // Wrong input, nothing to do here
            Toast.makeText(this, getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindMusicService();

        refreshPlayerViews();

        registerReceiver(playerBroadcastReceiver, new IntentFilter(PlayerService.MUSIC_STATUS_CHANGED));
    }

    private void refreshPlayerViews() {
        Song currentSong = mSongs.get(mSongIndex);
        mArtistName.setText(currentSong.getArtistName());
        mAlbumName.setText(currentSong.getAlbumName());
        mTrackName.setText(currentSong.getName());
        Picasso.with(this).load(currentSong.getAlbumPic()).into(mAlbumPic);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayerService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBar.setProgress(0);
        mTrackProgress.setText(R.string.default_track_progress);
        mTrackDuration.setText(R.string.default_track_duration);
    }

    @OnClick(R.id.player_btn_play)
    void handlePlayPauseButton() {
        if (mPlayerService == null) {
            return;
        }

        if (mPlayerService.isPlaying()) {
            mPlayerService.pause();
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            Song song = mSongs.get(mSongIndex);
            mPlayerService.play(song);
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @OnClick(R.id.player_btn_previous)
    void playPreviousTrack() {
        changeTrack(false);
    }

    @OnClick(R.id.player_btn_next)
    void playNextTrack() {
        changeTrack(true);
    }

    private void changeTrack(boolean forward) {
        Song song = getOtherSong(forward);
        mPlayerService.play(song);
        refreshPlayerViews();

    }

    private Song getOtherSong(boolean forward) {
        if (forward) {
            if (mSongIndex == mSongs.size() - 1) {
                mSongIndex = 0;
            } else {
                mSongIndex++;
            }
        } else {
            if (mSongIndex == 0) {
                mSongIndex = mSongs.size() - 1;
            } else {
                mSongIndex--;
            }
        }
        return mSongs.get(mSongIndex);
    }

    private void bindMusicService() {
        bindService(
                new Intent(this, PlayerService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder playerBinder = (PlayerService.PlayerBinder) service;
            mPlayerService = playerBinder.getService();
            mPlayerService.play(mSongs.get(mSongIndex));
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("vesko", "Activity, onServiceDisconnected");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(playerBroadcastReceiver);
        } catch (Exception ignored) {}

        // TODO serviceConnection keeps leaking
        serviceConnection = null;
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayerService == null || mSeekBar == null) {
                return;
            }

            int trackProgress = mPlayerService.getSongProgress();
            int totalDuration = mPlayerService.getSongDuration();

            mSeekBar.setMax(totalDuration);
            mSeekBar.setProgress(trackProgress);

            mTrackProgress.setText(Utils.getTimeString(trackProgress));
            mTrackDuration.setText(Utils.getTimeString(totalDuration));

            postSeekBarUpdate();
        }
    };

    private void postSeekBarUpdate() {
        mHandler.postDelayed(updateSeekBarRunnable, ONE_SECOND);
    }

    public BroadcastReceiver playerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean started = intent.getBooleanExtra(Extras.STARTED, false);
            mPlayPauseButton.setImageResource(started ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            if (started) {
                postSeekBarUpdate();
            } else {
                mHandler.removeCallbacks(updateSeekBarRunnable);
            }
        }

    };
}
