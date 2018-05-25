#include <stdint.h>
#include <qsee_hmac.h>
#include <qsee_heap.h>
#include <qsee_services.h>
#include <qsee_message.h>

#include "fp_log.h"

#define FP_TA_KEYMASTER_NAME "keymaster"

uint32_t qsee_auth_token_key_size = 0;
uint8_t qsee_auth_token_key[160 * 160] = {0};

int fp_ta_hw_auth_unwrap_key(uint8_t *encrypted_key,
                             uint32_t size_encrypted_key,
                             uint8_t *key, uint32_t *size_key)
{
	int i = 0;
    int status = 0;
    char send_app_name[256] = { 0 };

    LOGD("%s invoked", __func__);
    status = qsee_decapsulate_inter_app_message(send_app_name,
                                                    encrypted_key,
                                                    size_encrypted_key,
                                                    key,
                                                    size_key);
    if (status)
    {
        LOGE("%s: decapsulate returns error %d", __func__, status);
        return -1;
    }

    if (0 != strncmp(send_app_name, FP_TA_KEYMASTER_NAME, sizeof(send_app_name)))
    {
        LOGE("%s: received message from wrong app: %s", __func__, send_app_name);
        return -2;
    }

    LOGD("%s success, send_app_name:%s", __func__, send_app_name);
    LOGD("%s ta->hmac_size %d", __func__, *size_key);
    if (*size_key >= 8)
        LOGD("%X, %X, %X, %X, %X, %X, %X, %X", key[i], key[i+1], key[i+2], key[i+3], key[i+4], key[i+5], key[i+6], key[i+7]);
    return 0;
}

int fp_hmac_sha256(const uint8_t* data, uint32_t size_data,
                    const uint8_t* key, uint32_t size_key,
                    uint8_t* hmac)
{
    LOGD("%s invoked", __func__);
    if (qsee_hmac(QSEE_HMAC_SHA256, data, size_data, key, size_key, hmac)) {
        LOGE("qsee_hmac() failed");
        return -1;
    }

    LOGD("%s success", __func__);
    return 0;
}
