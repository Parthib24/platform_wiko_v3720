#include "util.h"
#include <sys/time.h>
#include <unistd.h>
#include <endian.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include "fpTa_entryproxy.h"
#include "fpGpioHal.h"
#include "fp_nav.h"
#include "fp_log.h"
#include "fp_input.h"
#include "time.h"

#define FPTAG "fp_nav.cpp "
static timer_t  timerid = 0;
static struct itimerspec it;
static struct sigevent evp;
//------navigation resource---------
enum
{
    LEFT = 0,
    RIGHT,
    UP,
    DOWN,
    TOUCH,
    LONG_PRESS,
    DOUBLE_CLICK
};

fpsensor_nav_task_t  nav = {0};


uint64_t get_jiffiess_ms()
{
    struct timeval ts;
    gettimeofday(&ts, NULL);
    return (ts.tv_usec / 1000 + (ts.tv_sec ) * 1000);
    // return ts.tv_usec/1000;
}

void init_enhanced_navi_setting(void)
{
    LOGD(FPTAG"%s\n", __func__);
    nav.input_mode = FPSENSOR_INPUTMODE_MOVE;
    switch (nav.input_mode)
    {
        case FPSENSOR_INPUTMODE_TRACKPAD:
            nav.p_sensitivity_key = 25;
            nav.p_sensitivity_ptr = 180;
            nav.p_multiplier_x = 75;
            nav.p_multiplier_y = 95;
            nav.multiplier_key_accel = 2;
            nav.multiplier_ptr_accel = 2;
            nav.threshold_key_accel = 70;
            nav.threshold_ptr_accel = 10;
            nav.threshold_ptr_start = 5;
            nav.duration_ptr_clear = 100;
            nav.nav_finger_up_threshold = 3;
            break;
        case FPSENSOR_INPUTMODE_TOUCH:
            nav.p_sensitivity_key = 200;//180;
            nav.p_sensitivity_ptr = 180;
            nav.p_multiplier_x = 255;//110;
            nav.p_multiplier_y = 110;
            nav.multiplier_key_accel = 1;
            nav.multiplier_ptr_accel = 2;
            nav.threshold_key_accel = 40;
            nav.threshold_ptr_accel = 20;
            nav.threshold_ptr_start = 5;
            nav.duration_ptr_clear = 100;
            nav.nav_finger_up_threshold = 3;
            break;
        case FPSENSOR_INPUTMODE_MOVE:
            nav.p_sensitivity_key = 180;//180;
            nav.p_sensitivity_ptr = 180;
            nav.p_multiplier_x = 200;//110;
            nav.p_multiplier_y = 200;
            nav.multiplier_key_accel = 1;
            nav.multiplier_ptr_accel = 2;
            nav.threshold_key_accel = 40;
            nav.threshold_ptr_accel = 20;
            nav.threshold_ptr_start = 5;
            nav.duration_ptr_clear = 100;
            nav.nav_finger_up_threshold = 3;
            if (gesture_config.enable_long_press)
            {
                nav.move_time_threshold = gesture_config.long_press_time_threshold;
            }
            else
            {
                nav.move_time_threshold = 0;
            }
            if (gesture_config.enable_double_click)
            {
                nav.double_click_time_threshold = gesture_config.double_click_time_threshold;
            }
            nav.move_distance_threshold = 50;
            break;
        default:
            break;
    }
}


void nav_time_update(unsigned long time)
{
    LOGD(FPTAG"nav_time_update:%ld", time);
    nav.time = time;
}

unsigned long nav_time_get(void)
{
    unsigned long time = nav.time;
    LOGD(FPTAG"nav_time_get:%ld", time);
    return time;
}

void report_key_event(int32_t key_code, fpGpioHal *pGpio)
{
    LOGD(FPTAG"report_key_event invoked,keycode is:%d", key_code);
    fpInput *pInput =  pGpio->pfpInput;
    pInput->click_key(key_code, 1);
    pInput->click_key(key_code, 0);
}


