/*  ====================================================================================================
**
**  ---------------------------------------------------------------------------------------------------
**
**	File Name:  	
**	
**	Description:	This file contains the implementation of interface
**
**					this is kernal code of SW framework.
**					It contributes one of functionalities of SW Platform. 
**					If the checkin is CR not PR, to add change History to this file head part 
**					will be appreciated.
**
**  ---------------------------------------------------------------------------------------------------
**
**  Author:			Warren Zhao
**
** -------------------------------------------------------------------------
**
**	Change History:
**	
**	Initial revision
**
**====================================================================================================*/


#ifndef __AINTERFACE_FPSVC_H__
#define __AINTERFACE_FPSVC_H__

#include "ainterfaceNCB.h"
#include "fpsvc_types.h"
#include "slfpapi.h"

class AInfFpsvc : public AInterfaceNCB
{
public:
			/*
			 * Class:     com_android_server_fpservice
			 * Method:    ResetFPService
			 * Signature: ()I
			 */
			virtual int ResetFPService() = 0;

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    InitFPService
			 * Signature: ()I
			 */
//			virtual int InitFPService() = 0;

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    DeinitFPService
			 * Signature: ()V
			 */
//			virtual int DeinitFPService() = 0;

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    EnrollCredential
			 * Signature: (I)I
			 */
            //modified by wells for Android M begin
			//virtual int EnrollCredential(int) = 0;
            virtual int EnrollCredential(int index,SLFpsvcFPEnrollParams *pparam,int32_t timeout) = 0;
            //modified by wells for Android M end

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    IdentifyCredential
			 * Signature: (I)I
			 */
            //modified by wells for Android M begin
			//virtual int IdentifyCredential(int) = 0;
            virtual int IdentifyCredential(int,UInt64) = 0;
            //modified by wells for Android M end

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    RemoveCredential
			 * Signature: (I)I
			 */
			virtual int RemoveCredential(int) = 0;

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    EnalbeCredential
			 * Signature: (IZ)I
			 */
			virtual int EnalbeCredential(int, int) = 0;

			/*
			 * Class:     com_android_server_fpservice
			 * Method:    FpCancelOperation
			 * Signature: ()I
			 */
			virtual int FpCancelOperation() = 0;
			/*
			 * Class:     com_android_server_fpservice
			 * Method:    GetEnableCredential
			 * Signature: (I)Z
			 */
			virtual int GetEnableCredential(int) = 0;

			virtual int GetFPInfo(SLFpsvcIndex_t* opFpInfo) = 0;

			virtual int SetFPInfo(SLFpsvcIndex_t* opFpInfo) = 0;
			
			virtual int SwitchUser(int) = 0;

			virtual int DeleteUser(int) = 0;

			virtual int SetFPScreenStatus(int) = 0;

            //add by wells for Android M begin
            virtual long  PreEnroll() = 0;
            virtual int   PostEnroll() = 0;
            virtual long  GetAuthenticatorId() = 0;
            virtual int   SetToMMode() = 0;
            virtual int   ShutdownFpsvcd() = 0;
            //add by wells for Android M end

			//add by mat
			virtual int GetIndexedFunctionKeyState(int) = 0;
			virtual int SetIndexedFunctionKeyState(int, int) = 0;

			//add by tim
			virtual int GetVirtualKeyCodeExpand(const char* keytype) = 0;
			virtual int SetVirtualKeyCodeExpand(const char* keytype,int keycode) = 0;
			virtual int GetVirtualKeyStateExpand(const char* keytype) = 0;
			virtual int SetVirtualKeyStateExpand(const char* keytype,int instate) = 0;

			//For ali_fp
			virtual int AliEnrollCredential(int* ratio) = 0;
			virtual int AliFinishEnroll(int uid, int fpid) = 0;
			virtual int AliIdentifyCredential(int uid,AInfAliIdList_t *idlist) = 0;
			virtual int AliFingerDetect(void) = 0;
			virtual int AliFingerLeave(void) = 0;
			virtual int	AliGetUids(AInfAliIdList_t *idlist) = 0;
			virtual int AliGetFpids(int uid, AInfAliIdList_t *idlist) = 0;
			virtual int AliGetFingerName(int uid, int fpid, AInfAliFingerName_t *name) = 0;
			virtual int AliSetFingerName(int uid, int fpid,  AInfAliFingerName_t *name) = 0;
			virtual int AliDeleteUid(int uid) = 0;
			virtual int AliDeleteFpid(int uid, int fpid) = 0;

public:
			virtual				~AInfFpsvc();

protected:
			AInfFpsvc(
				int				isRelayer,
				ARemoteApi*		rApi,
				AInterface*		inInfCallBackRelayer
				);

			virtual AInterface*	OnInfCallBackClientCreate(
				const char	*inRelayerNameOfCallBackClient
				);

};



#endif 
