package com.phuket.tour.studio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.phuket.tour.studio.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("lame");
    }

    private ActivityMainBinding binding;
    private AudioRecord audioRecord;
    private String pcmFilePath;
    private int bufferSize = 1024;
    private boolean isRecord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, 0);
        }
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
    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    public void startRecordPcm(View view) {
        int frequency = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                frequency,
                channelConfig,
                audioEncoding,
                bufferSize);
        pcmFilePath = getPCMFile().getAbsolutePath();
        isRecord = true;
        new RecordThread().start();
    }

    public void stopRecordPcm(View view) {
        isRecord = false;
    }

    private File getPCMFile() {
        File root = getExternalFilesDir(null);
        File csvDir = new File(root, "/audio/");
        if (!csvDir.exists()) {
            // 创建csv 目录
            csvDir.mkdir();
        }
        return new File(csvDir, "sing.pcm");
    }

    private File getMp3File() {
        File root = getExternalFilesDir(null);
        File csvDir = new File(root, "/audio/");
        if (!csvDir.exists()) {
            // 创建csv 目录
            csvDir.mkdir();
        }
        return new File(csvDir, "sing.mp3");
    }


    /**
     * 录制PCM音频线程
     */
    class RecordThread extends Thread {
        @Override
        public void run() {
            audioRecord.startRecording();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(pcmFilePath);
                byte[] bytes = new byte[bufferSize];
                Toast.makeText(MainActivity.this, "录制中", Toast.LENGTH_SHORT).show();
                while (isRecord) {
                    audioRecord.read(bytes, 0, bytes.length);
                    fos.write(bytes, 0, bytes.length);
                    fos.flush();
                }
                Log.d("TAG", "停止录制");
                audioRecord.stop();
                fos.flush();
            } catch (Exception e) {
                Log.d("TAG", "exception: " + e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    public native int pcmToMp3JNI(String pcmPath, String mp3Path, int sampleRate,
                                  int channel, int bitRate);
}