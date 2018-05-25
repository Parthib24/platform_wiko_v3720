#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include "fp_input.h"
#include "fp_log.h"
#include "fp_common.h"


#define FPTAG "fp_input.cpp "

#define     MAKEKEYCODEENABLE(keycode)                              ((0x8000 << 16) | (keycode & 0xFFFF))        //high 16 bit not 0
#define     MAKEKEYCODEDISABLE(keycode)                             (keycode & 0xFFFF)                           //high 16 bit is 0
#define     ISKEYENABLED(composed_keycode)                          (composed_keycode >> 16)
#define     GETFROMCOMPOSEDKEY(composed_keycode)                    (composed_keycode & 0xFFFF)


// int32_t gKeys[] =
// {
//     KEY_LEFT,
//     KEY_RIGHT,
//     KEY_UP,
//     KEY_DOWN,
//     KEY_ENTER,
//     KEY_ESC,
//     KEY_SELECT
// };

extern  const int32_t gKeys[7];
fpInput::fpInput(fpGpioHal *fp_hal)
{
    key_report_fd = 0;

    internal_key_array = NULL;
    internal_key_array_cnt = 0;
    p_hal = fp_hal;

    init((int32_t *)gKeys, sizeof(gKeys) / sizeof(gKeys[0]));
}

fpInput::~fpInput()
{
    deinit();
}


#define FPSENSOR_UINPUT_DEVICE_NAME "uinput-fpsensor"
#define UINPUT_DEV "/dev/uinput"

int32_t fpInput::deinit()
{

#ifndef NAV_REPORT_IOCTL
    if (key_report_fd > 0)
    {
        if (ioctl(key_report_fd, UI_DEV_DESTROY) < 0)
        {
            LOGE(FPTAG"error: ioctl");
            return -1;
        }

        close(key_report_fd);
    }
    key_report_fd = 0;
#endif
    if (internal_key_array)
    {
        fp_free(internal_key_array);
    }
    internal_key_array = NULL;
    internal_key_array_cnt = 0;

    return 0;
}

//each key is composed by two part  HIGH16 | LOW16
//high16 repsent the enable or disable state
//low16 represent the keycode
int32_t fpInput::init(int32_t *key_array, int32_t key_cnt)
{
    int32_t i = 0;

    internal_key_array = (int32_t *)fp_malloc(key_cnt * sizeof(int32_t));
    if (!internal_key_array)
    {
        LOGE(FPTAG"%s error: nomem", __func__);
        goto fail;
    }
    internal_key_array_cnt = key_cnt;
    for (i = 0; i < key_cnt; i++)
    {
        internal_key_array[i] = MAKEKEYCODEENABLE(key_array[i]);
    }

#ifdef NAV_REPORT_IOCTL
    return 0;
#endif
    struct uinput_user_dev uidev;

    LOGD("%s invoked, the keycnt is %d, key list:", __func__, internal_key_array_cnt);
    for (i = 0; i < internal_key_array_cnt; i++)
    {
        LOGD("      pKeyArray[%d]:%d", i, key_array[i]);
    }

    key_report_fd = open(UINPUT_DEV, O_WRONLY | O_NONBLOCK);
    if (key_report_fd < 0)
    {
        LOGE(FPTAG"%s error: open %d", __func__, -errno);
        goto fail;
    }

    if (internal_key_array_cnt > 0)
    {
        if (ioctl(key_report_fd, UI_SET_EVBIT, EV_KEY) < 0)
        {
            LOGE(FPTAG"%s error: ioctl %d", __func__, -errno);
            goto fail;
        }

        for (i = 0; i < internal_key_array_cnt; i++)
        {
            if (ioctl(key_report_fd, UI_SET_KEYBIT, key_array[i]) < 0)
            {
                LOGE("%s error: open %d", __func__, -errno);
                goto fail;
            }
        }
    }

    memset(&uidev, 0, sizeof(uidev));
    sprintf(uidev.name, FPSENSOR_UINPUT_DEVICE_NAME);
    uidev.id.bustype = BUS_USB;
    uidev.id.vendor  = 0x1;
    uidev.id.product = 0x1;
    uidev.id.version = 1;

    if (write(key_report_fd, &uidev, sizeof(uidev)) < 0)
    {
        LOGE(FPTAG"%s error: write %d", __func__, -errno);
        goto fail;
    }

    if (ioctl(key_report_fd, UI_DEV_CREATE) < 0)
    {
        LOGE(FPTAG"%s error: ioctl %d", __func__, -errno);
        goto fail;
    }
    return 0;
fail:
    deinit();
    return 0;
}


