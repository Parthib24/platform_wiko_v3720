#include <stdint.h>
#include <stdlib.h>
#include "fpsensor_config.h"
#include <string.h>
#include <qsee_spi.h>
#include "fp_internal_api.h"

#define LOGTAG "fp_ta_config.c "

fp_config_t *fp_config = NULL;
qsee_spi_device_id_t fpsensor_qsee_spi_id = QSEE_SPI_DEVICE_6;

int32_t fp_ta_config_init(void)
{
    if (NULL != fp_config)
    {
        LOGE(LOGTAG"ERROR: fp_config is not NULL!!\n");   
        return -FP_ERROR_CONFIG;
    }

    fp_config = fp_malloc(sizeof(fp_config_t));
    if (NULL == fp_config)
    {
        return -FP_ERROR_ALLOC;
    }
    //--------wait_finger_adc----------------
    /*capture wait finger down*/
    fp_config->wait_finger_adc[0].gain              = 5;
    fp_config->wait_finger_adc[0].shift             = 12;
    fp_config->wait_finger_adc[0].pxl_ctrl          = 0;
    fp_config->wait_finger_adc[0].threadhold        = 0xe0;
    fp_config->wait_finger_adc[0].sleep_dect        = 1;
    fp_config->wait_finger_adc[0].adjust            = NOT_ADJUST;
    fp_config->wait_finger_adc[0].esd_reset         = EANBLE;
 
    /*gesture wait finger down*/ 
    fp_config->wait_finger_adc[1].gain              = 6;
    fp_config->wait_finger_adc[1].shift             = 20;
    fp_config->wait_finger_adc[1].pxl_ctrl          = 0;
    fp_config->wait_finger_adc[1].threadhold        = 0x60;
    fp_config->wait_finger_adc[1].sleep_dect        = 1;
    fp_config->wait_finger_adc[1].adjust            = NOT_ADJUST;
    fp_config->wait_finger_adc[1].esd_reset         = DISABLE;
 
    /*gesture_config*/ 
    fp_config->gesture.enable_direction             = NAV_DISABLE;
 
    /*algorithm_config*/ 
    char algo[] = "neosq_xs";
    strncpy(fp_config->algorithm.alg_name, algo, sizeof(algo));
    fp_config->algorithm.valid_member                   = 0xFFFF;
    fp_config->algorithm.mt_size                        = 30;
    fp_config->algorithm.mt_size_bytes                  = 0;
    fp_config->algorithm.enr_samples                    = 10;
    fp_config->algorithm.secure_level                   = FAR_2M; // if using local_preprocess, FAR secure level should set to 2M which equal to 50k
    fp_config->algorithm.enable_enrollment_threshold    = 1;
    fp_config->algorithm.enrollment_qualtiy_threshold   = 40;
    fp_config->algorithm.enrollment_area_threshold      = 0;
    fp_config->algorithm.enable_verification_threshold  = 0;
    fp_config->algorithm.verification_qualtiy_threshold = 0;
    fp_config->algorithm.verification_area_threshold    = 0;
    fp_config->algorithm.enable_preprocessor            = 2; // 1:pb_preprocess 2:local_preprocess

    /*autogain_config*/
    /*if you want to modify init_pixel_control, 
    please modify valid_member to 0xFFFF ,
    modify relevant code and set proper value.
    init_pixel_control can be set to 0~3,
               0     1      2    3
    it means {0x14, 0x04, 0x10, 0x00}
    */
    fp_config->autogain.valid_member        = 0xFFDF;
    fp_config->autogain.autogain_timeout_ms = 200;
    fp_config->autogain.init_gain           = 0;
    fp_config->autogain.init_shift          = 0;
    fp_config->autogain.init_pixel_control  = 2;
    fp_config->autogain.init_cbase          = 0;
    fp_config->autogain.enable_post_check   = 0;

    return 0;
}


int32_t fp_ta_config_deinit(void)
{
    fp_free(fp_config);
    fp_config = NULL;

    return 0;
}
