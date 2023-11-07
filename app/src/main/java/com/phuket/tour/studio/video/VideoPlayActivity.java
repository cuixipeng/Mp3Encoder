package com.phuket.tour.studio.video;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.phuket.tour.studio.R;


public class VideoPlayActivity extends Activity {
    private YUVPlay mYUVPlay;
    static {
        System.loadLibrary("lame");
    }
    /**
     * 播放 path
     */
    private final String PATH = Environment.getExternalStorageDirectory() + "/yuvtest.yuv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mYUVPlay = findViewById(R.id.surface);
        mYUVPlay.setEGLContextClientVersion(3);
        mYUVPlay.setRenderer(new SimpleRenderer());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mYUVPlay.onResume();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mYUVPlay.onDestory();
//        mYUVPlay = null;
//    }

    @Override
    protected void onPause() {
        super.onPause();
        mYUVPlay.onPause();
    }

    /**
     * OpenGL ES 播放 YUV
     *
     * @param view
     */
    public void gles_play(View view) {
        glesPlay(PATH, mYUVPlay.getHolder().getSurface());
    }

    /**
     * nativeWindow 播放 YUV
     *
     * @param view
     */
    public void native_window_play(View view) {
        nativeWindowPlay(PATH, mYUVPlay.getHolder().getSurface());
    }
    public void glesPlay(final String yuv420pPath, final Object surface) {
        this.yuv420pPath = yuv420pPath;
        this.surface = surface;

        Thread thread = new Thread(playRunnable);
        thread.start();

    }

    public native void nativeGlesPlay(String yuv420pPath, Object surface);

    public native void nativeWindowPlay(String yuv420pPath, Object surface);

    /**
     * JNI 调用 原生
     *
     * @param message
     */
    public void showMessage(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoPlayActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public native void onDestory();

    private String yuv420pPath;
    private Object surface;
    private Runnable playRunnable = new Runnable() {
        @Override
        public void run() {
            nativeGlesPlay(yuv420pPath, surface);
        }
    };
}
