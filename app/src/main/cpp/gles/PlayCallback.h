//
// Created by mac on 11/7/23.
//

#ifndef MP3ENCODER_PLAYCALLBACK_H
#define MP3ENCODER_PLAYCALLBACK_H
#include <jni.h>

class PlayCallback {
public:
    PlayCallback(JavaVM *javaVM, JNIEnv *env, jobject job);
    ~PlayCallback();

    void onSucceed(const char *);

    void onError(const char *);

    void toJavaMessage(const char *message);


private:
    JavaVM *javaVm = 0;
    JNIEnv *env = 0;
    jobject instance;

    jmethodID jmd_showMessage;

};
#endif //MP3ENCODER_PLAYCALLBACK_H
