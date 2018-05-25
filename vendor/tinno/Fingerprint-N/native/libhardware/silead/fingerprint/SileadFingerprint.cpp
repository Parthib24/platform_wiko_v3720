#include "SileadFingerprint.h"
#include "mxml.h"
#include <cutils/properties.h>
#include <dirent.h>

static int lastGid = -1;

SileadFingerprint* SileadFingerprint::sInstance = NULL;

SileadFingerprint::SileadFingerprint() :
        mNotifyFunc(NULL), mLastPercent(0), mStep(0) {
}

SileadFingerprint::~SileadFingerprint() {
}

void SileadFingerprint::identifyCredentialRSP(int index, int result,
        int fingerid, int userId, SLFpsvcFPEnrollParams *pParams) {
    ALOGD(" SLCODE SileadFingerprint identifyCredentialRSP, index=%d, result=%d, fingerid=%d, userId=%d\n",
            index, result, fingerid, userId);
    if (pParams) {
        ALOGD(" SLCODE pParams not null");
    } else {
        ALOGD(" SLCODE pParams is null");
    }
    if (mNotifyFunc == NULL) {
        ALOGE(" SLCODE Invalid mNotifyFunc ptr");
        return;
    }
    switch (result) {
        case SL_IDENTIFY_WAKEUP_MATCHED:
        case SL_SUCCESS: {
            mMsg.type = FINGERPRINT_AUTHENTICATED;
            mMsg.data.authenticated.finger.fid = fingerid + 1;
            mMsg.data.authenticated.finger.gid = userId;
            if (pParams) {
                memcpy(&mMsg.data.authenticated.hat, pParams->token,
                        sizeof(hw_auth_token_t));
            }
            mNotifyFunc(&mMsg);
            ALOGD(" SLCODE notify the authenticated end");
            break;
        }
        case SL_DETECT_FINGER_OK: {
            ALOGD(" SLCODE SL_DETECT_FINGER_OK notify the onAcquired begin");
            mMsg.type = FINGERPRINT_ACQUIRED;
            mMsg.data.acquired.acquired_info = FINGERPRINT_ACQUIRED_GOOD;
            mNotifyFunc(&mMsg);
            ALOGD(" SLCODE notify the acquired end");
            break;
        }
        case SL_IDENTIFY_WAKEUP_NOT_MATCHED:
        case SL_IDENTIFY_ERR_MATCH: {
            ALOGD(" SLCODE notify the onAcquired begin");
            mMsg.type = FINGERPRINT_ACQUIRED;
            mMsg.data.acquired.acquired_info = FINGERPRINT_ACQUIRED_GOOD;
            mNotifyFunc(&mMsg);
            ALOGD(" SLCODE notify the acquired end");

            mMsg.type = FINGERPRINT_AUTHENTICATED;
            mMsg.data.authenticated.finger.fid = 0;
            mMsg.data.authenticated.finger.gid = userId;
            mNotifyFunc(&mMsg);
            ALOGD(" SLCODE notify the authenticated end");
            break;
        }
        case SL_IDENTIFY_CANCELED:
        case SL_IDENTIFY_ERROR:
        case SL_IDENTIFY_FAIL: {
            int errInfo = FINGERPRINT_ERROR_CANCELED;
            if (result == SL_IDENTIFY_CANCELED) {
                errInfo = FINGERPRINT_ERROR_CANCELED;
            }
            ALOGD(" SLCODE notify identifyCredentialRSP, errorInfo=%d",
                    errInfo);
            mMsg.type = FINGERPRINT_ERROR;
            mMsg.data.error = (fingerprint_error_t) errInfo;
            mNotifyFunc(&mMsg);
            break;
        }
        case SL_IDENTIFY_TMEOUT:
        case SL_IDENTIFY_CURR_IMG_BAD:
        case SL_TOUCH_TOO_FAST: {
            int acquiredInfo = SL_IDENTIFY_CURR_IMG_BAD;
            if (result == SL_IDENTIFY_TMEOUT) {
                acquiredInfo = FINGERPRINT_ACQUIRED_TOO_SLOW;
            } else if (result == SL_IDENTIFY_CURR_IMG_BAD) {
                acquiredInfo = FINGERPRINT_ACQUIRED_IMAGER_DIRTY;
            } else if (result == SL_TOUCH_TOO_FAST) {
                acquiredInfo = FINGERPRINT_ACQUIRED_TOO_FAST;
            }
            ALOGD(" SLCODE notify identifyCredentialRSP, acquiredInfo=%d",
                    acquiredInfo);
            mMsg.type = FINGERPRINT_ACQUIRED;
            mMsg.data.acquired.acquired_info =
                    (fingerprint_acquired_info_t) acquiredInfo;
            mNotifyFunc(&mMsg);
            break;
        }
        // Added by Janning for ALI-YunOS begin
        case SL_MSG_VERIFY_FINGER_UP:
        case SL_MSG_VERIFY_FINGER_DOWN: {
            int acquireInfo = SL_MSG_VERIFY_FINGER_UP;
            if (result == SL_MSG_VERIFY_FINGER_UP) {
                acquireInfo = FINGERPRINT_ACQUIRED_VENDOR_FINGER_UP;
            } else if (result == SL_MSG_VERIFY_FINGER_DOWN) {
                acquireInfo = FINGERPRINT_ACQUIRED_VENDOR_FINGER_DOWN;
            }
            ALOGD(" SLCODE identifyCredentialRSP, acquireInfo=%d,",
                    acquireInfo);
            mMsg.type = FINGERPRINT_ACQUIRED;
            mMsg.data.acquired.acquired_info =
                    (fingerprint_acquired_info_t) acquireInfo;
            mNotifyFunc(&mMsg);
            break;
        }
        // Added by Janning for ALI-YunOS end
        default: {
            break;
        }
    }
}

