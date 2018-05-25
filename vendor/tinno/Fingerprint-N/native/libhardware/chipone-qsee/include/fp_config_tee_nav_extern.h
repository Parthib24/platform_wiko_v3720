#define NAV_ENABLE 1
#define NAV_DISABLE 0
#define EANBLE 1
#define DISABLE 0
#include "linux-event-codes.h"
typedef struct gesture_config
{
    uint32_t enable_direction;
    uint32_t enable_long_press;
    uint32_t enable_double_click;
    uint32_t long_press_time_threshold;
    uint32_t double_click_time_threshold;
    uint32_t enable_touch;
    uint32_t up_threshold;
    uint32_t down_threshold;
    uint32_t left_threshold;
    uint32_t right_threshold; 
    uint32_t two_dimensional_nav;
    uint32_t report_touch_by_timer;
} gesture_config_t;
extern  const gesture_config_t gesture_config ;
extern  const int32_t gKeys[7];