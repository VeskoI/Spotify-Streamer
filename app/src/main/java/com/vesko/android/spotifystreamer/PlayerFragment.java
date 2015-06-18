package com.vesko.android.spotifystreamer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.model.Song;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlayerFragment extends DialogFragment {

    public static final String TAG = "PlayerFragmentTag";

    public static PlayerFragment get() {
        return new PlayerFragment();
    }

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
    private PlayerService.IPlayerCallbacks mService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, root);

        if (SpotifyStreamerApp.getApp().getCurrentSongIdx() == -1) {
            // Wrong input, nothing to do here
            Toast.makeText(getActivity(), getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
            return null;
        }

        // startService() will make sure it'll run beyong the lifecycle of the Fragment/Activity
        getActivity().startService(
                new Intent(getActivity(), PlayerService.class).setAction(PlayerService.ACTION_PLAY));

        refreshPlayerViews();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_MUSIC_STATUS_CHANGED);
        intentFilter.addAction(PlayerService.ACTION_MUSIC_PREPARE_STARTED);
        getActivity().registerReceiver(playerBroadcastReceiver, intentFilter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().bindService(
                new Intent(getActivity(), PlayerService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        mHandler.removeCallbacks(updateSeekBarRunnable);

        getActivity().unbindService(serviceConnection);
        serviceConnection = null;
    }

    /**
     * Called by the system when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void refreshPlayerViews() {
        Song currentSong = SpotifyStreamerApp.getApp().getCurrentSong();
        mArtistName.setText(currentSong.getArtistName());
        mAlbumName.setText(currentSong.getAlbumName());
        mTrackName.setText(currentSong.getName());
        Picasso.with(getActivity()).load(currentSong.getAlbumPic()).into(mAlbumPic);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mService.seekTo(progress);
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
        if (mService == null) {
            return;
        }

        if (mService.isPlaying()) {
            mService.pause();
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mService.play(SpotifyStreamerApp.getApp().getCurrentSongIdx());
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
        mService.changeTrack(forward);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (PlayerService.IPlayerCallbacks) service;

            if (mService.isPlaying()) {
                // Someone, somewhere has already started music ... make sure that our UI is up-to-date
                refreshUiAfterMusicUpdate(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            getActivity().unregisterReceiver(playerBroadcastReceiver);
        } catch (Exception ignored) {}

        // TODO serviceConnection keeps leaking
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mService == null || mSeekBar == null) {
                return;
            }

            int trackProgress = mService.getSongProgress();
            int totalDuration = mService.getSongDuration();

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

    private BroadcastReceiver playerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshPlayerViews();

            boolean started = intent.getBooleanExtra(Extras.STARTED, false);

            switch (intent.getAction()) {
                case PlayerService.ACTION_MUSIC_STATUS_CHANGED:
                    refreshUiAfterMusicUpdate(started);
                    break;
            }

        }

    };

    private void refreshUiAfterMusicUpdate(boolean started) {
        mPlayPauseButton.setImageResource(started ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        if (started) {
            postSeekBarUpdate();
        } else {
            mHandler.removeCallbacks(updateSeekBarRunnable);
        }
    }
}