void SileadFingerprint::enrollCredentialRSP(int index, int percent, int result,
        int area, int userid) {
    ALOGD(" SLCODE SileadFingerprint enrollCredentialRSP, index=%d, mLastPercent=%d, percent=%d, result=%d, area=%d",
            index, mLastPercent, percent, result, area);
    if (mNotifyFunc == NULL) {
        ALOGE(" SLCODE Invalid mNotifyFunc ptr");
        return;
    }
    //mClient->GetFPInfo(&mInfo);
    switch (result) {
        case SL_SUCCESS: {
            //Enroll success
            /*if(mLastPercent >= percent) {
             mLastPercent = 0;
             }
             int step = percent - mLastPercent;
             mLastPercent = percent;
             float remaing = (100.0f- percent)/step;
             int intRemaing = (int)remaing;
             if(remaing-intRemaing>0.5) {
             intRemaing++;
             }
             ALOGD("enrollCredentialRSP success intRemaing=%d, remaing=%f, step=%d",
             intRemaing, remaing, step);*/
            if (mLastPercent >= percent) {
                mLastPercent = 0;
            }
            if (mLastPercent == 0) {
                if (percent == 6) {
                    mStep = 15;//Modify mt6737 enroll 15 times.add by yinglong.tang
                } else {
                    mStep = 100 / percent;
                }
            }
            if (percent > mLastPercent) {
                if (mStep > 0) {
                    mStep--;
                }
            }
            mLastPercent = percent;
            int intRemaing = mStep;
            ALOGD(" SLCODE enrollCredentialRSP success intRemaing=%d",
                    intRemaing);
            mMsg.type = FINGERPRINT_TEMPLATE_ENROLLING;
            mMsg.data.enroll.finger.fid = index + 1;
            mMsg.data.enroll.finger.gid = userid;
            mMsg.data.enroll.samples_remaining = intRemaing;
            mNotifyFunc(&mMsg);
            break;
        }
        case SL_DETECT_FINGER_OK: {
            ALOGD(" SLCODE SL_DETECT_FINGER_OK notify the onAcquired begin");
            mMsg.type = FINGERPRINT_ACQUIRED;
            mMsg.data.acquired.acquired_info = FINGERPRINT_ACQUIRED_GOOD;
            mNotifyFunc(&mMsg);
            ALOGD(" SLCODE notify the acquired end");
            break;
        }
        case SL_NOT_SUPPORT:
        case SL_ENROLL_ERROR:
        case SL_ENROLL_FAIL:
        case SL_ENROLL_CURR_ENR_FAIL: {
            int errInfo = FINGERPRINT_ERROR_CANCELED;
            mMsg.type = FINGERPRINT_ERROR;
            ALOGD(" SLCODE enrollCredentialRSP, notify FINGERPRINT_ERROR_CANCELED");
            mMsg.data.error = (fingerprint_error_t) errInfo;
            mNotifyFunc(&mMsg);
            break;
        }
        case SL_CHECK_IMG_FAIL:
        case SL_ENROLL_CURR_IMG_BAD:
        case SL_ENROLL_CHECK_ERROR:
        case SL_ENROLL_ERROR_LOW_COVERAGE:
        case SL_ENROLL_ERROR_LOW_QUALITY:
        case SL_ENROLL_ERROR_SAME_AREA: {
            int acqiredInfo = FINGERPRINT_ACQUIRED_TOO_FAST;
            if (result == SL_ENROLL_CURR_IMG_BAD || result == SL_CHECK_IMG_FAIL
                    || result == SL_ENROLL_ERROR_LOW_QUALITY) {
                acqiredInfo = FINGERPRINT_ACQUIRED_INSUFFICIENT;
            } else if (result == SL_ENROLL_ERROR_LOW_COVERAGE) {
                acqiredInfo = FINGERPRINT_ACQUIRED_PARTIAL;
            } else if (result == SL_ENROLL_CHECK_ERROR) {
                acqiredInfo = FINGERPRINT_ACQUIRED_VENDOR_DUPLICATE_FINGER;
            } else if (result == SL_ENROLL_ERROR_SAME_AREA) {
                //acqiredInfo = FINGERPRINT_ACQUIRED_TOO_SLOW;
                acqiredInfo = FINGERPRINT_ACQUIRED_VENDOR_DUPLICATE_AREA;
            }
            ALOGD(" SLCODE enrollCredentialRSP, notify acqiredInfo=%d", acqiredInfo);
            mMsg.type = FINGERPRINT_ACQUIRED;
            mMsg.data.acquired.acquired_info =
                    (fingerprint_acquired_info_t) acqiredInfo;
            mNotifyFunc(&mMsg);
            break;
        }
    }
}

