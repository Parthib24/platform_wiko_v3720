#ifndef FP_NAV_H
#define FP_NAV_H

#include <inttypes.h>
#include <unistd.h>
#include "fp_common_external.h"
#include "fp_config_tee_nav_extern.h"
#define FLOAT_MAX 100
#define DEVICE_WIDTH 720
#define DEVICE_HEIGHT 1280
#define FPSENSOR_INPUT_POLL_TIME_MS  1000u
#define FPSENSOR_HW_DETECT_MASK 0x6f6
#define FPSENSOR_INPUT_POLL_INTERVAL 5
#define IN
#define OUT

#define MIN(a,b)  (a) < (b) ? (a) : (b)

class fpGpioHal;


typedef enum
{
    FPSENSOR_INPUTMODE_TRACKPAD  = 0,    //trackpad(navi) event report
    FPSENSOR_INPUTMODE_MOUSE     = 1,    //mouse event report
    FPSENSOR_INPUTMODE_TOUCH     = 2,    //touch event report
    FPSENSOR_INPUTMODE_MOVE      = 3,    //move event report
} finger_inputmode_t;

typedef enum
{
    FNGR_ST_NONE = 0,
    FNGR_ST_DETECTED,
    FNGR_ST_LOST,
    FNGR_ST_TAP,
    FNGR_ST_HOLD,
    FNGR_ST_MOVING,
    FNGR_ST_L_HOLD,
    FNGR_ST_DOUBLE_TAP,
} finger_status_t;

typedef struct fpsensor_nav_struct
{
    bool enabled;
    /*image based navigation parameter*/
    uint16_t image_nav_row_start;
    uint16_t image_nav_row_count;
    uint16_t image_nav_col_start;
    uint16_t image_nav_col_groups;

    unsigned long time;
    int tap_status;
    uint16_t input_mode;
    int nav_sum_x;
    int nav_sum_y;

    uint16_t p_multiplier_x;
    uint16_t p_multiplier_y;
    uint16_t p_sensitivity_key;
    uint16_t p_sensitivity_ptr;
    uint16_t multiplier_key_accel;
    uint16_t multiplier_ptr_accel;
    uint16_t threshold_key_accel;
    uint16_t threshold_ptr_accel;
    uint16_t threshold_ptr_start;
    uint16_t duration_ptr_clear;
    uint16_t nav_finger_up_threshold;
    uint64_t move_time_threshold;

    uint64_t            touch_time;
    uint64_t            leave_time;
    int                 move_distance;
    unsigned int        moving_key;
    unsigned int        left_threshold;
    unsigned int        right_threshold;
    unsigned int        up_threshold;
    unsigned int        down_threshold;
    unsigned int        right_count;
    unsigned int        left_count;
    unsigned int        up_count;
    unsigned int        down_count;
    int move_distance_threshold;
    uint64_t double_click_time_threshold;
    unsigned int        up;
    unsigned int        down;
    unsigned int        left;
    unsigned int        right;
    int                 sumy;
    int                 col_x;
    int                 col_y;
} fpsensor_nav_task_t;


extern void nav_time_update(unsigned long time);
extern unsigned long nav_time_get(void);
extern uint64_t get_jiffiess_ms();
extern void init_enhanced_navi_setting(void);
extern void dispatch_move_event(int x, int y, int finger_status, fpGpioHal *pHal);
extern void process_navi_event(int dx, int dy, int finger_status, fpGpioHal *pHal);
#endif
