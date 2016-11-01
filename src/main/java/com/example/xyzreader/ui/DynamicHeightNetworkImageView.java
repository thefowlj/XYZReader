package com.example.xyzreader.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;
import com.example.xyzreader.util.Utils;

public class DynamicHeightNetworkImageView extends NetworkImageView {
    private float mAspectRatio = 1.5f;

    private View mView = null;

    public DynamicHeightNetworkImageView(Context context) {
        super(context);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }

    /**
     * When the image is retrieved successfully, this method will be called and will allow the
     * dominant color of the image to be determined.
     * @param bm image that is loaded or set manually
     */
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        if(mView != null ) {
            int c = Utils.getDominantColorFromBitmap(bm);
            c = Utils.alphaColor(Utils.LIGHTEN_ALPHA, c);
            mView.setBackgroundColor(c);
        }
    }

    public void setViewToColorize(View view) {
        mView = view;
    }
}
