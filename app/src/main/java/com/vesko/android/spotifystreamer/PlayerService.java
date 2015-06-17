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


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String ACTION_PAUSE = "com.vesko.android.spotifystreamer.ACTION_PAUSE";
    public static final String ACTION_RESUME = "com.vesko.android.spotifystreamer.ACTION_RESUME";
    public static final String ACTION_NEXT = "com.vesko.android.spotifystreamer.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.vesko.android.spotifystreamer.ACTION_PREVIOUS";

    public static final String ACTION_MUSIC_STATUS_CHANGED = "com.vesko.android.spotifystreamer.ACTION_MUSIC_STATUS_CHANGED";
    public static final String ACTION_MUSIC_PREPARE_STARTED = "com.vesko.android.spotifystreamer.ACTION_MUSIC_PREPARE_STARTED";

    private static final int NOTIFICATION_ID = 1441;

    private static final int REQUEST_CODE_PAUSE = 2001;
    private static final int REQUEST_CODE_RESUME = 2002;
    private static final int REQUEST_CODE_NEXT = 2003;
    private static final int REQUEST_CODE_PREVIOUS = 2004;

    private final PlayerBinder mBinder = new PlayerBinder();
    private MediaPlayer mMediaPlayer;
    private RemoteViews mRemoteViews;
    private int mCurrentIndex;
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
                changeTrack(false);
                break;

            case ACTION_NEXT:
                changeTrack(true);
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
        notifyMusicStatusChanged(false);
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mState = STATE.NON_INITIALISED;
        }
    }

    private void play(int songIdx) {
        if (mMediaPlayer == null) {
            initMediaPlayer(songIdx);
        }

        if (songIdx == mCurrentIndex && mState.equals(STATE.PAUSED)) {
            // Same song, just resume it
            startPlayback();
        }

        if (songIdx != mCurrentIndex) {
            // Play a new song
            mMediaPlayer.stop();
            mMediaPlayer.reset();

            changeMediaPlayerSource(songIdx);
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
            notifyMusicStatusChanged(false);
        }
    }

    private void changeTrack(boolean forward) {
        int otherSong = getOtherSong(forward);
        play(otherSong);
    }

    private int getOtherSong(boolean forward) {
        int otherSong = mCurrentIndex;

        if (forward) {
            if (mCurrentIndex == SpotifyStreamerApp.getApp().getSongs().size() - 1) {
                otherSong = 0;
            } else {
                otherSong++;
            }
        } else {
            if (mCurrentIndex== 0) {
                otherSong = SpotifyStreamerApp.getApp().getSongs().size() - 1;
            } else {
                otherSong--;
            }
        }

        return otherSong;
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

        notifyMusicStatusChanged(true);
    }

    private void notifyMusicStatusChanged(boolean started) {
        sendBroadcast(
                new Intent(ACTION_MUSIC_STATUS_CHANGED)
                        .putExtra(Extras.STARTED, started)
                        .putExtra(Extras.TRACK_IDX, mCurrentIndex));
    }

    private void notifyPrepareStarted() {
        sendBroadcast(
                new Intent(ACTION_MUSIC_PREPARE_STARTED).putExtra(Extras.TRACK_IDX, mCurrentIndex));
    }

    private void initMediaPlayer(int songIdx) {
        log("initMediaPlayer ...");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        changeMediaPlayerSource(songIdx);
    }

    private void changeMediaPlayerSource(int index) {
        try {
            Song song = SpotifyStreamerApp.getApp().getSong(index);
            mMediaPlayer.setDataSource(song.getPreviewUrl());
            mMediaPlayer.prepareAsync();

            mCurrentIndex = index;
            mCurrentSong = song;
            mState = STATE.PREPARING;

            notifyPrepareStarted();
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

    public class PlayerBinder extends Binder implements IPlayerCallbacks {

        @Override
        public void play(int songIdx) {
            PlayerService.this.play(songIdx);
        }

        @Override
        public void pause() {
            PlayerService.this.pause();
        }

        @Override
        public void changeTrack(boolean forward) {
            PlayerService.this.changeTrack(forward);
        }

        @Override
        public void seekTo(int position) {
            PlayerService.this.seekTo(position);
        }

        @Override
        public boolean isPlaying() {
            return PlayerService.this.isPlaying();
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
        void play(int songIdx);
        void pause();
        void changeTrack(boolean forward);
        void seekTo(int position);
        boolean isPlaying();
        int getSongProgress();
        int getSongDuration();
    }
}
