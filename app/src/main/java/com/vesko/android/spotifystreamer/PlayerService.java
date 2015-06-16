package com.vesko.android.spotifystreamer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.vesko.android.spotifystreamer.model.Song;

import java.io.IOException;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String LOG_TAG = PlayerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1441;

    private MediaPlayer mMediaPlayer;
    private Song mCurrentSong;
    private STATE mState = STATE.NON_INITIALISED;

    public enum STATE {
        NON_INITIALISED,
        PREPARING,
        PLAYING,
        PAUSED,
        COMPLETED,
    }

    public static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public PlayerService() {
        log("constructor");
    }

    public void play(Song song) {
        if (mMediaPlayer == null) {
            initMediaPlayer(song);
        }

        if (song.equals(mCurrentSong) && mState.equals(STATE.PAUSED)) {
            // Same song, just resume it
            startPlayback();
        }

        if (!song.equals(mCurrentSong)) {
            // Play a new song
            mMediaPlayer.stop();
            mMediaPlayer.reset();

            changeMediaPlayerSource(song);
        }
    }

    private void startPlayback() {
        mMediaPlayer.start();
        mState = STATE.PLAYING;
        refreshOngoingNotification();
    }

    private void initMediaPlayer(Song song) {
        log("initMediaPlayer ...");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        changeMediaPlayerSource(song);
    }

    private void changeMediaPlayerSource(Song song) {
        try {
            mCurrentSong = song;
            mMediaPlayer.setDataSource(song.getPreviewUrl());
            mMediaPlayer.prepareAsync();
            mState = STATE.PREPARING;
            refreshOngoingNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return mState.equals(STATE.PLAYING);
    }
    private final PlayerBinder mBinder = new PlayerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log("onUnbind");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startPlayback();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        log("error, what: " + what + ", extra: " + extra);
        return false;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            mState = STATE.PAUSED;
            refreshOngoingNotification();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        log("onCompletion");
        mState = STATE.COMPLETED;
        refreshOngoingNotification();
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    private void refreshOngoingNotification() {
        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(), 0,
                new Intent(getApplicationContext(), PlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getNotificationTitle())
                .setContentText(mCurrentSong.getArtistName() + " -> " + mCurrentSong.getName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(pi);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private String getNotificationTitle() {
        switch (mState) {
            case PREPARING:
                return getString(R.string.music_buffering);
            case PAUSED:
                return getString(R.string.music_paused);
            case PLAYING:
                return getString(R.string.music_playing);
            case COMPLETED:
                return getString(R.string.music_completed);
            default:
                return null;
        }
    }
}
