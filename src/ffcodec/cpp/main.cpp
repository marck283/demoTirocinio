#include <iostream>
#include <string>
extern "C" {
    #include <libavutil/imgutils.h>
    #include <libavutil/pixfmt.h>
};

AVPixelFormat getPixFmt(const std::string& pix_fmt) {
    if(pix_fmt == "yuv420p") {
        return AV_PIX_FMT_YUV420P;
    }
    if(pix_fmt == "yuyv422") {
        return AV_PIX_FMT_YUYV422;
    }
    if(pix_fmt == "rgb24") {
        return AV_PIX_FMT_RGB24;
    }
    if(pix_fmt == "bgr24") {
        return AV_PIX_FMT_BGR24;
    }
    if(pix_fmt == "yuv422p") {
        return AV_PIX_FMT_YUV422P;
    }
    if(pix_fmt == "yuv444p") {
        return AV_PIX_FMT_YUV444P;
    }
    if(pix_fmt == "yuv410p") {
        return AV_PIX_FMT_YUV410P;
    }
    if(pix_fmt == "yuv411p") {
        return AV_PIX_FMT_YUV411P;
    }
    if(pix_fmt == "gray8") {
        return AV_PIX_FMT_GRAY8;
    }
    if(pix_fmt == "monowhite") {
        return AV_PIX_FMT_MONOWHITE;
    }
    if(pix_fmt == "monoblack") {
        return AV_PIX_FMT_MONOBLACK;
    }
    if(pix_fmt == "pal8") {
        return AV_PIX_FMT_PAL8;
    }
    if(pix_fmt == "yuvj420p") {
        return AV_PIX_FMT_YUVJ420P;
    }
    if(pix_fmt == "yuvj422p") {
        return AV_PIX_FMT_YUVJ422P;
    }
    if(pix_fmt == "yuvj444p") {
        return AV_PIX_FMT_YUVJ444P;
    }
    if(pix_fmt == "uyvy422") {
        return AV_PIX_FMT_UYVY422;
    }
    if(pix_fmt == "uyyvyy411") {
        return AV_PIX_FMT_UYYVYY411;
    }
    if(pix_fmt == "bgr8") {
        return AV_PIX_FMT_BGR8;
    }
    if(pix_fmt == "bgr4") {
        return AV_PIX_FMT_BGR4;
    }
    if(pix_fmt == "bgr4_byte") {
        return AV_PIX_FMT_BGR4_BYTE;
    }
    if(pix_fmt == "rgb8") {
        return AV_PIX_FMT_RGB8;
    }
    if(pix_fmt == "rgb4") {
        return AV_PIX_FMT_RGB4;
    }
    if(pix_fmt == "rgb4_byte") {
        return AV_PIX_FMT_RGB4_BYTE;
    }
    if(pix_fmt == "nv12") {
        return AV_PIX_FMT_NV12;
    }
    if(pix_fmt == "nv21") {
        return AV_PIX_FMT_NV21;
    }
    if(pix_fmt == "argb") {
        return AV_PIX_FMT_ARGB;
    }
    if(pix_fmt == "rgba") {
        return AV_PIX_FMT_RGBA;
    }
    if(pix_fmt == "abgr") {
        return AV_PIX_FMT_ABGR;
    }
    if(pix_fmt == "bgra") {
        return AV_PIX_FMT_BGRA;
    }
    if(pix_fmt == "gray16be") {
        return AV_PIX_FMT_GRAY16BE;
    }
    if(pix_fmt == "gray16le") {
        return AV_PIX_FMT_GRAY16LE;
    }
    if(pix_fmt == "yuv440p") {
        return AV_PIX_FMT_YUV440P;
    }
    if(pix_fmt == "yuvj440p") {
        return AV_PIX_FMT_YUVJ440P;
    }
    if(pix_fmt == "yuva420p") {
        return AV_PIX_FMT_YUVA420P;
    }
    if(pix_fmt == "rgb48be") {
        return AV_PIX_FMT_RGB48BE;
    }
    if(pix_fmt == "rgb48le") {
        return AV_PIX_FMT_RGB48LE;
    }
    if(pix_fmt == "rgb565be") {
        return AV_PIX_FMT_RGB565BE;
    }
    if(pix_fmt == "rgb565le") {
        return AV_PIX_FMT_RGB565LE;
    }
    if(pix_fmt == "rgb555be") {
        return AV_PIX_FMT_RGB555BE;
    }
    if(pix_fmt == "rgb555le") {
        return AV_PIX_FMT_RGB555LE;
    }
    if(pix_fmt == "bgr565be") {
        return AV_PIX_FMT_BGR565BE;
    }
    if(pix_fmt == "bgr565le") {
        return AV_PIX_FMT_BGR565LE;
    }
    if(pix_fmt == "bgr555be") {
        return AV_PIX_FMT_BGR555BE;
    }
    if(pix_fmt == "bgr555le") {
        return AV_PIX_FMT_BGR555LE;
    }
    if(pix_fmt == "vaapi_moco" || pix_fmt == "vappi_idct" || pix_fmt == "vaapi_vld") {
        return AV_PIX_FMT_VAAPI;
    }
    if(pix_fmt == "yuv420p16le") {
        return AV_PIX_FMT_YUV420P16LE;
    }
    if(pix_fmt == "yuv420p16be") {
        return AV_PIX_FMT_YUV420P16BE;
    }
    if(pix_fmt == "yuv422p16le") {
        return AV_PIX_FMT_YUV422P16LE;
    }
    if(pix_fmt == "yuv422p16be") {
        return AV_PIX_FMT_YUV422P16BE;
    }
    if(pix_fmt == "yuv444p16le") {
        return AV_PIX_FMT_YUV444P16LE;
    }
    if(pix_fmt == "yuv444p16be") {
        return AV_PIX_FMT_YUV444P16BE;
    }
    return AV_PIX_FMT_NONE;
}

int main(int argc, char** argv) {
    int w = std::stoi(argv[1]);
    int h = std::stoi(argv[2]);
    int res = av_image_check_size2(w, h, w*h, getPixFmt(argv[3]), 0, nullptr);
    if(res >= 0) {
        return 0;
    }
    return 1;
}