package com.team01.thememorygame;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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

    private static  class ViewHolder{
        ImageView imageView;
        ImageView selectImageView;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_view, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.grid_image);
            viewHolder.selectImageView = convertView.findViewById(R.id.select_image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.get().load(imageUrls.get(position).getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.noimage)
                .fit()
                .centerCrop()
                .into(viewHolder.imageView);

        // Update the visibility of the select image view based on the selection state
        viewHolder.selectImageView.setVisibility(selectedPositions.contains(position) ? View.VISIBLE : View.GONE);

        return convertView;
    }


//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView imageView;
//
//        if (inflater == null) {
//            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        }
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.grid_view, parent, false);
//        }
//
//        imageView = convertView.findViewById(R.id.grid_image);
//        ImageView selectImageView = convertView.findViewById(R.id.select_image);
//
//
//
//        Picasso.get().load(imageUrls.get(position).getImageUrl())
//                .placeholder(R.drawable.loading)
//                .error(R.drawable.noimage)
//                .fit()
//                .centerCrop()
//                .into(imageView);
//
//        if(selectedPositions.contains(position)) { // Todo: change this to a better way
//            selectImageView.setVisibility(View.VISIBLE);
//        } else {
//            selectImageView.setVisibility(View.GONE);
//        }
//        return imageView;
//    }


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

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }
}