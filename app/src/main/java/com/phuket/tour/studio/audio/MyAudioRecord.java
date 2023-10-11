package com.phuket.tour.studio.audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyAudioRecord {
    private static final String TAG = "MyAudioRecord";
    /**
     * 默认录音远
     */
    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * 采样率 使用常用的 44100
     */
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    /**
     * 通道
     */
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 采样格式为 16 bit
     */
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;
    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;
    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    public interface OnAudioFrameCapturedListener {
        public void onAudioFrameCaptured(byte[] audioData);
    }

    public boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    public boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);
    }

    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {

        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.d(TAG, "getMinBufferSize = " + mMinBufferSize + " bytes !");
        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        mAudioRecord.startRecording();

        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        mIsCaptureStarted = true;

        Log.d(TAG, "Start audio capture success !");

        return true;
    }

    public void stopCapture() {

        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;

        Log.d(TAG, "Stop audio capture success !");
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {

            while (!mIsLoopExit) {

                byte[] buffer = new byte[mMinBufferSize];

                int ret = mAudioRecord.read(buffer, 0, mMinBufferSize);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG , "Error ERROR_INVALID_OPERATION");
                }
                else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG , "Error ERROR_BAD_VALUE");
                }
                else {
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                    Log.d(TAG , "OK, Captured "+ret+" bytes !");
                }

            }
        }
    }


    public static void writePCM(byte[] frme) {
        try {
            String filePath = Environment.getExternalStorageDirectory() + "/_test.pcm";
            File file = new File(filePath);
            FileOutputStream fos = null;
            if (!file.exists()) {
                file.createNewFile();//如果文件不存在，就创建该文件
                fos = new FileOutputStream(file);//首次写入获取
            } else {
                //如果文件已存在，那么就在文件末尾追加写入
                fos = new FileOutputStream(file, true);//这里构造方法多了一个参数true,表示在文件末尾追加写入
            }
            fos.write(frme);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