// Added by Janning begin
// Fix bug: on Android M the system user can't correspond to fingerPrint user
#define BUF_SIZE        1024
#define PATH_MAX        255
static const char *FILE_PATH = "/data/silead/sileadUsersMapInfo.xml";
static const char *DIRECTORY_PATH = "/data/silead/";
static const int USER_NUMBERS_LIMITS = 5;
static const int SLOT_USED           = 1;
static const int SLOT_UNUSED         = 0;
static const int SYS_USER_ID_UNUSED  = -1;

typedef struct t_mapInfo {
    int sysUserId[USER_NUMBERS_LIMITS];
    int fpUserId[USER_NUMBERS_LIMITS];
    int slot[USER_NUMBERS_LIMITS];
} mapInfo;
static mapInfo g_mapInfo;

int copyFile () {
    const char *sfWithPath = "/etc/silead/sysparms/sileadUsersMapInfo.xml";
    const char *tfWithPath = FILE_PATH;
    int fpR, fpW;
    char buffer[BUF_SIZE];
    int lenR, lenW;
    // umask(0) is used to change the file userGroup read/write permissions,
    // like cmd: chmod 777 sileadUsersMapInfo.xml
    umask(0);
    SL_LOGD("SLCODE enter createFile..\n");
    if ((fpR = open(sfWithPath, O_RDONLY, S_IRWXU | S_IRGRP | S_IROTH)) < 0) {
        SL_LOGD("SLCODE The file '%s' can not be read! \n", sfWithPath);
        return -1;
    }
    if ((fpW = open(tfWithPath, O_RDWR | O_CREAT | O_EXCL, 0777)) < 0) {
        SL_LOGD("SLCODE The file '%s' can not be create! \n", tfWithPath);
        close(fpR);
        return -1;
    }
    SL_LOGD("SLCODE open src file:%s and dst file:%s success!\n", sfWithPath,
            tfWithPath);
    memset(buffer, 0, BUF_SIZE);
    while ((lenR = read(fpR, buffer, BUF_SIZE)) > 0) {
        if ((lenW = write(fpW, buffer, lenR)) != lenR) {
            printf("SLCODE Write to file '%s' failed!\n", tfWithPath);
            close(fpR);
            close(fpW);
            return -1;
        }
        memset(buffer, 0, BUF_SIZE);
    }
    SL_LOGD("SLCODE copy file success!\n");
    //fflush(fpR);
    fsync(fpW);
    close(fpR);
    close(fpW);
    SL_LOGD("SLCODE leave createFile..\n");
    return 0;
}

void save_configs_file(const char * file_path) {
    FILE *fp = NULL;
    ALOGD("SLCODE SileadFingerprint save_configs_file, file_path = %s\n",
            file_path);
    fp = fopen(file_path, "wb");
    if (fp == NULL) {
        ALOGE("SLCODE SileadFingerprint save_configs_file, Open %s file failed(%d), %s\n",
                file_path, errno, strerror(errno));
        return;
    }
    fwrite(&g_mapInfo, sizeof(mapInfo), 1, fp);
    fclose(fp);
    fp = NULL;
}

void load_configs_file(const char * file_path) {
    FILE *fp = NULL;
    int isExist = access(file_path, F_OK);
    ALOGD("SLCODE SileadFingerprint load_configs_file, file: %s, isExist= %d",
            file_path, isExist);
    // if file exist return result is 0, otherwise return -1
    if (isExist < 0) {
        // umask(0) is used to change the file userGroup read/write permissions,
        // like cmd: chmod 777 sileadUsersMapInfo.xml
        umask(0);
        int fileOpen = open(file_path, O_RDWR | O_CREAT | O_EXCL, 0777);
        ALOGD("SLCODE SileadFingerprint load_configs_file, create file result = %d\n",
                fileOpen);
        close(fileOpen);
        g_mapInfo.sysUserId[0] = 0;
        g_mapInfo.fpUserId[0] = 0;
        g_mapInfo.slot[0] = SLOT_USED;
        for (int i = 1; i < USER_NUMBERS_LIMITS; i++) {
            g_mapInfo.sysUserId[i] = 110 + i;
            g_mapInfo.fpUserId[i] = i;
            g_mapInfo.slot[i] = SLOT_UNUSED;
        }
        save_configs_file(file_path);
    } else {
        fp = fopen(file_path, "rb");
        if (fp == NULL) {
            // first load file, the file is empty, so init g_mapInfo
            ALOGE("SLCODE SileadFingerprint load_configs_file, Open %s file failed(%d), %s\n",
                    file_path, errno, strerror(errno));
        } else {
            fread(&g_mapInfo, sizeof(mapInfo), 1, fp);
            fclose(fp);
            fp = NULL;
        }
    }
}

int remove_dir(const char *dirname) {
    int isExist = access(dirname, F_OK);
    // if file exist return result is 0, otherwise return -1
    if (isExist < 0) {
        ALOGE("SLCODE SileadFingerprint remove_dir, dir: %s, not exist not need remove\n",
                dirname);
        return 0;
    }
    DIR *dir;
    struct dirent *entry;
    char path[PATH_MAX];
    dir = opendir(dirname);
    if (dir == NULL) {
        ALOGE("SLCODE SileadFingerprint remove_dir, Open %s dir failed(%d), %s\n",
                dirname, errno, strerror(errno));
        return -1;
    }
    // first remove the file under the directory
    while ((entry = readdir(dir)) != NULL) {
        if (strcmp(entry->d_name, ".") && strcmp(entry->d_name, "..")) {
            snprintf(path, (size_t) PATH_MAX, "%s/%s", dirname, entry->d_name);
            if (entry->d_type == DT_DIR) {
                remove_dir(path);
            } else {
                unlink(path);
            }
        }
    }
    closedir(dir);
    // At last remove the directory
    int result = rmdir(dirname);
    if (result != 0) {
        ALOGE("SLCODE SileadFingerprint remove_dir, delete %s dir failed(%d), %s\n",
                dirname, errno, strerror(errno));
    }
    return result;
}

