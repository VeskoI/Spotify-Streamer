package com.vesko.android.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.model.Song;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class PlayerActivity extends GenericActivity {

    /*
    <SeekBar
        android:id="@+id/player_track_duration"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
     */

    @InjectView(R.id.player_artist_name)TextView mArtistName;
    @InjectView(R.id.player_album_name) TextView mAlbumName;
    @InjectView(R.id.player_album_artwork) ImageView mAlbumPic;
    @InjectView(R.id.player_track_name) TextView mTrackName;
    @InjectView(R.id.player_btn_play) ImageButton mPlayPauseButton;
    @InjectView(R.id.player_btn_previous) ImageButton mPreviousButton;
    @InjectView(R.id.player_btn_next) ImageButton mNextButton;

    private PlayerService playerService;

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
    }

    private void refreshPlayerViews() {
        Song currentSong = mSongs.get(mSongIndex);
        mArtistName.setText(currentSong.getArtistName());
        mAlbumName.setText(currentSong.getAlbumName());
        mTrackName.setText(currentSong.getName());
        Picasso.with(this).load(currentSong.getAlbumPic()).into(mAlbumPic);
    }

    @OnClick(R.id.player_btn_play)
    void handlePlayPauseButton() {
        if (playerService == null) {
            return;
        }

        if (playerService.isPlaying()) {
            playerService.pause();
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            Song song = mSongs.get(mSongIndex);
            playerService.play(song);
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
        playerService.play(song);
        refreshPlayerViews();
    }

    private Song getOtherSong(boolean forward) {
        if (forward) {
            if (mSongIndex == mSongs.size() - 1) {
                Log.d("vesko", "end, =0");
                mSongIndex = 0;
            } else {
                Log.d("vesko", "++");
                mSongIndex++;
            }
        } else {
            if (mSongIndex == 0) {
                Log.d("vesko", "start, moving to end");
                mSongIndex = mSongs.size() - 1;
            } else {
                Log.d("vesko", "--");
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
            playerService = playerBinder.getService();
            playerService.play(mSongs.get(mSongIndex));
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

        // TODO serviceConnection keeps leaking
        serviceConnection = null;
    }
}
