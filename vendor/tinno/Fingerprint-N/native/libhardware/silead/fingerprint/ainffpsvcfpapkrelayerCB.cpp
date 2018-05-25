/*  ====================================================================================================
 **
 **
 **  ---------------------------------------------------------------------------------------------------
 **
 **	File Name:  	
 **	
 **	Description:	This file contains the interface for the Buffer Control.
 **
 **					this is kernal code of SW framework.
 **					It contributes one of functionalities of SW Platform. 
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

#include <unistd.h>
#include <utils/Log.h>


#include <hardware/hardware.h>
#include "fingerprint.h"
#include "SileadFingerprint.h"
#include "ainffpsvcfpapkrelayerCB.h"

AInfFpsvcFPApkRelayerCB* AInfFpsvcFPApkRelayerCB::s_pAInfFpsvcFPApkRelayerCB =
        NULL;

AInfFpsvcFPApkRelayerCB::AInfFpsvcFPApkRelayerCB() :
        AInfFpsvcRelayerCB() {
}

AInfFpsvcFPApkRelayerCB::~AInfFpsvcFPApkRelayerCB() {
    s_pAInfFpsvcFPApkRelayerCB = NULL;
}

AInterface* AInfFpsvcFPApkRelayerCB::OnInfDuplicate() {
    return new AInfFpsvcFPApkRelayerCB();
}

AInfFpsvcFPApkRelayerCB* AInfFpsvcFPApkRelayerCB::Create() {
    if (s_pAInfFpsvcFPApkRelayerCB == NULL) {
        s_pAInfFpsvcFPApkRelayerCB = new AInfFpsvcFPApkRelayerCB();
    }

    return (AInfFpsvcFPApkRelayerCB*) s_pAInfFpsvcFPApkRelayerCB;
}

/////////////////////////////////////////////////////
///INTERFACE///////////
/////////////////////////////////////////////////////

/*
 * Class:     com_android_server_fpservice
 * Method:    EnrollCredential_CB
 * Signature: (I)I
 * if return != 0, pls play VIB;
 * -2: curren enroll failure;
 * -3: abort whole enroll transaction;
 */
int AInfFpsvcFPApkRelayerCB::EnrollCredential_CB(int index, int percent,
        int inret, int area, int userid) {
    ALOGD("[SL_FP_CB]--AInfFpsvcFPApkRelayerCB::EnrollCredential_CB-index=%d-inret=%d-pid=%d,percent=%d,area=%d\n",
            index, inret, getpid(), percent, area);

    SileadFingerprint* fingerprint = SileadFingerprint::getInstance();
    if (fingerprint) {
        ALOGD("!proxy,callback the result");
        fingerprint->enrollCredentialRSP(index, percent, inret, area, userid);
    } else {
        ALOGD("fingerprint is null");
    }
    return 0;
}

/*
 * Class:     com_android_server_fpservice
 * Method:    IdentifyCredential_CB
 * Signature: (I)I
 */
int AInfFpsvcFPApkRelayerCB::IdentifyCredential_CB(int index, int inret,
        int fingerid, int userId, SLFpsvcFPEnrollParams *pParams) {
    ALOGD("[SL_FP_CB]--AInfFpsvcFPApkRelayerCB::IdentifyCredential_CB-index=%d-inret=%d-pid=%d, fingerid=%d\n",
            index, inret, getpid(), fingerid);

    SileadFingerprint* fingerprint = SileadFingerprint::getInstance();
    if (fingerprint) {
        fingerprint->identifyCredentialRSP(index, inret, fingerid, userId,
                pParams);
    } else {
        ALOGD("fingerprint is null");
    }
    return 0;
}

int AInfFpsvcFPApkRelayerCB::slfpkey_CB(int slfpkey) {
    ALOGD("[SL_FP_CB]--AInfFpsvcFPApkRelayerCB::IdentifyCredential_CB- slfpkey = %d-pid=%d\n",
            slfpkey, getpid());
    //slfpkeyRSP (slfpkey);
    return 0;
}
