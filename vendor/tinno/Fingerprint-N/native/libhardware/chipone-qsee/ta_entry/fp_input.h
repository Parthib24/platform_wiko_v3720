#ifndef FP_INPUT_H__
#define FP_INPUT_H__

#include <linux/input.h>
#include <linux/uinput.h>
#include "fpGpioHal.h"

class fpInput
{
  public:
    fpInput(fpGpioHal *fp_hal);
    virtual ~fpInput();
    int32_t click_key(int32_t key_code,int key_value);
    int32_t config_key_status(int32_t key_code, int32_t enable_status);

  private:
    int32_t init(int32_t *key_array, int32_t key_cnt);
    int32_t deinit();
    void report_sync(void);
    void report_key(uint32_t key, int32_t pressed);
    fpsensor_key_event_t nav_key_switch(int32_t keyCode);
    int32_t key_report_fd;

    int32_t *internal_key_array;
    int32_t internal_key_array_cnt;
    fpGpioHal *p_hal;
};

#endif
