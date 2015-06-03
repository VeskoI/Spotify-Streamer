package com.vesko.android.spotifystreamer.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vesko.android.spotifystreamer.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistsAdapter extends ArrayAdapter<Artist> {

    public ArtistsAdapter(Context context, int resource, List<Artist> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.generic_sporify_list_item, null);
            Tag tag = new Tag();
            tag.icon = (ImageView) convertView.findViewById(R.id.list_item_icon);
            tag.textLine1 = (TextView) convertView.findViewById(R.id.list_item_text_line_1);
            tag.textLine2 = (TextView) convertView.findViewById(R.id.list_item_text_line_2);
            convertView.setTag(tag);
        }

        Tag tag = (Tag) convertView.getTag();
        Artist artist = getItem(position);

        setArtistIcon(tag, artist);
        tag.textLine1.setText(artist.name);
        tag.textLine2.setVisibility(View.GONE); // not used in Artists list at all

        return convertView;
    }

    private void setArtistIcon(Tag tag, Artist artist) {
        String imageUrl = getMainImageUrl(artist);
        if (TextUtils.isEmpty(imageUrl)) {
            tag.icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(getContext()).load(imageUrl).into(tag.icon);
        }
    }

    @Nullable
    private String getMainImageUrl(Artist artist) {
        if (artist.images.size() > 0) {
            return artist.images.get(0).url;
        }
        return null;
    }

    private class Tag {
        ImageView icon;
        TextView textLine1;
        TextView textLine2;
    }
}
