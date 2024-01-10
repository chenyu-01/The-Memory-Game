package com.team01.thememorygame.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.team01.thememorygame.R;



public class CardViewHolder {
    public ImageView mIvImage;
    public RelativeLayout mRlBg;
    public ImageView mIvRect;
    public int realIndex;
    public boolean isFirst;
    public int position = -1;
    public boolean isShowing = true;

    public CardViewHolder(View itemView) {
        mIvImage = (ImageView) itemView.findViewById(R.id.mIvImage);
        mRlBg = (RelativeLayout) itemView.findViewById(R.id.mRlBg);
        mIvRect = (ImageView) itemView.findViewById(R.id.rect_iv);
    }
}