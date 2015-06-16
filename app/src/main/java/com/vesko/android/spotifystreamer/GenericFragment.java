package com.vesko.android.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public abstract class GenericFragment extends Fragment {

    protected SpotifyService spotify;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSpotify();
    }

    private void initSpotify() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
    }
}
