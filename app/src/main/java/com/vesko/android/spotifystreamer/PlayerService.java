package com.vesko.android.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vesko.android.spotifystreamer.model.Song;

import java.io.IOException;


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String ACTION_PLAY = "com.vesko.android.spotifystreamer.ACTION_PLAY";
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
    private int mCurrentIndex;
    private Song mCurrentSong;
    private STATE mState = STATE.NON_INITIALISED;

    private Bitmap mNotificationThumb;

    public enum STATE {
        NON_INITIALISED,
        PREPARING,
        PLAYING,
        PAUSED,
        COMPLETED,
    }

    public PlayerService() {
        log("constructor");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand(), action: " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_PLAY:
                int songIdx = SpotifyStreamerApp.getApp().getCurrentSongIdx();
                if (songIdx != -1) {
                    play(songIdx);
                }
                break;

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

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mBinder;
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
        Picasso.with(getApplicationContext()).cancelRequest(mThumbPicassoTarget);

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

            if (mState.equals(STATE.PREPARING) || mState.equals(STATE.PLAYING)) {
                mMediaPlayer.stop();
            }
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
        sendBroadcast(new Intent(ACTION_MUSIC_STATUS_CHANGED).putExtra(Extras.STARTED, started));
    }

    private void notifyPrepareStarted() {
        sendBroadcast(new Intent(ACTION_MUSIC_PREPARE_STARTED));
    }

    private void initMediaPlayer(int songIdx) {
        log("initMediaPlayer, songIdx: " + songIdx);
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

            mNotificationThumb = null;
            mCurrentIndex = index;
            mCurrentSong = song;
            SpotifyStreamerApp.getApp().setCurrentSongIdx(index);
            mState = STATE.PREPARING;

            notifyPrepareStarted();
            refreshOngoingNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshOngoingNotification() {
        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent piNext = PendingIntent.getService(
                getApplicationContext(),
                REQUEST_CODE_NEXT,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(ACTION_PREVIOUS);
        PendingIntent piPrevious = PendingIntent.getService(
                getApplicationContext(),
                REQUEST_CODE_PREVIOUS,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseIntent;
        PendingIntent piPlayPause;
        int iconRes;
        String buttonText;
        if (mState.equals(STATE.PLAYING)) {
            playPauseIntent = new Intent(ACTION_PAUSE);
            piPlayPause = PendingIntent.getService(
                    getApplicationContext(),
                    REQUEST_CODE_PAUSE,
                    playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            iconRes = android.R.drawable.ic_media_pause;
            buttonText = getString(R.string.pause);
        } else {
            playPauseIntent = new Intent(ACTION_RESUME);
            piPlayPause = PendingIntent.getService(
                    getApplicationContext(),
                    REQUEST_CODE_RESUME,
                    playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            iconRes = android.R.drawable.ic_media_play;
            buttonText = getString(R.string.play);
        }

        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(), 0,
                new Intent(getApplicationContext(), PlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getNotificationTitle())
                .setContentText(getString(R.string.song_info, mCurrentSong.getArtistName(), mCurrentSong.getName()))
                .addAction(android.R.drawable.ic_media_previous, getString(R.string.previous), piPrevious)
                .addAction(iconRes, buttonText, piPlayPause)
                .addAction(android.R.drawable.ic_media_next, getString(R.string.next), piNext)
                .setOngoing(true)
                .setContentIntent(pi);

        if (mNotificationThumb != null) {
            builder.setLargeIcon(mNotificationThumb);
        } else {
            Picasso.with(getApplicationContext()).load(mCurrentSong.getAlbumPic()).into(mThumbPicassoTarget);
        }

        boolean notificaitonOnLockscreen = Utils.lockscreenNotificationEnalbed(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(notificaitonOnLockscreen ? Notification.VISIBILITY_PUBLIC : Notification.VISIBILITY_SECRET);
        }

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

    private void log(String message) {
        Log.d(LOG_TAG, message);
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

    private Target mThumbPicassoTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mNotificationThumb = bitmap;
            refreshOngoingNotification();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

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
