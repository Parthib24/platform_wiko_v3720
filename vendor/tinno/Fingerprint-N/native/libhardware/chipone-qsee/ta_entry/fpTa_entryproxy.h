#ifndef FP_TA_IMPL_H
#define FP_TA_IMPL_H


#include <inttypes.h>
#include "fp_ta_proxy.h"
#include <semaphore.h>
#include <pthread.h>



#define MIN(a,b)  (a) < (b) ? (a) : (b)


class fpGesture;
class fpGpioHal;
class fpTaCmdProcessor;
class fpTestIntf;

typedef enum
{
    THREAD_IDLE_MODE = 0x0,
    THREAD_CAPTURE_MODE,
    THREAD_GESTURE_MODE,
} capture_mode_t;

class fpTaEntryProxy : public fpTaProxy
{
  public:
    fpTaEntryProxy(fpTacImpl *pTacImpl);
    virtual ~fpTaEntryProxy();
    virtual uint64_t open_hal(void);
    virtual int32_t close_hal(void);
    virtual uint64_t pre_enroll(void);
    virtual int32_t set_active_group(int32_t igid, char *pPath);

    virtual int32_t authorize_enrol(const hw_auth_token_t *hat);

    virtual int32_t start_enroll(int32_t igid, int32_t *fid);
    virtual int32_t finish_enroll(void);
    virtual int32_t enroll_img(int32_t *pLastProgress, int32_t *pTotalEnrollCnt, int32_t *pCurEnrollCnt,
                           int32_t *pEnrollFailReason, int32_t *pFillPart);

    virtual int32_t enrol_store_template(int32_t iGid, int32_t *pFid);

    virtual int32_t capture_img(int32_t CaptyreStyle);
    virtual int32_t begin_authenticate(uint64_t challangeID);
    virtual int32_t authenticate( int32_t igid, int32_t *matchResult, int32_t *imatchFinger, bool forEnrollMatchImage,
                              hw_auth_token_t *p_token);
    virtual int32_t end_authenticate(int32_t *isTemplateUpdated,int32_t allow_update_template);
    virtual uint64_t get_authenticator_id();
    virtual int32_t get_enrolled_fids(int32_t *pEnrolledFids, int32_t iArrayCap, int32_t *pRealCnt);

    virtual int32_t delete_fid(int32_t gid, int32_t fid);
    virtual int32_t set_stop_status(bool newStatus);
    virtual int32_t nav_loop(void);
    virtual int32_t service_control(int32_t ipara1, int32_t ipara2);
    virtual int32_t print_system_info(void);
    virtual int32_t hal_configuration(int32_t tag,int32_t value);
    virtual int32_t get_image_quality(int32_t *p1,int32_t *p2,int32_t *p3);
    virtual int32_t get_image_size(int32_t *width,int32_t *height);
    virtual fpTestIntf *get_test_intf(void);
    virtual int32_t finger_detect_test(void);
    int32_t store_template(void);
    int32_t after_image_captured(void);
    int32_t fpCaptureSetMode(capture_mode_t mode);
    capture_mode_t fpCaptureGetMode(void);

    int32_t CaptureWaitFingerUp(void);
    int32_t CaptureWaitFingerDown(int32_t capture_style);
    int32_t NavWaitFingerDown(void);
    int32_t CaptureImageExecution(void);
    int32_t read_file(const char *path, char **p_buff, size_t *file_len);
    int32_t saveTpls2File(char *path, uint8_t *pFileContent, int32_t iFileLen);
    int32_t load_template(void);
    int32_t finger_up_wait_irq(void);
    int32_t check_board(void);
    int32_t self_test(void);
#ifdef FP_TEE_QSEE4
    int32_t qsee_set_auth_token_key(void);
#endif
//private:
    fpTaCmdProcessor *pCmdProcessor;
    fpGpioHal *pGpioHal;
    //char *TemplatesFileBuf;
    char TemplatePath[PATH_MAX];
    uint32_t chip_revision;
    uint32_t chip_hardware_id;
};

#endif
