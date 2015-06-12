package com.vesko.android.spotifystreamer.model;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class Song {

    String id;
    String name;
    String previewUrl;
    String albumName;
    String albumPic;
    String artistName;

    public Song(Track track) {
        this.id = track.id;
        this.name = track.name;
        this.previewUrl = track.preview_url;

        if (track.album != null) {
            this.albumName = track.album.name;
            this.albumPic = getBiggestAlbumPictureUrl(track);
        }

        this.artistName = getArtistsNames(track);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumPic() {
        return albumPic;
    }

    public String getArtistName() {
        return artistName;
    }

    private String getArtistsNames(Track track) {
        StringBuilder sb = new StringBuilder();
        for (ArtistSimple artist : track.artists) {
            // NOTE to code-reviewer - my dilemma here was whether appending an extra comma and
            // trimming it in the end is faster than adding an additional if statement here.
            sb.append(artist.name).append(",");
        }

        return sb.substring(0, sb.length() - 1);
    }

    private String getBiggestAlbumPictureUrl(Track track) {
        List<Image> images = track.album.images;
        if (images == null || images.size() == 0) {
            return null;
        }

        Image biggest = images.get(0);
        for (int i = 1; i < images.size(); i++) {
            Image img = images.get(i);
            if (img.height > biggest.height) {
                biggest = img;
            }
        }

        return biggest.url;
    }
}