int32_t deleteFpUserInfo(AInfFpsvcClient* client, int32_t fpUserId, int32_t sysUserId) {
    if (fpUserId <= 0 || sysUserId <= 0) {
        ALOGE(" SLCODE SileadFingerprint deleteFpUserInfo, fpUserId or sysUserId invalide, return");
        return -1;
    }
    int result = -1;
    char fileName[128 + 1];
    snprintf(fileName, 128, "%s%d", DIRECTORY_PATH, sysUserId);
    ALOGD("SLCODE SileadFingerprint deleteFpUserInfo, fileName= %s\n",
            fileName);
    // delete the directory of this user
    result = remove_dir(fileName);
    if (result == 0) {
        ALOGD("SLCODE SileadFingerprint deleteFpUserInfo, delete %s file success\n",
                fileName);
    } else {
        // if not use fp.apk, so dir not exist need to deleteFpUser directly
        ALOGE("SLCODE SileadFingerprint deleteFpUserInfo, delete %s file failed(%d), %s\n",
                fileName, errno, strerror(errno));
    }
    // whatever dir not exist or delete fail, remove the fingerPrint user info
    // first switch to owner user
    if (client == NULL) {
        ALOGE("SLCODE SileadFingerprint deleteFpUserInfo, NO client \n");
        return -1;
    }
    result = client->SwitchUser(0);
    ALOGD("SLCODE SileadFingerprint deleteFpUserInfo, SwitchUser result=%d",
            result);
    // second delete the user which need to remove
    result = client->DeleteUser(fpUserId);
    ALOGD("SLCODE SileadFingerprint deleteFpUserInfo, DeleteUser result=%d",
            result);
    return result;
}

/**
 * fingerPrintUserId is between 0,1,2,3,4;
 * so need to according to systemUserId find the corresponding fingerPrintUserId.
 * sileadUsersMapInfo.xml saved relationship.
 */
void removeOldUserFromfpUser(AInfFpsvcClient* client) {
    mxml_node_t *sysUserTree;
    mxml_node_t *sysUsersNode, *sysUserNode;
    bool userIsExist = false;
    bool dataChanged = false;

    // load userlist.xml
    mxmlSetLoneWhitespaceOff(1);
    int fd = open("/data/system/users/userlist.xml", O_RDONLY);
    if (fd <= 0) {
        ALOGE("SLCODE SileadFingerprint removeOldUserFromfpUser, Open %s file failed(%d), %s\n",
                "userlist.xml", errno, strerror(errno));
        return;
    }
    ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser, loadFile tree!!\n");
    sysUserTree = mxmlLoadFd(NULL, fd, MXML_NO_CALLBACK);
    close(fd);

    if (sysUserTree) {
        sysUsersNode = mxmlFindElement(sysUserTree, sysUserTree, "users", NULL,
                NULL, MXML_DESCEND);
        if (sysUsersNode) {
            // find the sysUid user whether exist in userlist.xml
            ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser, curernt node name = %s\n",
                    sysUsersNode->value.element.name);
            for (int i = 1; i < USER_NUMBERS_LIMITS; i++) {
                userIsExist = false;
                ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser, g_mapInfo.sysUserId[%d] = %d\n",
                        i, g_mapInfo.sysUserId[i]);
                if (g_mapInfo.sysUserId[i] == SYS_USER_ID_UNUSED) {
                    continue;
                }
                char sysUid[64 + 1];
                snprintf(sysUid, 64, "%d", g_mapInfo.sysUserId[i]);
                sysUserNode = mxmlFindElement(sysUsersNode, sysUsersNode,
                        "user", "id", sysUid, MXML_DESCEND);
                if (sysUserNode) {
                    userIsExist = true;
                }
                ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser, sysUid = %s, isExist= %d\n",
                        sysUid, userIsExist);
                if (!userIsExist) {
                    // user not exist, delete fpInfo of this users
                    int result = deleteFpUserInfo(client, g_mapInfo.fpUserId[i],
                            g_mapInfo.sysUserId[i]);
                    ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser, deleteFpUserInfo result=%d",
                            result);
                    if (result == 0) {
                        dataChanged = true;
                        g_mapInfo.sysUserId[i] = SYS_USER_ID_UNUSED;
                        g_mapInfo.slot[i] = SLOT_UNUSED;
                    }
                }
            }
            ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser, dataChanged = %d\n",
                    dataChanged);
            if (dataChanged) {
                // save the mapInfo into file
                save_configs_file(FILE_PATH);
                ALOGD("SLCODE SileadFingerprint removeOldUserFromfpUser save_configs_file success\n");
            }
        }
    }

    // free memory
    if (sysUserTree) {
        mxmlDelete(sysUserTree);
    }
}

