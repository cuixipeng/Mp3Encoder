#ifndef LAME_MP3ENCODER_H
#define LAME_MP3ENCODER_H

#include "stdio.h"
#include "lame/lame.h"
#include "SLES/OpenSLES.h"
class Mp3Encoder {
private:
    FILE *pcmFile;
    FILE *mp3File;
    lame_t lameClient;
public:
    Mp3Encoder();

    ~Mp3Encoder();

    int Init(const char *pcmFilePath, const char *mp3FilePath, int simpleRate, int channel,
             int bitRate);

    void Encode();

    void Destroy();
};

#endif //LAME_MP3ENCODER_H