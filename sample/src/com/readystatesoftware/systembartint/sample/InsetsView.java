package com.readystatesoftware.systembartint.sample;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by jgilfelt on 03/04/2014.
 */
public class InsetsView extends FrameLayout {

    public InsetsView(Context context) {
        super(context);
    }

    public InsetsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InsetsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {

        Log.i("InsetsView", "--------------------");
        Log.i("InsetsView", "inset top = " + insets.top);
        Log.i("InsetsView", "inset left = " + insets.left);
        Log.i("InsetsView", "inset right = " + insets.right);
        Log.i("InsetsView", "inset bottom = " + insets.bottom);

        return super.fitSystemWindows(insets);
    }
}