/**
 * newGroupId: the system user id.
 * fingerPrintUserId is between 0,1,2,3,4;
 * so need to according to systemUserId find the corresponding fingerPrintUserId.
 * sileadUsersMapInfo.xml saved relationship.
 */
int32_t addNewUserTofpUser(int32_t newGroupId) {
    int fpUserId = -1;

    load_configs_file(FILE_PATH);
    for (int i = 0; i < USER_NUMBERS_LIMITS; i++) {
        ALOGD("SLCODE SileadFingerprint addNewUserTofpUser, index= %d, sysUID= %d, fpUID= %d\n",
                i, g_mapInfo.sysUserId[i], g_mapInfo.fpUserId[i]);
        if (g_mapInfo.sysUserId[i] == newGroupId) {
            fpUserId = g_mapInfo.fpUserId[i];
            g_mapInfo.slot[i] = SLOT_USED;
            break;
        } else {
            if (g_mapInfo.slot[i] == SLOT_UNUSED) {
                fpUserId = g_mapInfo.fpUserId[i];
                g_mapInfo.slot[i] = SLOT_USED;
                g_mapInfo.sysUserId[i] = newGroupId;
                ALOGD("SLCODE SileadFingerprint addNewUserTofpUser, find index=%d\n",
                        i);
                break;
            }
        }
    }
    if (fpUserId < 0) {
        ALOGE("SLCODE SileadFingerprint addNewUserTofpUser, no availabe index, return\n");
    } else {
        // save the tree into file
        save_configs_file(FILE_PATH);
        ALOGD("SLCODE SileadFingerprint addNewUserTofpUser success, fpUserId =%d\n",
                fpUserId);
    }
    return fpUserId;
}

/**
 * newGroupId: the system user id.
 * fingerPrintUserId is between 0,1,2,3,4;
 * so need to according to systemUserId find the corresponding fingerPrintUserId.
 * sileadUsersMapInfo.xml saved relationship when create a new system user in Settings.apk.
 */
int32_t silead_getFpUserIdByGroupId(int32_t newGroupId) {
    int fpUserId = -1;
    load_configs_file(FILE_PATH);
    for (int i = 0; i < USER_NUMBERS_LIMITS; i++) {
        ALOGD("SLCODE SileadFingerprint getFpUserIdByGroupId, index= %d, sysUID= %d, fpUID= %d, slot= %d\n",
                i, g_mapInfo.sysUserId[i], g_mapInfo.fpUserId[i], g_mapInfo.slot[i]);
        if (g_mapInfo.sysUserId[i] == newGroupId) {
            fpUserId = g_mapInfo.fpUserId[i];
            break;
        }
    }
    ALOGD("SLCODE SileadFingerprint leave getFpUserIdByGroupId, newGroupId= %d, find it fpUserId= %d\n",
            newGroupId, fpUserId);
    return fpUserId;
}
// Added by Janning end

uint64_t SileadFingerprint::fingerprint_pre_enroll(struct fingerprint_device __unused *dev)
{
    ALOGD(" SLCODE SileadFingerprint preEnroll");
    if (!mClient) {
        ALOGD(" SLCODE preEnroll No client");
        return -1;
    }
    return mClient->PreEnroll();
}

int SileadFingerprint::fingerprint_post_enroll(struct fingerprint_device __unused *dev)
{
    ALOGD(" SLCODE SileadFingerprint PostEnroll");
    if (!mClient) {
        ALOGD(" SLCODE PostEnroll No client");
        return -1;
    }
    return mClient->PostEnroll();
}

int SileadFingerprint::fingerprint_enroll(struct fingerprint_device __unused *dev,
        const hw_auth_token_t __unused *hat,
        uint32_t __unused gid,
        uint32_t __unused timeout_sec)
{
    ALOGD("SLCODE SileadFingerprint enroll(gid=%d, timeout=%d), token=%p\n",
            gid, timeout_sec, hat);
    int32_t ret = 0;
    if (!mClient) {
        ALOGD("SLCODE enroll No client");
        return -1;
    } else {
        ALOGD(" SLCODE fingerprint_enroll(SetToMMode)\n");
        mClient->SetToMMode();
    }
    int32_t newGroupId = gid;
    mClient->GetFPInfo(&mInfo);
    ALOGD("SLCODE fingerprint_enroll, mInfo.userid=%d, gid=%d",
            mInfo.userid, gid);
    SLFpsvcFPEnrollParams enrollParams;
    memset(&enrollParams,0,sizeof(SLFpsvcFPEnrollParams));
    enrollParams.tokenSize = sizeof(hw_auth_token_t);
    memcpy(enrollParams.token,hat,sizeof(hw_auth_token_t));
    int enrollIndex = -1;
    if (mInfo.userid != newGroupId) {
        int value = fingerprint_set_active_group(NULL, newGroupId, NULL);
        if (newGroupId != 0 && value == -1) {
            ALOGE("SLCODE SileadFingerprint enroll, this gid can't enroll finger, newGroupId=%d",
                    newGroupId);
            return -1;
        }
        // after set_active_group need getFpInfo again
        mClient->GetFPInfo(&mInfo);
        ALOGD("SLCODE fingerprint_enroll, after set_active_group, mInfo.userid=%d, gid=%d",
                mInfo.userid, newGroupId);
    }

    //get enroll index
    for (int i=0; i < SL_SLOT_ALL; i++) {
        if (mInfo.fpinfo[i].slot == 0) {
            enrollIndex = i;
            break;
        }
    }
    mLastPercent = 0;
    if (enrollIndex != -1) {
        ret = mClient->EnrollCredential(enrollIndex, &enrollParams, timeout_sec * 1000);
    } else {
        ALOGD("SLCODE SileadFingerprint can not enroll more fingerprint");
        ret = -1;
    }
    return ret;
}