void fpInput::report_sync(void)
{
    struct input_event ev;

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_SYN;
    ev.code = 0;
    ev.value = 0;
    if (key_report_fd > 0)
    {
        if (write(key_report_fd, &ev, sizeof(struct input_event)) < 0)
        {
            LOGE(FPTAG"%s error: write %d", __func__, -errno);
        }
    }
}
void fpInput::report_key(uint32_t key, int pressed)
{
    struct input_event ev;

    memset(&ev, 0, sizeof(struct input_event));
    ev.type = EV_KEY;
    ev.code = key;
    ev.value = pressed;
    if (key_report_fd > 0)
    {
        if (write(key_report_fd, &ev, sizeof(struct input_event)) < 0)
        {
            LOGE("%s error: write %d", __func__, -errno);
        }
    }
}

int32_t fpInput::config_key_status(int32_t key_code, int32_t enable_status)
{
    for (int32_t i = 0; i < internal_key_array_cnt; i++)
    {
        if (GETFROMCOMPOSEDKEY(internal_key_array[i]) == key_code)
        {
            LOGD(FPTAG"config_key_status invoked, find key = %d, new statusis %d", key_code, enable_status);
            if (enable_status)
            {
                MAKEKEYCODEENABLE(key_code);
            }
            else
            {
                MAKEKEYCODEDISABLE(key_code);
            }
            return 0;
        }
    }

    LOGE(FPTAG" config_key_status can't find key");
    return 0;
}

fpsensor_key_event_t fpInput::nav_key_switch(int32_t key_code)
{
    fpsensor_key_event_t key_event = FPSENSOR_KEY_NONE;

    switch (key_code)
    {
        case KEY_ENTER:
            key_event = FPSENSOR_KEY_TAP;
            break;

        case KEY_UP:
            key_event = FPSENSOR_KEY_UP;
            break;

        case KEY_DOWN:
            key_event = FPSENSOR_KEY_DOWN;
            break;

        case KEY_LEFT:
            key_event = FPSENSOR_KEY_LEFT;
            break;

        case KEY_RIGHT:
            key_event = FPSENSOR_KEY_RIGHT;
            break;

        default:
            LOGE(FPTAG"keycode:%d unknow!", key_code);
            break;
    }

    return key_event;
}

#ifdef NAV_REPORT_IOCTL
int32_t fpInput::click_key(int32_t key_code,int key_value)
{
    LOGD(FPTAG"%s invoked,keycode:%d", __func__, key_code);

    for (int i = 0; i < internal_key_array_cnt; i++)
    {
        if (GETFROMCOMPOSEDKEY(internal_key_array[i]) == key_code)
        {
            if (ISKEYENABLED(internal_key_array[i]))
            {
                LOGD(FPTAG"keycode:%d reported", key_code);
                p_hal->NavigationKeyReport(nav_key_switch(key_code));
                return 0;
            }
        }
    }
    LOGD(FPTAG"keycode:%d is masked", key_code);
    return -ENOENT;
}
#else
int32_t fpInput::click_key(int32_t key_code,int key_value)
{
    if (key_report_fd <= 0)
    {
        LOGE(FPTAG"%s error: write %d, input system not init", __func__, -errno);
        return -ENOENT;
    }
    LOGD(FPTAG"%s invoked,keycode:%d", __func__, key_code);

    for (int32_t i = 0; i < internal_key_array_cnt; i++)
    {
        if (GETFROMCOMPOSEDKEY(internal_key_array[i]) == key_code)
        {
            if (ISKEYENABLED(internal_key_array[i]))
            {
                //LOGD(FPTAG"keycode:%d reported", key_code);
                LOGD(FPTAG"THIS PLATFORM DO NOT NEED REPORT INPUT KEY!!!!!!!");
                //report_key(key_code, key_value);
                //report_sync();
                // report_key(key_code, 0);
                // report_sync();
                return 0;
            }
        }
    }
    LOGD(FPTAG"keycode:%d is masked", key_code);
    return -ENOENT;
}
#endif
