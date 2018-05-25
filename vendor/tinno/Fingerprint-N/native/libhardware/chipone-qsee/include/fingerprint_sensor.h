#ifndef FINGERPRINT_SENSOR_H
#define FINGERPRINT_SENSOR_H

#ifdef NDK_ROOT
#include "hardware.h"
#else
#include <hardware/hardware.h>
#endif
#include "fp_ialgorithm.h"

#define FP_FINGERPRINT_MODULE_API_VERSION_0 HARDWARE_MAKE_API_VERSION(0, 0)
#define FP_FINGERPRINT_DEVICE_API_VERSION_0 HARDWARE_DEVICE_API_VERSION(0, 0)

#define FP_FINGERPRINT_HARDWARE_MODULE_ID  "fpsensor_module"
#define FP_FINGERPRINT_SENSOR_DEVICE       "fpsensor_sensor"
#define FP_FINGERPRINT_SYSTEM_DEVICE       "fpsensor_system"

#define MSG_TYPE_COMMON_BASE                  150
#define MSG_TYPE_FINGER_FAST                  (MSG_TYPE_COMMON_BASE+0)
#define MSG_TYPE_FINGER_MOVE                  (MSG_TYPE_COMMON_BASE+1)
#define MSG_TYPE_FINGER_RETRY                 (MSG_TYPE_COMMON_BASE+2)
#define MSG_TYPE_FINGER_TIMEOUT               (MSG_TYPE_COMMON_BASE+3)
#define MSG_TYPE_FINGER_ERROR                 (MSG_TYPE_COMMON_BASE+4)
#define MSG_TYPE_FINGER_LOW_COVERAGE          (MSG_TYPE_COMMON_BASE+5)

//This macro means the status of finger before capture image
//less than  this value,  means the finger is aready pressed
//equal or greater than this value, means the finger in up status
#define PRE_CAPTURE_IMAGE_FINGER_PRESS_THRESHOLD       50 //ms

typedef struct fp_fingerprint_moudule_t
{
    struct hw_module_t common; //inheritance

} fp_fingerprint_moudule_t;

typedef enum
{
    FP_SENSOR_EVENT_WAITING_FINGER     = 1,
    FP_SENSOR_EVENT_FINGER_DOWN        = 2,
    FP_SENSOR_EVENT_FINGER_UP          = 3
} fp_sensor_event_t;

typedef void (fp_sensor_event_callback_t) (fp_sensor_event_t event, void* user);
typedef int (fp_exit_condition_t) (void* user);

typedef struct fp_image_format_t
{
    fp_frame_format_t frame_format;
    uint32_t max_frames;
} fp_image_format_t;



typedef struct fp_fingerprint_device_t
{
    struct hw_device_t common; //inheritance

    int32_t (*open)(struct fp_fingerprint_device_t* device);
    int32_t (*close)(struct fp_fingerprint_device_t* device);

    /**
     * captureImage must call shouldExit() when waiting for IO and return
     * -FP_ERROR_USER_CANCEL if the exit condition was true.
     */
    void (*setCallbacks)(struct fp_fingerprint_device_t* device,
                         fp_sensor_event_callback_t* callback,
                         fp_exit_condition_t* shouldExit, void* user);
    /**
     * captureImage shall block untill the capture is completed or the abort_condition
     * is set. All callbacks must be executed on the calling thread.
     */
    int32_t (*captureImage)(struct fp_fingerprint_device_t* device, fp_image_t* image);
    int32_t (*captureSingleFrame)(struct fp_fingerprint_device_t* device, fp_image_t* image);
    int32_t (*fpsensor_retry_capture_single_frame)(struct fp_fingerprint_device_t* device, fp_image_t* image);

    int32_t (*setProperty)(struct fp_fingerprint_device_t* device, const char* property, uint32_t value);
    /**
     * must return fp_image_t with all fields of
     */
    fp_image_format_t (*getImageFormat)(struct fp_fingerprint_device_t* device);

    void (*getIalgorithm)(ialgorithm_t* ialgorithm, size_t size);
    /* add for extension interface*/
    int32_t (*captureCheckBoard)(struct fp_fingerprint_device_t* device, fp_image_t* image, int inv);
    int (*doNavigation)(struct fp_fingerprint_device_t* _device);
    void(*print_fpsensor_hal_info)();
    int32_t (*fpsensor_wait_finger_up)(struct fp_fingerprint_device_t *device);
    void (*reserved_function[9])();
} fp_fingerprint_device_t;
#endif // FINGERPRINT_SENSOR_H
