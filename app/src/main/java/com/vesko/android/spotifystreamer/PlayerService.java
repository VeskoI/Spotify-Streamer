package com.vesko.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";

    public static final String LOG_TAG = PlayerService.class.getSimpleName();

    private MediaPlayer mMediaPlayer;

    public static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    public PlayerService() {
        log("constructor");
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        log("onStartCommand, action: " + intent.getAction());
//        switch (intent.getAction()) {
//            case ACTION_PLAY:
//                String
//                play(intent);
//                break;
//
//            case ACTION_PAUSE:
//                if (isPlaying()) {
//                    mMediaPlayer.pause();
//                }
//                break;
//        }
//
//
//        return START_STICKY;
//    }

    public void play(String url) {
        if (mMediaPlayer == null) {
            initMediaPlayer(url);
        }
        else if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void initMediaPlayer(String url) {
        log("initMediaPlayer ...");
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
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
        mMediaPlayer.start();
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
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        log("onCompletion");
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }
}