uint64_t SileadFingerprint::fingerprint_get_auth_id(struct fingerprint_device __unused *dev)
{
    ALOGD(" SLCODE SileadFingerprint enter get_auth_id");
    if (mClient == NULL) {
        ALOGE(" SLCODE SileadFingerprint get_auth_id NO client \n");
        return -1;
    }
    return mClient->GetAuthenticatorId();
}

int SileadFingerprint::fingerprint_cancel(struct fingerprint_device __unused *dev)
{
    ALOGD(" SLCODE SileadFingerprint enter cancel()\n");
    int ret = -1;
    if (!mClient) {
        ALOGD(" SLCODE fingerprint_cancel No client");
    } else {
        ret = mClient->FpCancelOperation();

        char prop[PROPERTY_VALUE_MAX];
        property_get("ro.build.version.sdk", prop, "0");
        int sdkint = atoi(prop);
        if (sdkint > 23) {
            int errInfo = FINGERPRINT_ERROR_CANCELED;
            mMsg.type = FINGERPRINT_ERROR;
            mMsg.data.error = (fingerprint_error_t) errInfo;
            mNotifyFunc(&mMsg);
        }
    }
    return ret;
}

int SileadFingerprint::fingerprint_remove(struct fingerprint_device __unused *dev,
        uint32_t __unused gid, uint32_t __unused fid)
{
    ALOGD(" SLCODE SileadFingerprint remove(fid=%d, gid=%d)\n", fid, gid);
    if (mClient == NULL) {
        ALOGE(" SLCODE remove NO client \n");
        return -1;
    }
    if (fid <= 0) {
        ALOGE(" SLCODE fingerprint_remove, fingerId invalide, return");
        return -1;
    }
    int ret,i,fingerlistempty = 1;
    mClient->GetFPInfo(&mInfo);
    int32_t originalUserId = mInfo.userid;
    ALOGD(" SLCODE fingerprint_remove, mInfo.userid=%d, groupId=%d", originalUserId, gid);
    if (originalUserId != gid) {
        int activeRet = fingerprint_set_active_group(NULL, gid, NULL);
        if (activeRet < 0) {
            ALOGE(" SLCODE SileadFingerprint remove, setActiveGroup failed, gId=%d not find corresponding fpUser",
                    gid);
            ret = -1;
        } else {
            ALOGD(" SLCODE SileadFingerprint remove, setActiveGroup success, begin to remove Finger");
            ret = mClient->RemoveCredential(fid - 1);
            // TODO: whether need to switch to original user
            //mClient->SwitchUser(originalUserId);
        }
    } else {
        ret = mClient->RemoveCredential(fid - 1);
    }
    ALOGD(" SLCODE SileadFingerprint remove, removeFingerprint result=%d", ret);
    if (mNotifyFunc != NULL) {
        ALOGD(" SLCODE SileadFingerprint remove callback not null");
        if (ret == 0) {
            mMsg.type = FINGERPRINT_TEMPLATE_REMOVED;
            mMsg.data.removed.finger.fid = fid;
            mMsg.data.removed.finger.gid = gid;
            mNotifyFunc(&mMsg);

            /*report cancel msg to set removeClient Null start */            
            int errInfo = FINGERPRINT_ERROR_CANCELED;            
            mMsg.type = FINGERPRINT_ERROR;           
            mMsg.data.error = (fingerprint_error_t) errInfo;            
            mNotifyFunc(&mMsg);			
            /*report cancel msg to set removeClient Null end */
        }
    } else {
        ALOGE(" SLCODE SileadFingerprint remove error callback is null");
    }

    mClient->GetFPInfo(&mInfo);
    
    for(i=0;i<SL_SLOT_ALL;i++)
    {
        if(mInfo.fpinfo[i].slot == 1 && mInfo.fpinfo[i].enable == 1)
        {
            fingerlistempty = 0;
            break;
        }
    }
    if(fingerlistempty)
    {
        mMsg.type = FINGERPRINT_TEMPLATE_REMOVED;
        mMsg.data.removed.finger.fid = 0;
        mMsg.data.removed.finger.gid = gid;
        mNotifyFunc(&mMsg);
    }
    
    return ret;
}

