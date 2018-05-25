#include <stdint.h>
#include <qsee_prng.h>
#include <qsee_hmac.h>
#include <qsee_timer.h>
#include <qsee_heap.h>
#include <gpPersistObjCrypto.h>
#include <gpPersistObjFileIO.h>

#include "fp_log.h"

typedef struct {
    uint32_t data_len;
    uint8_t hmac[GPCRYPTO_HMAC_SIZE]; //QSEE_SHA256_HASH_SZ  32
    uint8_t iv[GPCRYPTO_IV_SIZE];     //QSEE_AES256_IV_SIZE  16
    uint8_t data[];
} fp_data_obj_t;

uint32_t fp_get_wrapped_size(uint32_t data_size)
{
    return sizeof(fp_data_obj_t) + data_size;
}

int32_t fp_wrap_crypto(uint8_t* data, uint32_t data_size, uint8_t* enc_data, uint32_t* enc_data_size)
{
    uint32_t ret;
    fp_data_obj_t *obj = NULL;

    LOGI("%s: begin", __func__);

    if (NULL == data || NULL == enc_data || NULL == enc_data_size ) {
        LOGE("%s: Input parameters invalid", __func__);
         return -1;
    }

    obj = (fp_data_obj_t *)qsee_malloc(sizeof(fp_data_obj_t) + data_size);
    if (NULL == obj) {
        LOGE("%s: Failed to allocate memory for obj", __func__);
        return -1;
    }

    obj->data_len = data_size;
    memcpy(obj->data, data, data_size);

    ret = gpCrypto_Encrypt(NULL, obj->data, obj->data_len, obj->iv, sizeof(obj->iv));
    if (0 != ret) {
        LOGE("%s: gpCrypto_Encrypt failed with error %d", __func__, ret);
        qsee_free(obj);
        return -2;
    }

    ret = gpCrypto_Integrity_Protect(NULL,
                                     obj->iv,
                                     obj->data_len + sizeof(obj->iv),
                                     NULL,
                                     0,
                                     obj->hmac,
                                     sizeof(obj->hmac));

    if (0 != ret) {
        LOGE("%s: gpCrypto_Integrity_Protect failed with error %d", __func__, ret);
        qsee_free(obj);
        return -3;
    }

    *enc_data_size = sizeof(*obj) + data_size;
    memcpy(enc_data, obj, *enc_data_size);
    qsee_free(obj);

    LOGE("%s: end", __func__);

    return 0;
}

int32_t fp_unwrap_crypto(uint8_t* enc_data, uint32_t enc_data_size, uint8_t **data, uint32_t *data_size)
{
    uint32_t ret = 0;
    fp_data_obj_t *obj = NULL;

    LOGE("%s: begin", __func__);

    if (NULL == data || NULL == enc_data) {
        return -1;
    }

    obj = (fp_data_obj_t *) enc_data;
    if (enc_data_size < obj->data_len + sizeof(fp_data_obj_t)) {
        LOGE("%s: Mismatch in size %u, data_len %u",
             __func__, enc_data_size, obj->data_len + sizeof(fp_data_obj_t));
        return -1;
    }

    ret = gpCrypto_Integrity_Verify(NULL,
                                    obj->iv,
                                    obj->data_len + sizeof(obj->iv),
                                    NULL,
                                    0,
                                    obj->hmac, sizeof(obj->hmac));

    if (0 != ret) {
        LOGE("%s: gpCrypto_Integrity_Verify failed with error %d", __func__, ret);
        return -2;
    }

    ret  = gpCrypto_Decrypt(NULL, obj->data, obj->data_len, obj->iv, sizeof(obj->iv));

    if (0 != ret) {
        LOGE("%s: gpCrypto_Decrypt failed with error %d",__func__,  ret);
        return -3;
    }

    if (NULL == *data) {
        *data = obj->data;
    } else {
        memcpy(*data, obj->data, obj->data_len);
    }
    *data_size = obj->data_len;

    LOGE("%s: end", __func__);

    return 0;
}
