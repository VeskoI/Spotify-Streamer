package com.vesko.android.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class Song implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(previewUrl);
        dest.writeString(albumName);
        dest.writeString(albumPic);
        dest.writeString(artistName);
    }

    public static final Parcelable.Creator<Song> CREATOR
            = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private Song(Parcel in) {
        id = in.readString();
        name = in.readString();
        previewUrl = in.readString();
        albumName = in.readString();
        albumPic = in.readString();
        artistName = in.readString();
    }
}
