#include <iostream>
#include <vector>
#include <set>
extern "C" {
    #include <libavformat/avformat.h>
    #include <libavcodec/avcodec.h>
    #include <string.h>
}

bool enumCodecs(std::string str) {
    // enumerate all codecs and put into list
    std::vector<const AVCodec*> encoderList;

    void* codecState = nullptr; //start with null
    const AVCodec* codec = av_codec_iterate(&codecState);
    while (codec) {
        encoderList.push_back(codec);
        codec = av_codec_iterate(&codecState);
    }

    // enumerate all containers available for muxing
    void* muxerState = nullptr;
    const AVOutputFormat* ofmt = av_muxer_iterate(&muxerState);
    while (ofmt) {
        for (auto codec : encoderList) {
            // check if codec can be used in this container
            if (avformat_query_codec(ofmt, codec->id, FF_COMPLIANCE_NORMAL) == 1) {
                // check for video codecs here
                if (avcodec_get_type(codec->id) == AVMEDIA_TYPE_VIDEO && strcmp(str.c_str(), avcodec_get_name(codec->id)) == 0) {
                    return true;
                }
            }
        }
        // get next format
        ofmt = av_muxer_iterate(&muxerState);
    }

    return false;
}

int main(int argc, char** argv) {
    bool enumCodec = enumCodecs(argv[1]);
    if(enumCodec) {
        return 0;
    }
    return 1;
}
