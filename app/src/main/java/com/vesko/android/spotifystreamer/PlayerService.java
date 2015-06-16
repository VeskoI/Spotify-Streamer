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

import java.io.IOException;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
//    public static final String ACTION_PLAY = "play";
//    public static final String ACTION_PAUSE = "pause";

    public static final String LOG_TAG = PlayerService.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private boolean mLoading = false;

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

    public void play(String trackUri) {
        if (mMediaPlayer == null) {
            initMediaPlayer(trackUri);
        }
        else if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void playOtherTrack(String trackUri) {
        if (mMediaPlayer == null) {
            initMediaPlayer();
        }

        if (mMediaPlayer.isPlaying() || mLoading) {
            Log.d("vesko", "isPlaying: " + (mMediaPlayer.isPlaying()) + ", mLoading: " + mLoading);
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }

        // TODO crash if we receive another request while the previous is paused ...
        try {
            mMediaPlayer.setDataSource(trackUri);
            mMediaPlayer.prepareAsync();
            mLoading = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initMediaPlayer() {
        initMediaPlayer(null);
    }

    private void initMediaPlayer(String url) {
        log("initMediaPlayer ...");
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            if (url != null) {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepareAsync();
                mLoading = true;
            }
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
        mLoading = false;
        mMediaPlayer.start();
        runAsForeground();
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

    private void runAsForeground() {
        String songName;
        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), PlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("test")
                .setContentText("test content text")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(pi);

        startForeground(1441, builder.build());
    }
}
