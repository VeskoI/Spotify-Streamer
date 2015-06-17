package com.vesko.android.spotifystreamer;

import android.app.Application;
import android.util.Log;

import com.vesko.android.spotifystreamer.model.Song;

import java.util.ArrayList;

public class SpotifyStreamerApp extends Application {

    private static SpotifyStreamerApp instance;

    private ArrayList<Song> mSongs = new ArrayList<>();

    public static SpotifyStreamerApp getApp() {
        if (instance == null) {
            instance = new SpotifyStreamerApp();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("vesko", "APP, onCreate()");
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    public void setSongs(ArrayList<Song> songs) {
        Log.d("vesko", "APP, setting songs, size: " + songs.size());
        mSongs = songs;
    }

    public Song getSong(int songIdx) {
        return mSongs.get(songIdx);
    }
}
