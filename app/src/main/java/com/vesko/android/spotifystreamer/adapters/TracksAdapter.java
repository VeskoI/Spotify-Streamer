package com.vesko.android.spotifystreamer.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class TracksAdapter extends GenericSpotifyAdapter<Track> {

    public TracksAdapter(Context context, int resource, List<Track> objects) {
        super(context, resource, objects);
    }

    @Override
    public void setItemViews(GenericSpotifyAdapter<Track>.Tag tag, Track item) {
        tag.textLine1.setText(item.name);
        tag.textLine2.setText(item.album.name);

        String imageUrl = getMainImageUrl(item);
        if (TextUtils.isEmpty(imageUrl)) {
            tag.icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(getContext()).load(imageUrl).into(tag.icon);
        }
    }

    @Nullable
    public String getMainImageUrl(Track track) {
        if (track.album.images.size() > 0) {
            return track.album.images.get(0).url;
        }
        return null;
    }
}
