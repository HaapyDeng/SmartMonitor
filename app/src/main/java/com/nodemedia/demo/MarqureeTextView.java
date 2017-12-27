package com.nodemedia.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView的跑马灯效果.
 */
public class MarqureeTextView extends TextView {
    public MarqureeTextView(Context context) {
        super(context);
    }

    public MarqureeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqureeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public MarqureeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            super.onWindowFocusChanged(focused);
        }
    }

    @Override
    public boolean isFocused() {
        return true;

    }
}