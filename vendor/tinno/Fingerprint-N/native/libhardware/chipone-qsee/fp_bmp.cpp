#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include "fp_log.h"
#include "fp_common.h"
#define WIDTHBYTES(i)    ((i+3)/4*4)
/* constants for the biCompression field */
#define BI_RGB        0L
#define BI_RLE8       1L
#define BI_RLE4       2L
#define BI_BITFIELDS  3L
#define BI_JPEG       4L
#define BI_PNG        5L

typedef unsigned char BYTE;               // 1B
typedef unsigned short WORD;              // 2B
typedef unsigned int DWORD;               // 4B
typedef int LONG;                         // 4B

typedef struct tagRGBQUAD
{
    BYTE    rgbBlue;
    BYTE    rgbGreen;
    BYTE    rgbRed;
    BYTE    rgbReserved;
} __attribute__((packed))RGBQUAD;

typedef struct tagBITMAPINFOHEADER
{
    DWORD biSize; //指定此结构体的长度，为40
    LONG biWidth; //位图宽
    LONG biHeight; //位图高
    WORD biPlanes; //平面数，为1
    WORD biBitCount; //采用颜色位数，可以是1，2，4，8，16，24，新的可以是32
    DWORD biCompression; //压缩方式，可以是0，1，2，其中0表示不压缩
    DWORD biSizeImage; //实际位图数据占用的字节数
    LONG biXPelsPerMeter; //X方向分辨率
    LONG biYPelsPerMeter; //Y方向分辨率
    DWORD biClrUsed; //使用的颜色数，如果为0，则表示默认值(2^颜色位数)
    DWORD biClrImportant; //重要颜色数，如果为0，则表示所有颜色都是重要的
} __attribute__((packed))BITMAPINFOHEADER;

typedef struct tagBITMAPFILEHEADER
{
    WORD magic; // "BM"，即0x4d42
    DWORD bfSize; //文件大小
    WORD bfReserved1; //保留字，不考虑
    WORD bfReserved2; //保留字，同上
    DWORD bfOffBits; //实际位图数据的偏移字节数，即前三个部分长度之和
} __attribute__((packed))BITMAPFILEHEADER;

int save_bmp(const char *file_name, char *img_buffer, int img_width, int img_height)
{
    FILE *pfile;
    //当前显示分辨率下每个像素所占字节数
    WORD            wBitCount;
    //位图中每个像素所占字节数
    DWORD           dwPaletteSize = 0, dwBmBitsSize,
                    dwDIBSize;  //定义调色板大小， 位图中像素字节大小 ，位图文件大小 ， 写入文件字节数
    BITMAPFILEHEADER   bmfHdr;
    //位图属性结构
    BITMAPINFOHEADER   bi;
    //位图文件头结构
    //位图信息头结构
    int width = img_width;
    int height = img_height;

    wBitCount = 8;
    dwPaletteSize = (1 << wBitCount) * sizeof(RGBQUAD);

    //设置位图信息头结构
    bi.biSize            = sizeof(BITMAPINFOHEADER);
    bi.biWidth           = width;
    bi.biHeight          = height;
    bi.biPlanes          = 1;
    bi.biBitCount         = wBitCount;
    bi.biCompression      = BI_RGB;
    bi.biSizeImage        = 0;
    bi.biXPelsPerMeter     = 0;
    bi.biYPelsPerMeter     = 0;
    bi.biClrUsed           = 0;
    bi.biClrImportant      = 0;

    dwBmBitsSize = ((width * wBitCount + 31) / 32) * 4 * height;

    // 设置位图文件头
    //bmfHdr.bfType = 0x4D42;  // "BM"
    bmfHdr.magic = 0x4D42;  // "BM"
    dwDIBSize    = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER) +
                   dwPaletteSize + dwBmBitsSize;
    bmfHdr.bfSize = dwDIBSize;
    bmfHdr.bfReserved1 = 0;
    bmfHdr.bfReserved2 = 0;
    bmfHdr.bfOffBits = (DWORD)sizeof(BITMAPFILEHEADER)
                       + (DWORD)sizeof(BITMAPINFOHEADER)
                       + dwPaletteSize;

    pfile = fopen(file_name, "wb"); //打开文件

    if (pfile != NULL)
    {
        LOGD("file %s open success.\n", file_name);


        // 写入位图文件头
        fwrite(&bmfHdr, sizeof(BITMAPFILEHEADER), 1, pfile);
        fwrite(&bi, sizeof(BITMAPINFOHEADER), 1, pfile);
        //调色板
        RGBQUAD pallette[256];
        for (int i = 0; i < 256; i++)
        {
            pallette[i].rgbRed = i;
            pallette[i].rgbGreen = i;
            pallette[i].rgbBlue = i;
            pallette[i].rgbReserved = 0;
        }
        fwrite(pallette, sizeof(pallette), 1, pfile);

/*        for (int i = height - 1; i >= 0; i--)
        {
            fwrite((img_buffer + width * i), width, 1, pfile);
        }*/
        fwrite(img_buffer, width * height, 1, pfile);
    }
    else
    {
        LOGD("file %s open fail,errno = %d.\n", file_name, -errno);
        return -1;
    }

    fclose(pfile);
    return 0;
}
