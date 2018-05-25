#ifndef FP_IALGORITHM_H
#define FP_IALGORITHM_H

#include "stdint.h"
#include "fp_image.h"

#define FP_IALGORITHM_VERSION1  (1)
#define FP_IALGORITHM_VERSION2  (2)

#define FP_ENROL_DONE 100
#define FP_TEMPLATE_UPDATED 1

#define MAX_RECTS_NUM 256
#define MAX_ISLAND_NUM 20

#define NOT_ENOUGH_RECT -1
#define MULTI_ISLANDS 0
#define UP_LESS 1
#define DOWN_LESS 2
#define LEFT_LESS 3
#define RIGHT_LESS 4


#ifdef __cplusplus
extern "C" {
#endif

typedef struct  _fpEnrolSession_st enrolment_session_t;


typedef struct _fp_template_t fp_template_t;

typedef fpImage fp_image_t;


typedef struct fp_image_quality_t2   //this def is used on the algo version 2
{
    uint16_t area;
    uint8_t quality;
    uint8_t condition;
} fp_image_quality_t2;
//--------------------------end

typedef struct fp_image_quality_t1
{
    int coverage;
    int quality;
} fp_image_quality_t1;

typedef struct fp_image_quality_t
{
    union
    {
        fp_image_quality_t1 quality_t1;
        fp_image_quality_t2 quality_t2;
    } q;
} fp_image_quality_t;

typedef struct _UiFeedBackTag
{
    int enr_num;
    int32_t num_accepted;
    uint8_t total_coverage;
    int event_;
    int fill_part;
} UiFeedBack;

typedef struct ialgorithm_t
{
    enrolment_session_t *(*enrolStart)(uint32_t imgW, uint32_t imgH);
    int32_t (*enrolAddImage)(enrolment_session_t *session,
                             fp_image_t *image,
                             fp_image_quality_t *image_quality);

    int32_t (*enrolGetTemplate)(enrolment_session_t *session, fp_template_t **tpl);
    int32_t (*enrolFinish)(enrolment_session_t *session);

    int32_t (*identifyImage)(fp_image_t *img,
                             fp_template_t **candidates,
                             uint32_t candidate_count,
                             int32_t *result,
                             int32_t *score,
                             fp_image_quality_t *image_quality);

    uint32_t (*templateGetPackedSize)(fp_template_t *tpl);
    int32_t (*templatePack)(fp_template_t *tpl, uint8_t *dst);
    int32_t (*templateUnPack)(uint8_t *src, uint32_t length, fp_template_t **tpl);
    void (*templateDelete)(fp_template_t *tpl);

    int32_t  (*identifyImage2)(fp_image_t *img, fp_template_t **candidates, uint32_t candidate_count,
                               int32_t *result, int32_t *score, fp_image_quality_t *image_quality, int32_t reqMatchLevel,
                               fp_template_t **verify_template);
    int32_t (*getUiFeedBack)(enrolment_session_t *session, UiFeedBack *UiFBK, size_t size);
    int32_t(*identify_for_enrolment)(fp_image_t *img, fp_template_t **candidates,
                                     uint32_t candidate_count, int32_t *result);
    int32_t (*update_template)( fp_image_t *img, fp_template_t *verify_template,
                                fp_template_t **candidates, uint32_t candidate_count, int32_t *result, int32_t *match_score );

    void (*reserved_function[9])();

    int32_t (*getImageQuality)(enrolment_session_t *session, fp_image_t *image,
                               fp_image_quality_t *image_quality);
    int32_t (*getTemplateId)(enrolment_session_t *session, int32_t *id, int32_t idlength);
    int32_t (*setProperty)(uint32_t propertytag, int32_t value);
    int32_t (*getFingerRectCnt)(enrolment_session_t *session);
    int32_t (*getFingerRect)(enrolment_session_t *session, int32_t index, int32_t *pdata);

} ialgorithm_t;

void getIalgorithm(ialgorithm_t *ialgorithm, size_t size);
int32_t getIalgorithmVersion(void);

#ifdef __cplusplus
}
#endif
#endif // FP_ALGORITHM_H
