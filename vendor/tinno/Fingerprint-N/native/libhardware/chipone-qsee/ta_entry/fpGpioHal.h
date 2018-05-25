#ifndef FP_GPIO_HAL_H
#define FP_GPIO_HAL_H


#include <inttypes.h>
#include <pthread.h>

typedef enum fpsensor_key_event
{
    FPSENSOR_KEY_NONE = 0,
    FPSENSOR_KEY_HOME,
    FPSENSOR_KEY_POWER,
    FPSENSOR_KEY_MENU,
    FPSENSOR_KEY_BACK,
    FPSENSOR_KEY_CAPTURE,
    FPSENSOR_KEY_UP,
    FPSENSOR_KEY_DOWN,
    FPSENSOR_KEY_RIGHT,
    FPSENSOR_KEY_LEFT,
    FPSENSOR_KEY_TAP,
    FPSENSOR_KEY_HEAVY
} fpsensor_key_event_t;

struct fpsensor_key
{
    enum fpsensor_key_event key;
    uint32_t value;   /* key down = 1, key up = 0 */
};

class fpTaEntryProxy;
class fpInput;

class fpGpioHal
{
  public:
    fpGpioHal(fpTaEntryProxy *fpProxy);
    ~fpGpioHal();
    int WaitForGpioIrq();
    int IOCtrlInit(void);
    //int IrqNetlinkReceive(void);
    int ChipGpioReset(void);
    int DisableSPICLK(void);
    int EnableSPICLK(void);
    int SetDevInfo(void);
    int SetVerInfo(const char *ca_ver, const char *ta_ver);
    int SetFingerState(int);
    int CancelWaitFingerDown(void);
    int CancelClear(void);
    int Remove(void);
    void LockPollMutex(void);
    void UnLockPollMutex(void);
    int NavigationKeyReport(fpsensor_key_event_t event);

    fpTaEntryProxy *TaProxy;
    fpInput *pfpInput;
  private:
    int IrqPoll(void);
    pthread_mutex_t poll_mutex;
    int cancel_fds[2];
};

#endif
