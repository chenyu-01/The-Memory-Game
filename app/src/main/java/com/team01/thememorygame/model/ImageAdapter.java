package com.team01.thememorygame.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.team01.thememorygame.R;
import com.team01.thememorygame.model.ImageModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<ImageModel> imageUrls;

    private Set<Integer> selectedPositions;

    LayoutInflater inflater;

    public ImageAdapter(Context context,List <ImageModel> imageUrls){
        this.context=context;
        this.imageUrls=imageUrls;
        this.selectedPositions= new HashSet<>();
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public ImageModel getItem(int position) {
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



        Picasso.get().load(imageUrls.get(position).getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.noimage)
                .fit()
                .centerCrop()
                .into(imageView);

        imageView.setActivated(selectedPositions.contains(position));
        return imageView;
    }


    public void setImageDrawable(Drawable placeHolderDrawable) {
    }


    public void toggleSelection(int position) {
        if(selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyDataSetChanged();
    }

}