int SileadFingerprint::fingerprint_set_active_group(struct fingerprint_device __unused *dev,
        uint32_t __unused gid, const char __unused *store_path)
{
    ALOGD(" SLCODE SileadFingerprint setActiveGroup gid=%d, store_path=%s",
            gid, store_path);
    int ret = -1;
    if (!mClient) {
        ALOGE(" SLCODE SileadFingerprint setActiveGroup NO client \n");
    } else {
        // Added by Janning begin
        // Fix bug: on Android M the systemUser can't correspond to fingerPrintUser
        int32_t newFpUserId = -1;
        if (gid == 0) {
            // first need to create file to save mapInfo
            load_configs_file(FILE_PATH);
            if (gid == lastGid) {
                newFpUserId = 0;
            }
        } else {
            newFpUserId = silead_getFpUserIdByGroupId(gid);
        }
        if (newFpUserId < 0) {
            ALOGD("SLCODE SileadFingerprint setActiveGroup, gid= %d, lastGid = %d\n",
                    gid, lastGid);
            if (gid != lastGid) {
                // sync the userInfo with userlist.xml, delete the old not exist user
                removeOldUserFromfpUser(mClient);
                // if is a new user, add it into sileadUsersMapInfo.xml
                newFpUserId = addNewUserTofpUser(gid);
                if (newFpUserId < 0) {
                    ALOGE("SLCODE SileadFingerprint setActiveGroup, this fpUser is all in used!");
                    return -1;
                }
                lastGid = gid;
            } else {
                ALOGE("SLCODE SileadFingerprint setActiveGroup, this systemUser not find any fpUser!");
                return -1;
            }
        }
        mClient->GetFPInfo(&mInfo);
        if (newFpUserId >= 0 && mInfo.userid != newFpUserId) {
            ret = mClient->SwitchUser(newFpUserId);
        } else {
            ret = 0;
            ALOGD(" SLCODE SileadFingerprint setActiveGroup, fpUserId is same not need to switch!");
        }
        // Added by Janning end
    }
    return ret;
}

int SileadFingerprint::fingerprint_authenticate(struct fingerprint_device __unused *dev,
        uint64_t __unused operation_id, __unused uint32_t gid)
{
    ALOGD(" SLCODE SileadFingerprint authenticate(gid=%d)\n", gid);
    if (mClient == NULL) {
        ALOGE(" SLCODE fingerprint_authenticate NO client \n");
        return -1;
    } else {
        ALOGD(" SLCODE fingerprint_authenticate(SetToMMode)\n");
        mClient->SetToMMode();
    }
    int ret;
    mClient->GetFPInfo(&mInfo);
    ALOGD(" SLCODE SileadFingerprint authenticate, mInfo.userid=%d, groupId=%d",
            mInfo.userid, gid);
    if (mInfo.userid != gid) {
        int value = fingerprint_set_active_group(NULL, gid, NULL);
        if (gid != 0 && value == -1) {
            ALOGE("SLCODE SileadFingerprint authenticate, this gid can't authenticate with finger, newGroupId=%d",
                    gid);
            return -1;
        }
        ret = mClient->IdentifyCredential(0, operation_id);
    } else {
        ret = mClient->IdentifyCredential(0, operation_id);
    }
    return ret;
}

int SileadFingerprint::set_notify_callback(struct fingerprint_device *dev,
        fingerprint_notify_t notify) {
    ALOGD("SLCODE SileadFingerprint enter set_notify_callback\n");
    if (!notify) {
        ALOGE("SLCODE SileadFingerprint set_notify_callback, notify is null, return off\n");
        return -1;
    }
    if (dev) {
        dev->notify = notify;
    }
    mNotifyFunc = notify;
    return 0;
}

int SileadFingerprint::connect_silead_deamon() {
    ALOGD("SLCODE SileadFingerprint enter connect_silead_deamon\n");
    if (!mRelayer) {
        mRelayer =
                reinterpret_cast<AInfFpsvcFPApkRelayerCB*>(AInfFpsvcFPApkRelayerCB::Create());
    }

    if (!mClient) {
        mClient = AInfFpsvcClient::Create(mRelayer);
    }
    if (mClient) {
        mClient->SetToMMode();
    }
    return 0;
}

int32_t SileadFingerprint::SetFPScreenStatus(int32_t screenStatus) {
    ALOGD(" SLCODE SetFPScreenStatus screenStatus=%d \n", screenStatus);
    if (mClient == NULL) {
        ALOGE(" SLCODE SetFPScreenStatus NO client \n");
        return -1;
    }
    return mClient->SetFPScreenStatus(screenStatus);
}

int32_t SileadFingerprint::setFPEnableCredential(int32_t index,
        int32_t enable) {
    ALOGD(" SLCODE setFPEnableCredential index=%d ,enable=%d \n", index,
            enable);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPEnableCredential NO client \n");
        return -1;
    }
    mClient->GetFPInfo(&mInfo);
    mInfo.fpinfo[index - 1].enable = enable;
    return mClient->SetFPInfo(&mInfo);
}

int32_t SileadFingerprint::getFPEnableCredential(int32_t index) {
    ALOGD(" SLCODE getFPEnableCredential index=%d \n", index);
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPEnableCredential NO client \n");
        return -1;
    }
    mClient->GetFPInfo(&mInfo);
    return mInfo.fpinfo[index - 1].enable;
}

int32_t SileadFingerprint::setFPFunctionKeyState(int32_t index,
        int32_t enable) {
    ALOGD(" SLCODE setFPFunctionKeyState index=%d, enable=%d \n", index,
            enable);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPFunctionKeyState NO client \n");
        return -1;
    }
    mClient->GetFPInfo(&mInfo);
    mInfo.fpinfo[index - 1].functionkeyon = enable;
    return mClient->SetFPInfo(&mInfo);
}

