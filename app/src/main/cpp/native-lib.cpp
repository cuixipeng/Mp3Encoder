#include <jni.h>
#include <string>
#include "include/mp3_encoder.h"
#include "pthread.h"
#include "OpenSLAudioPlay.h"

Mp3Encoder *encoder;
extern "C"
JNIEXPORT jint JNICALL
Java_com_phuket_tour_studio_MainActivity_pcmToMp3JNI(JNIEnv *env, jobject thiz, jstring pcm_path,
                                                     jstring mp3_path, jint sample_rate,
                                                     jint channel, jint bit_rate) {
    const char *pcmPath = env->GetStringUTFChars(pcm_path, NULL);
    const char *mp3Path = env->GetStringUTFChars(mp3_path, NULL);
    encoder = new Mp3Encoder();
    encoder->Init(pcmPath, mp3Path, sample_rate, channel, bit_rate);
    encoder->Encode();
    env->ReleaseStringUTFChars(pcm_path, pcmPath);
    env->ReleaseStringUTFChars(mp3_path, mp3Path);

    return 0;
}
/**
 * 播放cmp文件
 */
FILE *pcmFile = 0;

OpenSLAudioPlay *slAudioPlayer = nullptr;
/**
 * 是否正在播放
 */
bool isPlaying = false;

void *playThreadFunc(void *arg);

void *playThreadFunc(void *arg) {
    const int bufferSize = 2048;
    short buffer[bufferSize];
    while (isPlaying && !feof(pcmFile)) {
        fread(buffer, 1, bufferSize, pcmFile);
        slAudioPlayer->enqueueSample(buffer, bufferSize);
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_phuket_tour_studio_MainActivity_nativePlayPCM(JNIEnv *env, jclass type, jstring pcmPath_) {
    //将 Java 传递过来的 String 转为 C 中的 char *
    const char *_pcmPath = env->GetStringUTFChars(pcmPath_, NULL);

    //如果已经实例化了，就释放资源
    if (slAudioPlayer) {
        slAudioPlayer->release();
        delete slAudioPlayer;
        slAudioPlayer = nullptr;
    }
    //实例化 OpenSLAudioPlay
    slAudioPlayer = new OpenSLAudioPlay(44100, SAMPLE_FORMAT_16, 1);
    slAudioPlayer->init();
    pcmFile = fopen(_pcmPath, "r");
    isPlaying = true;
    pthread_t playThread;
    pthread_create(&playThread, nullptr, playThreadFunc, 0);

    env->ReleaseStringUTFChars(pcmPath_, _pcmPath);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_phuket_tour_studio_MainActivity_nativeStopPcm(JNIEnv *env, jclass type) {
    isPlaying = false;
    if (slAudioPlayer) {
        slAudioPlayer->release();
        delete slAudioPlayer;
        slAudioPlayer = nullptr;
    }
    if (pcmFile) {
        fclose(pcmFile);
        pcmFile = nullptr;
    }
}



