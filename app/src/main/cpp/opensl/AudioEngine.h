//
// Created by mac on 10/11/23.
//

#ifndef MP3ENCODER_AUDIOENGINE_H
#define MP3ENCODER_AUDIOENGINE_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <stdio.h>
#include <assert.h>
#include <android/log.h>

class AudioEngine {
public:
    SLObjectItf engineObj;
    SLEngineItf engine;

    SLObjectItf outputMixObj;

private:
    void createEngine() {
        //TODO STEP ONE
        //1.创建引擎并获取引擎接口
        SLresult result = slCreateEngine(&engineObj, 0, NULL, 0, NULL, NULL);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }
        //1.2 初始化引擎
        result = (*engineObj)->Realize(engineObj, SL_BOOLEAN_FALSE);
        if (SL_BOOLEAN_FALSE != result) {
            return;
        }
        //1.3 获取这个引擎对象的方法接口
        result = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engine);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }
        //TODO  STEP TWO 设置混音器
        //2.1  创建需要的对象接口 创建混音器
        result = (*engine)->CreateOutputMix(engine, &outputMixObj, 0, 0, 0);
        if (SL_RESULT_SUCCESS != result) {
            return;
        }
        //2.2初始化混音器
        result = (*outputMixObj)->Realize(outputMixObj, SL_BOOLEAN_FALSE);
        if (SL_BOOLEAN_FALSE != result) {
            return;
        }

    }

    virtual void release() {
        if (outputMixObj) {
            (*outputMixObj)->Destroy(outputMixObj);
            outputMixObj = nullptr;
        }
        if (engineObj) {
            (*engineObj)->Destroy(engineObj);
            engineObj = nullptr;
            engine = nullptr;
        }
    }

public:
    AudioEngine() : engineObj(nullptr), engine(nullptr), outputMixObj(nullptr) {
        createEngine();
    }

    virtual ~AudioEngine() {
        release();
    }

};

#endif //MP3ENCODER_AUDIOENGINE_H
