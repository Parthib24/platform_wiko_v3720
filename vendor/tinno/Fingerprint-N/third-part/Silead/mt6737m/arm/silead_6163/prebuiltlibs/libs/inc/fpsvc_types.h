
#ifndef __FPSVC_TYPES_H__
#define __FPSVC_TYPES_H__

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "metatypes.h"
/*
 * Define the base type
 */
typedef unsigned char			SL_UINT8;
typedef	unsigned short			SL_UINT16;
typedef unsigned int			SL_UINT32;
typedef long long unsigned int	SL_UINT64;
typedef signed char				SL_INT8;
typedef signed short			SL_INT16;
typedef signed int				SL_INT32;
typedef int						SL_BOOL;
typedef char					SL_CHAR;
typedef void					SL_VOID;

#define SL_TRUE					1
#define SL_FALSE				0
#define SL_NULL					0
#define ARRAY_SIZE(a)			(sizeof(a) / sizeof( (a)[0] ) )

#define  SL_TEMPLATE_FILE		"/data/data/com.android.FpTest"

/*
 * Define the error code
 */




#define         SL_SUCCESS                      0
#define 		SL_FAIL							-1
#define         SL_RSP_SESSION_BUSY_IN_VERI            -4
#define         SL_RSP_SESSION_BUSY_IN_ENRO            -5
#define         SL_RSP_SESSION_BUSY_IN_WAKEUP_VERI     -6


#define			SL_CHECK_IMG_FAIL				-1

#define         SL_ENROLL_CANCELED              -2
#define         SL_NOT_SUPPORT                  -3
#define         SL_ENROLL_ERROR					-4
#define         SL_ENROLL_FAIL					-105
#define         SL_ENROLL_CURR_ENR_FAIL			-106 //for current single enroll failure due to img is not suitable
#define         SL_ENROLL_CURR_IMG_BAD			-107 //for img is not good when finger is there.
#define         SL_ENROLL_CHECK_ERROR			-108 //add by arthur 2015.10.27
#define			SL_ENROLL_ERROR_LOW_COVERAGE	-109
#define			SL_ENROLL_ERROR_LOW_QUALITY		-110
#define			SL_ENROLL_ERROR_SAME_AREA		-111


#define 		SL_IDENTIFY_TMEOUT				-1
#define         SL_IDENTIFY_CANCELED            -2
#define         SL_IDENTIFY_ERR_MATCH           -3
#define         SL_IDENTIFY_ERROR               -4
#define         SL_IDENTIFY_FAIL               	-5
#define         SL_IDENTIFY_WAKEUP_NOT_MATCHED	-206
#define         SL_IDENTIFY_WAKEUP_MATCHED		-207
#define         SL_IDENTIFY_WAKEUP_BAD_IMG		-208
#define         SL_IDENTIFY_CURR_IMG_BAD		-209 //for img is not good when finger is there.

#define         SL_DETECT_FINGER_OK		        -301  //for img good when enrol or identify

#define         SL_WRONG_PARAM                  -1001
#define         SL_NOT_MATCH                    -1003
#define         SL_TOUCH_ERROR                  -1004
#define         SL_TOUCH_TOO_FAST               -1005
#define         SL_PRESS_TOO_LONG               -1006
#define         SL_FINGER_NOT_EXIST             -1008
#define         SL_DEVICE_NOT_READY             -1009
#define         SL_MEMORY_ERROR                 -1010
#define         SL_ALGORITHM_INIT_ERROR         -1011
#define			SL_LOAD_API_ERROR				-1013

#define         SL_UNKNOWN_ERROR                -1100
//for Ali Android.M
#define         SL_MSG_VERIFY_FINGER_UP         -10000
#define         SL_MSG_VERIFY_FINGER_DOWN       -10001


/*----------------------------------------------------------
 * Define the command for enrollment or identification
 */
#define SL_ABS(x) (((x) >= 0) ? (x) : -(x))
#define SL_SQR(x) ((x) * (x))

#define SLFPAPI_FPSTATE_IDLE				0
#define SLFPAPI_FPSTATE_VERI				1
#define SLFPAPI_FPSTATE_ENRO				2
#define SLFPAPI_FPSTATE_VERI_FOR_WAKEUP		3
#define	SLFPAPI_FPSTATE_PRE_ALI_STATE		4

//add by wells begin
#define  SLFPAPI_ENROLL_TOKEN_SIZE  69
//add by wells end

#define FINGERNAMELEN			32
#define FINGER_USERDATA_LEN		256

#define VERIFY_RETRY_TIMES		5

typedef struct {
	UInt32 u32MagicNumber;
 	UInt32 u32Crc;
	UInt32 u32Crypt;
	UInt32 u32Length;
} __attribute__((packed)) SLFpsvcParaHead_t;

/*---------------------------------------------------------*
 * Fingerprint Algorithm Context
 *---------------------------------------------------------*/
typedef void  SL_CONTEXT;

/*---------------------------------------------------------*
 * Fingerprint Sensor Parameters
 *---------------------------------------------------------*/
typedef struct
{
	int  length;
	char fpname[FINGERNAMELEN*2+1];
}__attribute__((packed)) AInfAliFingerName_t;

typedef struct
{
	int  idcount;
	int  idlist[5+1];
}__attribute__((packed)) AInfAliIdList_t;


