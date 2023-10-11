package com.phuket.tour.studio;

import static com.phuket.tour.studio.audio.MyAudioRecord.writePCM;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.phuket.tour.studio.audio.AudioTrackManager;
import com.phuket.tour.studio.audio.MyAudioRecord;
import com.phuket.tour.studio.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("lame");
    }

    private ActivityMainBinding binding;
    private MyAudioRecord audioRecord;
    //PCM播放实例
    private AudioTrackManager mAudioTracker;
    private final String PATH = Environment.getExternalStorageDirectory() + "/_test.pcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO
            }, 0);
        }

        mAudioTracker = new AudioTrackManager(this);
        audioRecord = new MyAudioRecord();

        audioRecord.setOnAudioFrameCapturedListener(MyAudioRecord::writePCM);
    }

    /**
     * 调用native代码完成pcm文件转成mp3
     */

    public void pcm2mp3(View view) {
        String pcmPath = getPCMFile().getAbsolutePath();
        String mp3Path = getMp3File().getAbsolutePath();
        int sampleRate = 44100;
        int channel = 2;
        int bitRate = 64000;
        int ret = pcmToMp3JNI(pcmPath, mp3Path, sampleRate, channel, bitRate);
        Toast.makeText(this, ret + "", Toast.LENGTH_SHORT).show();
    }

    /**
     * 开始录制PCM音频文件
     */
    public void startRecordPcm(View view) {
        audioRecord.startCapture();
    }

    public void stopRecordPcm(View view) {
        audioRecord.stopCapture();
    }

    private File getPCMFile() {
        File root = Environment.getExternalStorageDirectory();
        return new File(root, "_test.pcm");
    }

    private File getMp3File() {
        File root = Environment.getExternalStorageDirectory();
        return new File(root, "_test.mp3");
    }


    public void playPCM(View view) {
        mAudioTracker.createAudioTrack(PATH);
        mAudioTracker.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAudioTracker.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioTracker.release();
    }


    public native int pcmToMp3JNI(String pcmPath, String mp3Path, int sampleRate,
                                  int channel, int bitRate);


}