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
import android.widget.RemoteViews;

import com.vesko.android.spotifystreamer.model.Song;

import java.io.IOException;
import java.util.ArrayList;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String ACTION_PAUSE = "com.vesko.android.spotifystreamer.ACTION_PAUSE";
    public static final String ACTION_RESUME = "com.vesko.android.spotifystreamer.ACTION_RESUME";
    public static final String ACTION_NEXT = "com.vesko.android.spotifystreamer.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.vesko.android.spotifystreamer.ACTION_PREVIOUS";

    public static final String MUSIC_STATUS_CHANGED = "com.vesko.android.spotifystreamer.MUSIC_STATUS_CHANGED";

    private static final int NOTIFICATION_ID = 1441;

    private static final int REQUEST_CODE_PAUSE = 2001;
    private static final int REQUEST_CODE_RESUME = 2002;
    private static final int REQUEST_CODE_NEXT = 2003;
    private static final int REQUEST_CODE_PREVIOUS = 2004;

    private final PlayerBinder mBinder = new PlayerBinder();
    private MediaPlayer mMediaPlayer;
    private RemoteViews mRemoteViews;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_RESUME:
                resume();
                break;

            case ACTION_PAUSE:
                pause();
                break;

            case ACTION_PREVIOUS:

                break;

            case ACTION_NEXT:

                break;
        }

        return START_STICKY;
    }

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
    public void onCompletion(MediaPlayer mp) {
        log("onCompletion");
        mState = STATE.COMPLETED;
        refreshOngoingNotification();
        sendBroadcast(new Intent(MUSIC_STATUS_CHANGED).putExtra(Extras.STARTED, false));
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mState = STATE.NON_INITIALISED;
        }
    }

    private void play(Song song) {
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

    private void resume() {
        if (mState.equals(STATE.PAUSED)) {
            startPlayback();
        }
    }

    private void pause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            mState = STATE.PAUSED;
            refreshOngoingNotification();
            sendBroadcast(new Intent(MUSIC_STATUS_CHANGED).putExtra(Extras.STARTED, false));
        }
    }

    private void seekTo(int progress) {
        if (!mState.equals(STATE.PLAYING)) {
            return;
        }

        mMediaPlayer.seekTo(progress);
    }

    private boolean isPlaying() {
        return mState.equals(STATE.PLAYING);
    }

    private int getSongDuration() {
        if (mState.equals(STATE.PLAYING)) {
            return mMediaPlayer.getDuration();
        }

        return 0;
    }

    private int getSongProgress() {
        if (mState.equals(STATE.PLAYING)) {
            return mMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    private void startPlayback() {
        mMediaPlayer.start();
        mState = STATE.PLAYING;
        refreshOngoingNotification();

        sendBroadcast(new Intent(MUSIC_STATUS_CHANGED).putExtra(Extras.STARTED, true));
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

    private void refreshOngoingNotification() {
        if (mRemoteViews == null) {
            mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        }

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent next = PendingIntent.getService(
                getApplicationContext(),
                REQUEST_CODE_NEXT,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_next, next);

        Intent previousIntent = new Intent(ACTION_PREVIOUS);
        PendingIntent previous = PendingIntent.getService(
                getApplicationContext(),
                REQUEST_CODE_PREVIOUS,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_previous, previous);

        if (mState.equals(STATE.PLAYING)) {
            Intent pauseIntent = new Intent(ACTION_PAUSE);
            PendingIntent pause = PendingIntent.getService(
                    getApplicationContext(),
                    REQUEST_CODE_PAUSE,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mRemoteViews.setImageViewResource(R.id.notification_play_pause, android.R.drawable.ic_media_pause);
            mRemoteViews.setOnClickPendingIntent(R.id.notification_play_pause, pause);
        } else {
            Intent playIntent = new Intent(ACTION_RESUME);
            PendingIntent play = PendingIntent.getService(
                    getApplicationContext(),
                    REQUEST_CODE_RESUME,
                    playIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mRemoteViews.setImageViewResource(R.id.notification_play_pause, android.R.drawable.ic_media_play);
            mRemoteViews.setOnClickPendingIntent(R.id.notification_play_pause, play);
        }

        mRemoteViews.setTextViewText(R.id.notification_title, getNotificationTitle());
        mRemoteViews.setTextViewText(R.id.notification_text, mCurrentSong.getArtistName() + " -> " + mCurrentSong.getName());

        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(), 0,
                new Intent(getApplicationContext(), PlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
//                .setContentTitle(getNotificationTitle())
//                .setContentText(mCurrentSong.getArtistName() + " -> " + mCurrentSong.getName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContent(mRemoteViews)
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

    private ArrayList<Song> mSongs;

    public class PlayerBinder extends Binder implements IPlayerCallbacks {

        @Override
        public void setMusic(ArrayList<Song> songs) {
            mSongs = songs;
        }

        @Override
        public void play(Song song) {
            PlayerService.this.play(song);
        }

        @Override
        public void pause() {
            PlayerService.this.pause();
        }

        @Override
        public void seekTo(int position) {
            PlayerService.this.seekTo(position);
        }

        @Override
        public boolean isPlaying() {
            return false;
        }

        @Override
        public int getSongProgress() {
            return PlayerService.this.getSongProgress();
        }

        @Override
        public int getSongDuration() {
            return PlayerService.this.getSongDuration();
        }
    }

    public interface IPlayerCallbacks {
        void setMusic(ArrayList<Song> songs);
        void play(Song song);
        void pause();
        void seekTo(int position);
        boolean isPlaying();
        int getSongProgress();
        int getSongDuration();
    }
}
