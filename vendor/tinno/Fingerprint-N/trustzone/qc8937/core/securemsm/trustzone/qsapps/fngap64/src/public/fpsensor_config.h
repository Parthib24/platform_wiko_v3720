#ifndef FP_CONFIG
#define FP_CONFIG

#include "fp_tee_types.h"

#define NO_USE -1
#define NAV_ENABLE 1
#define NAV_DISABLE 0
#define ADJUST 1
#define NOT_ADJUST 0
#define EANBLE 1
#define DISABLE 0

// to enable finger_skin classifier
#define FINGER_SKIN_CLASS_PB   0x01
#define FINGER_SKIN_CLASS_GLCM 0x02

typedef struct wait_finger_adc
{
    unsigned int gain;
    unsigned int shift;
    unsigned int pxl_ctrl;
    unsigned int threadhold;
    unsigned int sleep_dect;
    unsigned int adjust;
    unsigned int esd_reset;
} wait_finger_adc_t;



typedef struct gesture_ta_config
{
    unsigned int enable_direction;
} gesture_ta_config_t;

typedef enum far{
    FAR_1               = 0,
    FAR_2               = 1,
    FAR_5               = 2,
    FAR_10              = 3,
    FAR_20              = 4,
    FAR_50              = 5,
    FAR_100             = 6,
    FAR_200             = 7,
    FAR_500             = 8,
    FAR_1000            = 9,
    FAR_2K              = 10,
    FAR_5000            = 11,
    FAR_10000           = 12,
    FAR_20K             = 13,
    FAR_50000           = 14,
    FAR_100000          = 15,
    FAR_200K            = 16,
    FAR_500000          = 17,
    FAR_1000000         = 18,
    FAR_2M              = 19,
    FAR_5M              = 20,
    FAR_10M             = 21,
    FAR_20M             = 22,
    FAR_50M             = 23,
    FAR_100M            = 24,
    FAR_200M            = 25,
    FAR_500M            = 26,
    FAR_1000M           = 27,
    FAR_Inf             = 28
} far_t;

typedef enum pixel_control{
    PIXEL_CONTROL_LEVEL0 = 0,
    PIXEL_CONTROL_LEVEL1,
    PIXEL_CONTROL_LEVEL2,
    PIXEL_CONTROL_LEVEL3
} pixel_control_t;

typedef enum memmber_mask{
    bit_mt_size = 2,
    bit_mt_size_bytes = 3,
    bit_secure_level = 4,
    bit_enr_samples = 5,
    bit_enable_enrollment_threshold = 6,
    bit_enrollment_qualtiy_threshold = 7,
    bit_enrollment_area_threshold = 8,
    bit_enable_verification_threshold = 9,
    bit_verification_qualtiy_threshold = 10,
    bit_verification_area_threshold = 11,
    bit_alg_name = 12,
    bit_enable_preprocessor = 13,


    bit_autogain_timeout_ms = 2,
    bit_init_gain,
    bit_init_shift,
    bit_init_pixel_control, 
    bit_init_cbase,
    bit_enable_post_check
} member_mask_t;

#define VALID_MEMBER(config, member) ( (config).valid_member & ( 1 << bit_##member ) )
#define SET_MEMBER(config, member, value) { config.valid_member |= ( 1 << bit_##member );  config.member = value;}

typedef struct algorithm_config
{
    unsigned int valid_member;
    unsigned int mt_size; // number of subtemplates in a template
    unsigned int mt_size_bytes; // max size of template by bytes
    unsigned int enr_samples; // number sample to enroll a finger
    far_t    secure_level;
    unsigned int enable_enrollment_threshold;
    unsigned int enrollment_qualtiy_threshold;
    unsigned int enrollment_area_threshold;
    unsigned int enable_verification_threshold;
    unsigned int verification_qualtiy_threshold;
    unsigned int verification_area_threshold;
    char alg_name[ALG_NAME_LEN];
    unsigned int enable_preprocessor;
} algorithm_config_t; 

typedef struct autogain_config
{
    unsigned int valid_member;
    unsigned int autogain_timeout_ms; // max time in milisecond used to do auto gain
    unsigned int init_gain;
    unsigned int init_shift;
    pixel_control_t init_pixel_control; 
    unsigned int init_cbase;
    unsigned int enable_post_check;
} autogain_config_t;

typedef struct fp_ta_config
{
    wait_finger_adc_t   wait_finger_adc[2];
    gesture_ta_config_t gesture;
    algorithm_config_t  algorithm;
    autogain_config_t   autogain;
} fp_config_t;

extern fp_config_t *fp_config;
extern int32_t fp_ta_config_init(void);
extern int32_t fp_ta_config_deinit(void);
// extern int key_array[6];
// extern int32_t dumy_for_link(void);
// extern int32_t dummy_link();

#endif