/* -------------------------------------------------------------------- */
void dispatch_move_event(int x, int y, int finger_status, fpGpioHal *pHal)
{
    int sign_x;
    int sign_y;
    int abs_x;
    int abs_y;
    // int newY;
    // int newX;
    // struct timeval ts;
    uint64_t jiffiess = 0 ;
    uint32_t finger_state = 0x02;
    //fpsensor_key_event_t key_event = FPSENSOR_KEY_NONE;

//    LOGD(FPTAG"dispatch_move_event invoked");
    switch (finger_status)
    {
        case FNGR_ST_DETECTED:
            finger_state = 0x01;
            pHal->SetFingerState(finger_state);
            nav.nav_sum_x = 640;
            nav.nav_sum_y = 640;
            jiffiess =  get_jiffiess_ms();
            LOGD(FPTAG"[rickon]-----begin:   %" PRIu64 " \n", jiffiess);
            nav.touch_time = jiffiess;
            if (gesture_config.enable_direction)
            {
                nav.moving_key = 3;
            }
            else
            {
                nav.moving_key = 0;
            }
            nav.sumy  = 0;
            nav.up     = 0;
            nav.down   = 0;
            nav.right  = 0;
            nav.left   = 0;
            nav.left_count = 0;
            nav.right_count = 0;
            nav.down_count = 0;
            nav.up_count = 0;
            nav.col_y = 0;
            nav.col_x = 0;
            nav.left_threshold  = gesture_config.left_threshold ;
            nav.right_threshold = gesture_config.right_threshold ;
            nav.up_threshold    = gesture_config.up_threshold ;
            nav.down_threshold  = gesture_config.down_threshold ;
            if (gesture_config.enable_touch == 1)
            {

            }
            break;

        case FNGR_ST_LOST:
            finger_state = 0x02;
            pHal->SetFingerState(finger_state);
            jiffiess = get_jiffiess_ms();
            // LOGD(FPTAG"[rickon] end:%ld  time %" PRIu64 " ms\n", jiffiess, (jiffiess - nav.touch_time));
            // if (moving_key) {
            //     LOGD(FPTAG"[rickon1]----- finger LOST!!! \n");
            //     // input_report_key(input_dev, moving_key, 0);
            //     // input_sync(input_dev);
            // } else if ((jiffiess - touch_time ) < nav.move_time_threshold) {
            //     LOGD(FPTAG"[rickon1]----- finger TOUCH!!! \n");
            //     key_event = FPSENSOR_KEY_TAP;
            //     pHal->pfpInput->clickKey(KEY_ENTER);
            //     pHal->NavigationKeyReport(key_event);

            // }

            if (nav.moving_key == 0 && gesture_config.enable_touch == 1
                && (jiffiess - nav.leave_time) > nav.double_click_time_threshold)
            {
                if (gesture_config.report_touch_by_timer == 1)
                {
                    it.it_interval.tv_sec = 0;
                    it.it_interval.tv_nsec = 0;
                    it.it_value.tv_sec = 0;
                    it.it_value.tv_nsec = (nav.double_click_time_threshold + 1) * 1000 * 1000;
                    if (timerid != 0)
                    {
                        LOGD(FPTAG"[rickon1]------ start touch timer!!! ");
                        if (timer_settime(timerid, 0, &it, NULL) == -1)
                        {
                            LOGE("fail to timer_settime");
                            break;
                        }
                    }

                }
                else
                {
                    LOGD(FPTAG"[rickon1]----- finger TOUCH value 1!!! \n");
                    pHal->pfpInput->click_key(gKeys[TOUCH], 1);
                    LOGD(FPTAG"[rickon1]----- finger TOUCH value 0!!! ");
                    pHal->pfpInput->click_key(gKeys[TOUCH], 0);
                }

                if (gesture_config.enable_double_click == 1)
                {
                    nav.leave_time = jiffiess;
                }
                else
                {
                    nav.leave_time = 0;
                }
                break;
            }
            else if (nav.moving_key == 0 && gesture_config.enable_double_click == 1
                     && (jiffiess - nav.leave_time) <= nav.double_click_time_threshold)
            {
                if (timerid != 0 && gesture_config.report_touch_by_timer == 1)
                {
                    it.it_value.tv_sec = 0;
                    it.it_value.tv_nsec = 0 ;
                    if (timer_settime(timerid, 1, &it, NULL) == -1)
                    {
                        LOGE("fail to timer_settime");
                        break;
                    }
                }
                LOGD(FPTAG"[rickon1]------ finger double click!!!");
                pHal->pfpInput->click_key(gKeys[DOUBLE_CLICK], 1);
                pHal->pfpInput->click_key(gKeys[DOUBLE_CLICK], 0);
                nav.leave_time = 0;
                break;
            }
            else if (2 == nav.moving_key)
            {

                if (timerid != 0 && gesture_config.report_touch_by_timer == 1)
                {
                    it.it_value.tv_sec = 0;
                    it.it_value.tv_nsec = 0 ;
                    if (timer_settime(timerid, 1, &it, NULL) == -1)
                    {
                        LOGE("fail to timer_settime");
                        break;
                    }
                }
                if (gesture_config.two_dimensional_nav == 1)
                {
                    abs_x = nav.col_x > 0 ? nav.col_x : -nav.col_x;
                    abs_y = nav.col_y > 0 ? nav.col_y : -nav.col_y;
                    LOGD(FPTAG"[rickon1]----------x:%d y:%d \n", nav.col_x , nav.col_y);
                    if (abs_x > abs_y )
                    {
                        if (nav.col_x > 0)
                        {
                            LOGD(FPTAG"[rickon1]----------RIGHT \n");
                            pHal->pfpInput->click_key(gKeys[RIGHT], 1);
                            pHal->pfpInput->click_key(gKeys[RIGHT], 0);
                        }
                        else
                        {
                            LOGD(FPTAG"[rickon1]----------LEFT \n");
                            pHal->pfpInput->click_key(gKeys[LEFT], 1);
                            pHal->pfpInput->click_key(gKeys[LEFT], 0);
                        }
                    }
                    else
                    {
                        if (nav.col_y > 0)
                        {
                            LOGD(FPTAG"[rickon1]----------UP \n");
                            pHal->pfpInput->click_key(gKeys[UP], 1);
                            pHal->pfpInput->click_key(gKeys[UP], 0);
                        }
                        else
                        {
                            LOGD(FPTAG"[rickon1]----------DOWN \n");
                            pHal->pfpInput->click_key(gKeys[DOWN], 1);
                            pHal->pfpInput->click_key(gKeys[DOWN], 0);
                        }
                    }
                }
                else
                {
                    if ((nav.up > nav.down) && ((nav.up + nav.down) > 2))
                    {
                        LOGD(FPTAG"[rickon1]----------UP \n");
                        pHal->pfpInput->click_key(gKeys[UP], 1);
                        pHal->pfpInput->click_key(gKeys[UP], 0);
                    }
                    else if (nav.down > nav.up && ((nav.up + nav.down) > 2) )
                    {
                        LOGD(FPTAG"[rickon1]----------DOWN \n");
                        pHal->pfpInput->click_key(gKeys[DOWN], 1);
                        pHal->pfpInput->click_key(gKeys[DOWN], 0);
                    }
                    else if ((nav.sumy > 1) && (nav.up > 1))
                    {
                        LOGD(FPTAG"[rickon1]----------UP \n");
                        pHal->pfpInput->click_key(gKeys[UP], 1);
                        pHal->pfpInput->click_key(gKeys[UP], 0);
                    }
                    else if ((nav.sumy < -1) && (nav.down > 1))
                    {
                        LOGD(FPTAG"[rickon1]----------DOWN \n");
                        pHal->pfpInput->click_key(gKeys[DOWN], 1);
                        pHal->pfpInput->click_key(gKeys[DOWN], 0);
                    }
                    if (nav.left > (nav.right + 1) /*&& nav.left > nav.up && nav.left > nav.down*/)
                    {
                        LOGD(FPTAG"[rickon1]----------LEFT \n");
                        pHal->pfpInput->click_key(gKeys[LEFT], 1);
                        pHal->pfpInput->click_key(gKeys[LEFT], 0);
                    }
                    else if (nav.right > (nav.left + 1) /*&& nav.right > nav.up && nav.right > nav.down*/)
                    {
                        LOGD(FPTAG"[rickon1]----------RIGHT \n");
                        pHal->pfpInput->click_key(gKeys[RIGHT], 1);
                        pHal->pfpInput->click_key(gKeys[RIGHT], 0);
                    }
                }
            }

            break;

        case FNGR_ST_MOVING:
            sign_x = x > 0 ? 1 : -1;
            sign_y = y > 0 ? 1 : -1; //reverse direction
            abs_x = x > 0 ? x : -x;
            abs_y = y > 0 ? y : -y;
            LOGD(FPTAG"[rickon]-----before %s ,X: %d  Y: %d \n ", __func__ , x, y);
            LOGD(FPTAG"[rickon]----- p_multiplier_y :%d  p_sensitivity_key :%d p_multiplier_x:%d \n",
                 nav.p_multiplier_y, nav.p_sensitivity_key, nav.p_multiplier_x );
            if (abs_y > nav.threshold_key_accel)
            {
                y = ( nav.threshold_key_accel + ( abs_y - nav.threshold_key_accel ) * nav.multiplier_key_accel ) *
                    sign_y;
            }

            y = y * nav.p_multiplier_y / FLOAT_MAX;
            y = y * nav.p_sensitivity_key / FLOAT_MAX;

            if (abs_x > nav.threshold_key_accel)
            {
                x = ( nav.threshold_key_accel + ( abs_x - nav.threshold_key_accel ) * nav.multiplier_key_accel ) *
                    sign_x;
            }

            x = x * nav.p_multiplier_x / FLOAT_MAX;
            x = x * nav.p_sensitivity_key / FLOAT_MAX;

            LOGD(FPTAG"[rickon]-----after %s ,X: %d  Y: %d \n ", __func__ , x, y);
            abs_x = x > 0 ? x : -x;
            abs_y = y > 0 ? y : -y;
            jiffiess =  get_jiffiess_ms();
            nav.sumy += y;
            LOGD(FPTAG"[rickon]------MOVE time : %" PRIu64 " \n", (jiffiess - nav.touch_time));
            if (nav.moving_key == 3)
            {
                nav.moving_key = 0 ;
            }
            if ((jiffiess - nav.touch_time) >= nav.move_time_threshold)
            {
                if (gesture_config.enable_long_press && nav.moving_key == 0)
                {
                    LOGD(FPTAG"[rickon1]---- long press \n");
                    pHal->pfpInput->click_key(gKeys[LONG_PRESS], 1);
                    pHal->pfpInput->click_key(gKeys[LONG_PRESS], 0);
                    nav.moving_key = 1;
                }
            }
            if (gesture_config.enable_direction && nav.moving_key != 1)
            {
                if (abs_x != 0 || abs_y != 0)
                {
                    LOGD(FPTAG"[rickon1]----- ,newX: %d  newY: %d \n ",  x, y);
                    if (x > 0 )
                    {
                        nav.right_count ++;
                        if (nav.right_count > nav.right_threshold)
                        {
                            nav.moving_key = 2;
                            nav.right++;
                            // LOGD(FPTAG"[rickon]----------RIGHTing \n");
                            // nav.right_count = 0;
                            // nav.left_count = 0;
                            nav.left_threshold = 0;
                            nav.right_threshold = 0;
                        }
                    }
                    else
                    {
                        nav.left_count++;
                        if (nav.left_count > nav.left_threshold)
                        {
                            nav.moving_key = 2;
                            nav.left++;
                            nav.left_threshold = 0;
                            nav.right_threshold = 0;
                            // LOGD(FPTAG"[rickon]----------LEFTing \n");
                            // pHal->pfpInput->clickKey(KEY_LEFT);
                            // nav.right_count = 0;
                            // nav.left_count = 0;
                            // nav.up_count = 0;
                            // nav.down_count = 0;
                        }
                    }
                    nav.col_x += x;
                    if (y > 0)
                    {
                        nav.up_count++;
                        if (nav.up_count > nav.up_threshold)
                        {
                            nav.moving_key = 2;
                            nav.up++;
                            // LOGD(FPTAG"[rickon]----------UPing \n");
                            // pHal->pfpInput->clickKey(KEY_UP);
                            // nav.right_count = 0;
                            // nav.left_count = 0;
                            nav.up_threshold = 0;
                            nav.down_threshold = 0;
                            nav.up_count = 0;
                            nav.down_count = 0;
                        }
                    }
                    else
                    {
                        nav.down_count++;
                        if (nav.down_count > nav.down_threshold)
                        {
                            // LOGD(FPTAG"[rickon]----------DOWNing \n");
                            // pHal->pfpInput->clickKey(KEY_DOWN);
                            nav.moving_key = 2;
                            nav.down++;
                            // nav.right_count = 0;
                            // nav.left_count = 0;
                            //  nav.up_threshold = 0;
                            nav.down_threshold = 0;
                            nav.up_count = 0;
                            nav.down_count = 0;
                        }
                    }
                    nav.col_y += y;
                }
            }

            break;

        default:
            break;
    }
}
#define CLOCKID CLOCK_REALTIME
void timer_thread(union sigval v)
{
    LOGD(FPTAG"[rickon1]------ in timer!!!");
    fpGpioHal *pHal = (fpGpioHal *)v.sival_ptr;
    LOGD(FPTAG"[rickon1]----- finger TOUCH value 1!!! \n");
    pHal->pfpInput->click_key(gKeys[TOUCH], 1);
    LOGD(FPTAG"[rickon1]----- finger TOUCH value 0!!! ");
    pHal->pfpInput->click_key(gKeys[TOUCH], 0);

}
/* -------------------------------------------------------------------- */
void process_navi_event(int dx, int dy, int finger_status, fpGpioHal *pHal)
{
    const int THRESHOLD_RANGE_TAP = 100;
    int filtered_finger_status = finger_status;
    static int deviation_x = 0;
    static int deviation_y = 0;
    int deviation;
    static unsigned long tick_down = 0;
    uint64_t jiffiess = 0;
    jiffiess = get_jiffiess_ms();
    unsigned long tick_curr = jiffiess / 1000 ;
    unsigned long duration = 0;
    const unsigned long THRESHOLD_DURATION_HOLD = 200;
    /////timer for touch
    if (gesture_config.report_touch_by_timer == 1)
    {
        if (timerid == 0)
        {
            LOGD(FPTAG"GET evp ADDR: %p \n",&evp);
            memset(&evp, 0, sizeof(struct sigevent));
            evp.sigev_value.sival_ptr = pHal;
            evp.sigev_notify = SIGEV_THREAD;
            evp.sigev_notify_function = timer_thread;
            if (timer_create(CLOCKID, &evp, &timerid) == -1)
            {
                LOGE(FPTAG"fail to timer_create");
            }
        }
    }

//    LOGD(FPTAG"process_navi_event invoked");
    if ( finger_status == FNGR_ST_DETECTED )
    {
        tick_down = tick_curr;
        deviation_x = 0;
        deviation_y = 0;
    }

    if ( tick_down > 0 )
    {
        duration = tick_curr - tick_down;
        deviation_x += dx;
        deviation_y += dy;
        deviation =  deviation_x * deviation_x + deviation_y * deviation_y;

        if ( deviation > THRESHOLD_RANGE_TAP )
        {
            deviation_x = 0;
            deviation_y = 0;
            tick_down = 0;
            nav.tap_status = -1;
        }
        else if ( duration > THRESHOLD_DURATION_HOLD )
        {
            filtered_finger_status = FNGR_ST_HOLD;
            tick_down = 0;
            deviation_x = 0;
            deviation_y = 0;
        }

    }

    nav.input_mode = FPSENSOR_INPUTMODE_MOVE;
    switch (nav.input_mode)
    {
        case FPSENSOR_INPUTMODE_TRACKPAD :
            break;
        case FPSENSOR_INPUTMODE_TOUCH:
            break;
        case FPSENSOR_INPUTMODE_MOVE:
            dispatch_move_event(dx, dy, filtered_finger_status, pHal);
            break;
        default:
            break;
    }
}

