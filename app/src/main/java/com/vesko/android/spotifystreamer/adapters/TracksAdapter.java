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

import kaaes.spotify.webapi.android.models.Track;

public class TracksAdapter extends ArrayAdapter<Track> {

    public TracksAdapter(Context context, int resource, List<Track> objects) {
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
        Track track = getItem(position);

        setTrackIcon(tag, track);
        tag.textLine1.setText(track.name);
        tag.textLine2.setText(track.album.name);

        return convertView;
    }

    private void setTrackIcon(Tag tag, Track track) {
        String imageUrl = getMainImageUrl(track);
        if (TextUtils.isEmpty(imageUrl)) {
            tag.icon.setImageResource(R.mipmap.ic_launcher);
        } else {
            Picasso.with(getContext()).load(imageUrl).into(tag.icon);
        }
    }

    @Nullable
    private String getMainImageUrl(Track track) {
        if (track.album.images.size() > 0) {
            return track.album.images.get(0).url;
        }
        return null;
    }

    private class Tag {
        ImageView icon;
        TextView textLine1;
        TextView textLine2;
    }
}
