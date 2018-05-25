
#ifndef _SLFPAPI_H_
#define _SLFPAPI_H_

#include "fpsvc_types.h"
#include "metatypes.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct __attribute__((__packed__)) {
    UInt64    data;
} silead_unsigned_64;

int SLFPAPI_GetFrmBuffReady();
void SLFPAPI_ClearFrmBuffReady();
int SLFPAPI_RxFrmBuffReadyInd(int length);

int SLFPAPI_StorageRdUpSetEnable(int is_enable);
int SLFPAPI_StorageRdUpIsEnable(void);

int SLFPAPI_InitFPService(
	int	alg_frame_w,
	int	alg_frame_h,
	int	alg_frame_ppi
	);

int SLFPAPI_DeinitFPService(void);

int SLFPAPI_InitAlg(int alg_frame_w,int alg_frame_h,int alg_frame_ppi);

int SLFPAPI_IsFingersEnabled();

void SLFPAPI_FPService_reset();
int SLFPAPI_GetFunctionKeyState();
int SLFPAPI_GetIndexedFunctionKeyState(int index);
int SLFPAPI_SetIndexedFunctionKeyState(int index, int state);

/*
 * Class:     com_android_server_fpservice
 * Method:    EnrollCredential
 * Signature: (I)I
 */
//modified by wells for Android M begin
int SLFPAPI_EnrollCredential(int,int*,int*, SLFpsvcFPEnrollParams *,int);
//modified by wells for Android M end

/*
 * Class:     com_android_server_fpservice
 * Method:    IdentifyCredential
 * Signature: (I)I
 */
//modified by wells for Android M begin
int SLFPAPI_IdentifyCredential(int,int*,SLFpsvcIndex_t *,UInt64,UInt64);
//modified by wells for Android M end

int SLFPAPI_IdentifyUpdateTemp(int index);

int SLFPAPI_GetAuthenticatorId();

int SLFPAPI_GetLastIdentifiedTokenParams(SLFpsvcFPEnrollParams *pParams);
int SLFPAPI_SetLastIdentifiedTokenParams(SLFpsvcFPEnrollParams *pParams);
int SLFPAPI_LastIdentifiedIndexInvalidate(void);
int SLFPAPI_GetLastIdentifiedIndex(void);
int SLFPAPI_GetFpFid(int index, UInt32 *pFid);
int SLFPAPI_GetFpName(int index, AInfAliFingerName_t* pFingerName);
int SLFPAPI_GetEnrolledFidList(AInfAliIdList_t *pFidList);
int SLFPAPI_WriteFpInfo(void);

/*
 * Class:     com_android_server_fpservice
 * Method:    RemoveCredential
 * Signature: (I)I
 */
int SLFPAPI_RemoveCredential(int);

/*
 * Class:     com_android_server_fpservice
 * Method:    EnalbeCredential
 * Signature: (IZ)I
 */
int SLFPAPI_EnalbeCredential(int, int);

/*
 * Class:     com_android_server_fpservice
 * Method:    GetEnableCredential
 * Signature: (I)Z
 */
int SLFPAPI_GetEnableCredential(int);

int SLFPAPI_GetFPInfo(SLFpsvcIndex_t *opFpInfo);

int SLFPAPI_SetFPInfo(SLFpsvcIndex_t *opFpInfo);

int SLFPAPI_SwitchUser(int userid);

int SLFPAPI_DeleteUser(int userid);

int SLFPAPI_GetWakeUpState();

int SLFPAPI_GetVirtualKeyState();

int SLFPAPI_GetVirtualKeyStateExpand(const char* keytype);

int SLFPAPI_GetFingerPrintState();

int SLFPAPI_GetVirtualKeyCode(FP_VKeyType_t keytype);

int SLFPAPI_GetVirtualKeyCodeExpand(const char* keytype);

int SLFPAPI_SetWakeUpState(int WakeUpState);

int SLFPAPI_SetVirtualKeyState(int VirtualKeyState);

int SLFPAPI_SetVirtualKeyStateExpand(const char* keytype,int instate);

int SLFPAPI_SetFingerPrintState(int FingerPrintState);

int SLFPAPI_SetVirtualKeyCode(FP_VKeyType_t keytype, int VirtualKeyCode);

int SLFPAPI_SetVirtualKeyCodeExpand(const char* keytype,int keycode);

int SLFPAPI_SetPowerFuncKeyState(int functionkeystate);

int SLFPAPI_GetPowerFuncKeyState();

int SLFPAPI_SetIdleFuncKeyState(int functionkeystate);

int SLFPAPI_GetIdleFuncKeyState();

int SLFPAPI_SetMaxEnrollNum(int enrollnum);

//int SLFPAPI_SetFingerPrintModuleState(int FPModuleState);
int SLFPAPI_AutoSetFPModuleState(FP_OPERATION_t fpOperator);

int SLFPAPI_PreSetFPModuleState(int IsFpExist);

int SLFPAPI_PreGetFPInfoExist();

//ali_fp
/*typedef struct
{
	int  length;
	char fpname[FINGERNAMELEN*2+1];
}__attribute__((packed)) AInfAliFingerName_t;*/

/*typedef struct
{
	int  idcount;
	int  idlist[5+1];
}AInfAliIdList_t;*/

int SLFPAPI_ALI_EnrollCredential(int* percent);
int SLFPAPI_ALI_EnrollFinish(int uid, int fpid);
//int SLFPAPI_ALI_IdentifyCredential(int uid, int* fpid);
int SLFPAPI_ALI_IdentifyCredential(int uid, AInfAliIdList_t* pIdList);
//int SLFPAPI_ALI_GetUids(int *uids, int *uidsCount);
int SLFPAPI_ALI_GetUids(AInfAliIdList_t* pIdList);
//int SLFPAPI_ALI_GetFpids(int uid, int *ids, int *idsCount);
int SLFPAPI_ALI_GetFpids(int uid, AInfAliIdList_t* pIdList);
//int SLFPAPI_ALI_GetFingerName(int uid, int fpid, char *name, int *len);
int SLFPAPI_ALI_GetFingerName(int uid, int fpid, AInfAliFingerName_t* pFpName);
//int SLFPAPI_ALI_SetFingerName(int uid, int fpid, char *name, int len);
int SLFPAPI_ALI_SetFingerName(int uid, int fpid, AInfAliFingerName_t* pFpName);
int SLFPAPI_ALI_DeleteUid(int uid);
int SLFPAPI_ALI_DeleteFpid(int uid, int fpid);

#ifdef __cplusplus
}
#endif
#endif
