package com.thunsaker.soup.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class EventedScrollView extends ScrollView {
    private OnScrollListener mOnScrollListener;

    public EventedScrollView(Context context) {
        super(context);
    }

    public EventedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EventedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface OnScrollListener {
        void onScrollChanged(EventedScrollView v, int x, int y, int oldx, int oldy);
    }

    public void setOnScrollViewListener(OnScrollListener l) {
        this.mOnScrollListener = l;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(mOnScrollListener != null) {
            mOnScrollListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}
