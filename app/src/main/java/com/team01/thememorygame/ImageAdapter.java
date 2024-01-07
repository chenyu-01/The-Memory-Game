package com.team01.thememorygame;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<ImageModel> imageUrls;

    LayoutInflater inflater;

    public ImageAdapter(Context context,List <ImageModel> imageUrls){
        this.context=context;
        this.imageUrls=imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_view, parent, false);
        }

        imageView = convertView.findViewById(R.id.grid_image);

        // Now you can use Picasso to load the image into the ImageView
        Picasso.get().load(imageUrls.get(position).getImageUrl())
//                .placeholder(R.drawable.noimage)
                .into(imageView);

        return imageView;
    }


}