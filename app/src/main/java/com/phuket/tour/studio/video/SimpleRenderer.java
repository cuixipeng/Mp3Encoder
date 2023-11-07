package com.phuket.tour.studio.video;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SimpleRenderer implements GLSurfaceView.Renderer {


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //surface被创建后需要做的处理
        //如设置渲染窗口背景颜色
        GLES30.glClearColor(0.0f,0.0f,0.0f,1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // 渲染窗口大小发生改变或者屏幕方法发生变化时候回调
        GLES30.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //执行渲染工作
        //如清空颜色缓冲区
        GLES30.glClearColor(GLES20.GL_COLOR_BUFFER_BIT,0f,0f,0f);


    }
}