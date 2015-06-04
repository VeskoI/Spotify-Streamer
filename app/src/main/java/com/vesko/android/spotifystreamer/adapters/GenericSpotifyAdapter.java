package com.vesko.android.spotifystreamer.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vesko.android.spotifystreamer.R;

import java.util.List;

public abstract class GenericSpotifyAdapter<T> extends ArrayAdapter<T> {

    public abstract void setItemViews(Tag tag, T item);

    @Nullable
    public abstract String getMainImageUrl(T item);

    public GenericSpotifyAdapter(Context context, int resource, List<T> objects) {
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
        T item = getItem(position);

        setItemViews(tag, item);

        return convertView;
    }

    protected class Tag {
        ImageView icon;
        TextView textLine1;
        TextView textLine2;
    }
}
