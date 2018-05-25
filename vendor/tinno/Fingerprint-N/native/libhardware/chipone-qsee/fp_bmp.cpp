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
    DWORD biSize; //ָ���˽ṹ��ĳ��ȣ�Ϊ40
    LONG biWidth; //λͼ��
    LONG biHeight; //λͼ��
    WORD biPlanes; //ƽ������Ϊ1
    WORD biBitCount; //������ɫλ����������1��2��4��8��16��24���µĿ�����32
    DWORD biCompression; //ѹ����ʽ��������0��1��2������0��ʾ��ѹ��
    DWORD biSizeImage; //ʵ��λͼ����ռ�õ��ֽ���
    LONG biXPelsPerMeter; //X����ֱ���
    LONG biYPelsPerMeter; //Y����ֱ���
    DWORD biClrUsed; //ʹ�õ���ɫ�������Ϊ0�����ʾĬ��ֵ(2^��ɫλ��)
    DWORD biClrImportant; //��Ҫ��ɫ�������Ϊ0�����ʾ������ɫ������Ҫ��
} __attribute__((packed))BITMAPINFOHEADER;

typedef struct tagBITMAPFILEHEADER
{
    WORD magic; // "BM"����0x4d42
    DWORD bfSize; //�ļ���С
    WORD bfReserved1; //�����֣�������
    WORD bfReserved2; //�����֣�ͬ��
    DWORD bfOffBits; //ʵ��λͼ���ݵ�ƫ���ֽ�������ǰ�������ֳ���֮��
} __attribute__((packed))BITMAPFILEHEADER;

int save_bmp(const char *file_name, char *img_buffer, int img_width, int img_height)
{
    FILE *pfile;
    //��ǰ��ʾ�ֱ�����ÿ��������ռ�ֽ���
    WORD            wBitCount;
    //λͼ��ÿ��������ռ�ֽ���
    DWORD           dwPaletteSize = 0, dwBmBitsSize,
                    dwDIBSize;  //�����ɫ���С�� λͼ�������ֽڴ�С ��λͼ�ļ���С �� д���ļ��ֽ���
    BITMAPFILEHEADER   bmfHdr;
    //λͼ���Խṹ
    BITMAPINFOHEADER   bi;
    //λͼ�ļ�ͷ�ṹ
    //λͼ��Ϣͷ�ṹ
    int width = img_width;
    int height = img_height;

    wBitCount = 8;
    dwPaletteSize = (1 << wBitCount) * sizeof(RGBQUAD);

    //����λͼ��Ϣͷ�ṹ
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

    // ����λͼ�ļ�ͷ
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

    pfile = fopen(file_name, "wb"); //���ļ�

    if (pfile != NULL)
    {
        LOGD("file %s open success.\n", file_name);


        // д��λͼ�ļ�ͷ
        fwrite(&bmfHdr, sizeof(BITMAPFILEHEADER), 1, pfile);
        fwrite(&bi, sizeof(BITMAPINFOHEADER), 1, pfile);
        //��ɫ��
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