int32_t SileadFingerprint::getFPFunctionKeyState(int32_t index) {
    ALOGD(" SLCODE getFPFunctionKeyState index=%d \n", index);
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPFunctionKeyState NO client \n");
        return -1;
    }
    mClient->GetFPInfo(&mInfo);
    return mInfo.fpinfo[index - 1].functionkeyon;
}

int32_t SileadFingerprint::getFPVirtualKeyCode() {
    ALOGD(" SLCODE getFPVirtualKeyCode");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPVirtualKeyCode NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_VIRTUAL_KEY_CODE;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPVirtualKeyCode(int VirtualKeyCode) {
    ALOGD(" SLCODE setFPVirtualKeyCode index=%d \n", VirtualKeyCode);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPVirtualKeyCode NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_VIRTUAL_KEY_CODE;
    return mClient->GeneralCall(param, VirtualKeyCode);
}

int32_t SileadFingerprint::getFPLongPressVirtualKeyCode() {
    ALOGD(" SLCODE setFPVirtualKeyCode\n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPLongPressVirtualKeyCode NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_VIRTUAL_KEY_CODE_LONG_PRESS;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPLongPressVirtualKeyCode(int VirtualKeyCode) {
    ALOGD(" SLCODE setFPLongPressVirtualKeyCode VirtualKeyCode=%d  \n",
            VirtualKeyCode);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPLongPressVirtualKeyCode NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_VIRTUAL_KEY_CODE_LONG_PRESS;
    return mClient->GeneralCall(param, VirtualKeyCode);
}

int32_t SileadFingerprint::getFPDouClickVirtualKeyCode() {
    ALOGD(" SLCODE getFPDouClickVirtualKeyCode \n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPDouClickVirtualKeyCode NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_VIRTUAL_KEY_CODE_DOUBULE_CLICK;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPDouClickVirtualKeyCode(int VirtualKeyCode) {
    ALOGD(" SLCODE setFPDouClickVirtualKeyCode VirtualKeyCode =%d \n",
            VirtualKeyCode);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPDouClickVirtualKeyCode NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_VIRTUAL_KEY_CODE_DOUBULE_CLICK;
    return mClient->GeneralCall(param, VirtualKeyCode);
}

int32_t SileadFingerprint::getFPVirtualKeyState() {
    ALOGD(" SLCODE getFPVirtualKeyState \n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPVirtualKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_VIRTUAL_KEY_STATE;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPVirtualKeyState(int VirtualKeyState) {
    ALOGD(" SLCODE setFPVirtualKeyState, VirtualKeyState=%d \n",
            VirtualKeyState);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPVirtualKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_VIRTUAL_KEY_STATE;
    return mClient->GeneralCall(param, VirtualKeyState);
}

int32_t SileadFingerprint::getFPWakeUpState() {
    ALOGD(" SLCODE getFPWakeUpState \n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPWakeUpState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_WAKE_UP_STATE;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPWakeUpState(int WakeUpState) {
    ALOGD(" SLCODE setFPWakeUpState WakeUpState=%d \n", WakeUpState);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPWakeUpState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_WAKE_UP_STATE;
    return mClient->GeneralCall(param, WakeUpState);
}

int32_t SileadFingerprint::getFingerPrintState() {
    ALOGD(" SLCODE getFingerPrintState \n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFingerPrintState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_FINGER_PRINT_STATE;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFingerPrintState(int32_t FingerPrintState) {
    ALOGD(" SLCODE setFingerPrintState FingerPrintState=%d\n",
            FingerPrintState);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFingerPrintState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_FINGER_PRINT_STATE;
    return mClient->GeneralCall(param, FingerPrintState);
}

int32_t SileadFingerprint::setFPPowerFuncKeyState(int32_t FuncKeyState) {
    ALOGD(" SLCODE setFPPowerFuncKeyState FuncKeyState=%d\n", FuncKeyState);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPPowerFuncKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_POWER_FUNC_KEY_STATE;
    return mClient->GeneralCall(param, FuncKeyState);
}

int32_t SileadFingerprint::getFPPowerFuncKeyState() {
    ALOGD(" SLCODE getFPPowerFuncKeyState \n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPPowerFuncKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_POWER_FUNC_KEY_STATE;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPIdleFuncKeyState(int32_t FuncKeyState) {
    ALOGD(" SLCODE setFPIdleFuncKeyState, FuncKeyState =%d \n", FuncKeyState);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPIdleFuncKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_IDLE_FUNC_KEY_STATE;
    return mClient->GeneralCall(param, FuncKeyState);
}

int32_t SileadFingerprint::getFPIdleFuncKeyState() {
    ALOGD(" SLCODE getFPIdleFuncKeyState \n");
    if (mClient == NULL) {
        ALOGE(" SLCODE getFPIdleFuncKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_GET_IDLE_FUNC_KEY_STATE;
    return mClient->GeneralCall(param, 0);
}

int32_t SileadFingerprint::setFPWholeFuncKeyState(int FuncKeyState) {
    ALOGD(" SLCODE setFPWholeFuncKeyState  FuncKeyState =%d \n", FuncKeyState);
    if (mClient == NULL) {
        ALOGE(" SLCODE setFPWholeFuncKeyState NO client \n");
        return -1;
    }
    GENERAL_PARAM param = SL_SET_WHOLE_FUNC_KEY_STATE;
    return mClient->GeneralCall(param, FuncKeyState);
}
