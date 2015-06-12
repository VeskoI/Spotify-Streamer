package com.vesko.android.spotifystreamer.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistsAdapter extends GenericSpotifyAdapter<Artist> {

    public ArtistsAdapter(Context context, int resource, List<Artist> objects) {
        super(context, resource, objects);
    }

     @Override
    public void setItemViews(GenericSpotifyAdapter<Artist>.Tag tag, Artist item) {
        tag.textLine1.setText(item.name);
        tag.textLine2.setVisibility(View.GONE); // not used in Artists list at all

        String imageUrl = getMainImageUrl(item);
        if (TextUtils.isEmpty(imageUrl)) {
            tag.icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(getContext()).load(imageUrl).into(tag.icon);
        }
    }

    @Nullable
    public String getMainImageUrl(Artist artist) {
        if (artist.images.size() > 0) {
            return artist.images.get(0).url;
        }
        return null;
    }

}