typedef enum {
	SL_SENSOR_WIDTH,
	SL_SENSOR_HEIGHT,
	SL_SENSOR_DPI,
	SL_UNKNOWN_PARAM
} SL_DEVICE_PARAM, *PSL_DEVICE_PARAM;

/*---------------------------------------------------------*
 * Slot index for fingerprint template enroll/identify
 *---------------------------------------------------------*/
typedef enum {
	SL_SLOT_0,
	SL_SLOT_1,
	SL_SLOT_2,
	SL_SLOT_3,
	SL_SLOT_4,
	SL_SLOT_ALL = 5,
	SL_UNKNOW_SLOT
} FP_SLOT_INDEX;

#define  EXPORT
#define  IMPORT

//#define SL_MAX_ENROLL_COUNT				8
//#define SL_MIN_ENROLL_COUNT				3


typedef SL_BOOL         (*FingerDetectFunc)(void* frame);

typedef struct __attribute__((__packed__)) {
    UInt8 version;  // Current version is 0
    UInt64 challenge;
    UInt64 user_id;             // secure user ID, not Android user ID
    UInt64 authenticator_id;    // secure authenticator ID
    UInt32 authenticator_type;  // hw_authenticator_type_t, in network order
    UInt64 timestamp;           // in network order
    UInt8 hmac[32];
} silead_hw_auth_token_t;

//add by wells for Android M begin
typedef struct {
    int     tokenSize;
    UInt8 token[SLFPAPI_ENROLL_TOKEN_SIZE];
}__attribute__((packed))  SLFpsvcFPEnrollParams;
//add by wells for Android M end


typedef struct {
	UInt32 index;
	UInt32 slot;
	UInt32 enable;
	UInt32 functionkeyon;
    SLFpsvcFPEnrollParams   enrollParams;  //add by wells for Android M
	UInt8 fingername[FINGERNAMELEN*2+1];
	UInt8 userdata[FINGER_USERDATA_LEN+1];
} __attribute__((packed)) SLFpsvcFPInfo_t;

typedef struct {
	UInt32 total;
 	UInt32 max;
	UInt32 wenable;
	UInt32 frame_w;
	UInt32 frame_h;
	UInt32 userid;
	UInt32 usernow;
	SLFpsvcFPInfo_t fpinfo[SL_SLOT_ALL];
} __attribute__((packed)) SLFpsvcIndex_t;

typedef struct {
	UInt32 u32RsvdSz1[8];
	UInt8 u8RsvdSz1[256];
	int iRsvdSz1[36];
	int iRsvd1;/*if need use several bytes Rsvd space, first use this, then up*/
	int wakeupstate;
	int virtualkeystate;
	int fingerprintstate;
	int virtualkeycodetap;
	int virtualkeycodelpress;
	int virtualkeycodedouclick;
	int virtualkeycodeslideup;
	int virtualkeycodeslidedn;
	int virtualkeycodeslidelt;
	int virtualkeycodeslidert;
	int virtualkeystatetap;
	int virtualkeystatelpress;
	int virtualkeystatedouclick;
	int virtualkeystateslideup;
	int virtualkeystateslidedn;
	int virtualkeystateslidelt;
	int virtualkeystateslidert;
	int powerfunckeystate;
	int idlefunckeystate;
	SLFpsvcIndex_t fpindex;
}__attribute__((packed))  SLFpStorage;

typedef struct {
	SLFpsvcParaHead_t fpsvcParaHead;
	SLFpStorage fpStorageData[2];
}__attribute__((packed))  SLFpsvcStoragePara_t;

typedef enum {
	SL_GET_WAKE_UP_STATE,
	SL_SET_WAKE_UP_STATE,
	SL_GET_VIRTUAL_KEY_STATE,
	SL_SET_VIRTUAL_KEY_STATE,
	SL_GET_FINGER_PRINT_STATE,
	SL_SET_FINGER_PRINT_STATE,
	SL_GET_VIRTUAL_KEY_CODE,
	SL_SET_VIRTUAL_KEY_CODE,
	SL_GET_VIRTUAL_KEY_CODE_LONG_PRESS,
	SL_SET_VIRTUAL_KEY_CODE_LONG_PRESS,
	SL_GET_VIRTUAL_KEY_CODE_DOUBULE_CLICK,
	SL_SET_VIRTUAL_KEY_CODE_DOUBULE_CLICK,
	SL_SET_POWER_FUNC_KEY_STATE,
	SL_GET_POWER_FUNC_KEY_STATE,
	SL_SET_IDLE_FUNC_KEY_STATE,
	SL_GET_IDLE_FUNC_KEY_STATE,
	SL_SET_WHOLE_FUNC_KEY_STATE,
	SL_SET_MAX_ENROLL_NUM,
	SL_SET_FP_MODULE_STATE	
} GENERAL_PARAM;

#define VEKY_TAP		0
#define VEKY_LONGPRESS	1
#define VEKY_DOUCLICK	2
typedef int FP_VKeyType_t;

typedef enum {
	SL_FP_ADD,
	SL_FP_REMOVE
} FP_OPERATION_t;

#define STRING_CONTAINER_LENGTH  255

typedef struct {
    char context[STRING_CONTAINER_LENGTH+1];
}__attribute__((packed))   StringContainer_t;



#endif
