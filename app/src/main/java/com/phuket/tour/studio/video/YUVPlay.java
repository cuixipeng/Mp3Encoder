package com.phuket.tour.studio.video;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class YUVPlay extends GLSurfaceView implements SurfaceHolder.Callback {

    public YUVPlay(Context context) {
        this(context,null);
    }

    public YUVPlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

    }

}
