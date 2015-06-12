package com.vesko.android.spotifystreamer.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.R;
import com.vesko.android.spotifystreamer.model.Song;

import java.util.List;

public class SongsAdapter extends GenericSpotifyAdapter<Song> {

    public SongsAdapter(Context context, int resource, List<Song> objects) {
        super(context, resource, objects);
    }

    @Override
    public void setItemViews(GenericSpotifyAdapter<Song>.Tag tag, Song song) {
        tag.textLine1.setText(song.getName());
        tag.textLine2.setText(song.getAlbumName());

        String imageUrl = getMainImageUrl(song);
        if (TextUtils.isEmpty(imageUrl)) {
            tag.icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(getContext()).load(imageUrl).into(tag.icon);
        }
    }

    @Nullable
    public String getMainImageUrl(Song song) {
        return song.getAlbumPic();
    }
}
