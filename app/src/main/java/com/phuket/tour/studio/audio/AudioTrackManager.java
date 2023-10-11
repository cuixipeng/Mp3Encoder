package com.phuket.tour.studio.audio;

import static android.media.AudioTrack.PLAYSTATE_PLAYING;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音频播放
 */
public class AudioTrackManager {
    private static final String TAG = "AudioTrackManager";
    private AudioTrack mAudioTrack;
    private volatile Status mStatus = Status.STATUS_NO_READY;

    private String mFilePath;


    //音频流类型
    private static final int mStreamType = AudioManager.STREAM_MUSIC; //音乐声
    //指定采样率（MediaRecorder 的采样率通常是8000Hz AAC是44100Hz  设置采样率为44100Hz 目前为常用采样率 官方文档表示这个值兼容所有设置 ）
    private static final int mSampleRateInHz = 44100;
    //声道数（通道数）的配置
    private static final int mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;//单声道
    //采样格式
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //AudioTrack内部音频缓冲区大小
    private int mBufferSizeInBytes;

    //单任务线程池
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private Context mContext;

    public AudioTrackManager(Context context) {
        this.mContext = context;
    }

    public void createAudioTrack(String filePath) throws IllegalStateException {
        mFilePath = filePath;
        //内部的音频缓冲区的大 小
        mBufferSizeInBytes = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
        if (mBufferSizeInBytes <= 0) {
            throw new IllegalStateException("AudioTrack is not available " + mBufferSizeInBytes);
        }
        //初始化AudioTrack
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(mAudioFormat)
                            .setSampleRate(mSampleRateInHz)
                            .setChannelMask(mChannelConfig)
                            .build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(mBufferSizeInBytes)
                    .build();
        } else {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRateInHz, mChannelConfig, mAudioFormat,
                    mBufferSizeInBytes, AudioTrack.MODE_STREAM);
        }
        mStatus = Status.STATUS_READY;
    }


    /**
     * 播放
     */
    public void play() throws IllegalStateException{
        if (mStatus == Status.STATUS_NO_READY || mAudioTrack == null) {
            throw new IllegalStateException("播放器尚未初始化");
        }
        if (mStatus == Status.STATUS_START) {
            throw new IllegalStateException("正在播放...");
        }
        mExecutorService.execute(() -> {
            try {
                playAudioData();
            } catch (IOException e) {
                mMainHandler.post(() -> Toast.makeText(mContext, "播放出错", Toast.LENGTH_SHORT).show());
//                throw new RuntimeException(e);
            }
        });
        mStatus = Status.STATUS_START;

    }

    private void playAudioData() throws IOException {
        InputStream dis = null;
        try {
            mMainHandler.post(() -> Toast.makeText(mContext, "播放开始", Toast.LENGTH_SHORT).show());
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(mFilePath)));
            byte[] bytes = new byte[mBufferSizeInBytes];
            int length;
            //在当前播放实例是否初始化成功，如果处于初始化成功的状态并且未播放的状态那么就调用play
            if (null != mAudioTrack
                    && mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED
                    && mAudioTrack.getPlayState() != PLAYSTATE_PLAYING) {
                mAudioTrack.play();
                //write 是阻塞方法
                while ((length = dis.read(bytes)) != -1 && mStatus == Status.STATUS_START) {
                    mAudioTrack.write(bytes, 0, length);
                }
                mMainHandler.post(() -> Toast.makeText(mContext, "播放结束", Toast.LENGTH_SHORT).show());
            }
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }

    /**
     * 停止
     */
    public void stop() throws IllegalStateException {
        if (mStatus == Status.STATUS_NO_READY || mStatus == Status.STATUS_READY) {
            Log.d(TAG, "播放尚未开始");
        } else {
            mStatus = Status.STATUS_STOP;
            mAudioTrack.stop();
            release();
        }
    }

    public void release() {
        Log.d(TAG, "==release===");
        mStatus = Status.STATUS_NO_READY;
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    /**
     * 播放对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //播放
        STATUS_START,
        //停止
        STATUS_STOP
    }

}
