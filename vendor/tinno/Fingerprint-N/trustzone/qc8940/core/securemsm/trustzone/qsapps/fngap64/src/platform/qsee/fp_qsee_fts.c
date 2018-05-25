#include <stdio.h>
#include <qsee_heap.h>
#include <qsee_fs.h>
#include <qsee_timer.h>

#include <gpPersistObjCrypto.h>
#include <gpPersistObjFileIO.h>

#include "fp_log.h"
#include "fp_qsee_fs.h"

#define LOG_TAG
#define FP_QSEE_FTS_PERF    1
/*
 * QSEE Fast trusted storage
 * The FTS feature is supported on the 8994 LA 1.2 PL and it can provide a read throughput of
 * approximately 17 MBps on MSM8994. This means it takes approximately 17 ms to read one
 * 300 KB biometric template. This is in comparison to SFS whose throughput it typically less than
 * 1MBps.*
 * */

typedef struct 
{
  uint32_t data_len;
  unsigned char hmac[GPCRYPTO_HMAC_SIZE]; //QSEE_SHA256_HASH_SZ  32
  unsigned char iv[GPCRYPTO_IV_SIZE];     //QSEE_AES256_IV_SIZE  16
  unsigned char data[];
} fp_fts_obj_t;

int fp_tee_file_open(uint8_t *path, uint32_t mode, FP_ObjectHandle *so_handle)
{
    int fd = -1;

    if (path == NULL) {
        LOGE(LOGTAG "%s path is NULL, wrong!!!\n", __func__);
        return -1;
    }

    // just readonly support
    //if (mode != SO_MODE_READ) {
    //    LOGE(LOGTAG "%s mode is %d, wrong!!!\n", __func__, mode);
    //    return -1;
    //}

    fd = open((const char *)path, O_RDONLY);
    if (-1 == fd)
    {
        LOGE(LOGTAG "%s: failed to open the file : %s", __func__, path);
        return -2;
    }
    LOGD(LOG_TAG "open file OK!,fd:%d\n", fd);
    // open success, file exist, close it now
    close(fd);
    return 0;
}

void fp_tee_file_close(FP_ObjectHandle so_handle)
{
    return ;
}

int fp_tee_file_delete(uint8_t* path)
{
    int ret = 0;

    //Removes a specified file from the HLOS virtual file system
    ret = gpFileIO_Remove_File(NULL, (const char *)path);
    LOGE(LOGTAG "%s: QSEE FTS %s with ret %d", __func__, path, ret);
    return ret;
}

static int fp_fts_read(char* path, uint8_t *buf, uint32_t size)
{
    uint32_t ret = 0;
    int r_cnt = 0;
    fp_fts_obj_t *obj = NULL;
    int obj_size = sizeof(fp_fts_obj_t);

#ifdef FP_QSEE_FTS_PERF
  unsigned long long t0 = 0;
  unsigned long long t1 = 0;
  t0 = qsee_get_uptime();
#endif

    if (!path || !buf) {
        LOGE(LOGTAG "%s: wrong!!! path/buf is NULL", __func__);
        return -3;
    }

    if (size < sizeof(fp_fts_obj_t)) {
        LOGE(LOGTAG "%s: wrong size %u, must larger than %d", __func__, size, obj_size);
        return -3;
    }

    obj = (fp_fts_obj_t*)qsee_malloc(size);
    if (NULL == obj) {
        LOGE(LOGTAG "%s: Failed to allocate memory for obj", __func__);
        return -2;
    }

    ret = gpFileIO_Read_File(NULL ,path ,obj ,size ,0 ,(uint32_t*) &r_cnt);
    if (ret !=0) {
        LOGE(LOGTAG "%s: gpFileIO_Read_File failed for %s with error %d", __func__, path, ret);
        qsee_free(obj);
        return -1;
    }
    LOGD(LOGTAG "%s: obj->data_len: %d",__func__,obj->data_len);

    if (size < sizeof(fp_fts_obj_t) + obj->data_len) {
        LOGE(LOGTAG "%s: Bad file, size %u, data_len %u", __func__, size, obj->data_len);
        qsee_free(obj);
        return -1;
    }
    /* 
     * Verifies the specified data by comparing the hash and/or HMAC provided in the 
     * specified containers with the ones generated from the data. 
     */
    ret = gpCrypto_Integrity_Verify(NULL,obj->iv, obj->data_len + sizeof(obj->iv), NULL,
                                        0, obj->hmac, sizeof(obj->hmac));
    if (ret !=0) {
        LOGE(LOGTAG "%s: gpCrypto_Integrity_Verify failed with error %d",__func__,  ret);
        qsee_free(obj);
        return -1;
    }
    /*
     * Decrypts the specified data via the IV provided by the
     * caller and a unique key generated internally for the application.
     * The decryption operation is done in place within the provided user-allocated buffer. 
     * */
    ret  = gpCrypto_Decrypt(NULL, obj->data, obj->data_len, obj->iv, sizeof(obj->iv));
    if ( ret !=0) {
        LOGE(LOGTAG "%s: gpCrypto_Decrypt failed with error %d",__func__,  ret);
        qsee_free(obj);
        return -1;
    }

    memcpy((char *)buf, obj->data, obj->data_len);
    ret = obj->data_len;
    LOGD(LOGTAG "%s :  Read file %d Bytes , Read  data: %d",__func__, r_cnt, obj->data_len);
    qsee_free(obj);
#ifdef FP_QSEE_FTS_PERF
  t1 = qsee_get_uptime();
  uint32_t duration = (uint32_t) (t1 - t0);
  LOGD("%s Operation took %d ms for %d bytes", __func__, duration, r_cnt);
#endif
    LOGD(LOGTAG "%s: Read %d bytes", __func__, r_cnt);
    return r_cnt;
}

int fp_tee_file_read(FP_ObjectHandle so_handle, uint8_t *buffer, uint32_t size, uint8_t *path)
{
    int ret = 0;
    
    ret = fp_fts_read((char *)path, buffer, size);
    // read ok
    if (ret == size)
        return 0;
    // read error
    if (ret < 0) {
        LOGD(LOGTAG "%s read failed %d, check!!!\n", __func__, ret);
        return -1;
    }
    // read bytes error
    if (ret < size) {
        LOGD(LOGTAG "%s read byte %d is not correct, check!!!\n", __func__, ret);
        return -2;
    }
    return -1;
}

static int fp_fts_write(char* path, uint8_t *buf, uint32_t size)
{
    int w_cnt = 0;
    uint32_t ret;
    fp_fts_obj_t *obj = NULL;

#ifdef FP_QSEE_FTS_PERF
  unsigned long long t0 = 0;
  unsigned long long t1 = 0;
  t0 = qsee_get_uptime();
#endif
    if (NULL == buf) {
        LOGE(LOGTAG "%s: Write buffer is NULL", __func__);
        return -3;
    }

    obj = (fp_fts_obj_t*)qsee_malloc(sizeof(fp_fts_obj_t)+size);
    if (NULL == obj) {
        LOGE(LOGTAG "%s: Failed to allocate memory for obj", __func__);
        return -2;
    }
    obj->data_len = size;
    memcpy(obj->data, buf, size);

    /*
     * Encrypts the specific data via the IV provided by the
     * caller and a unique key generated internally for the application. 
     * The encryption operation is done in place within the provided user-allocated buffer.
     * */
    LOGD(LOGTAG "%s: gpCrypto_Encrypt for %s  obj->data_len: %d", __func__, path, obj->data_len);
    ret = gpCrypto_Encrypt(NULL, obj->data, obj->data_len, obj->iv, sizeof(obj->iv));
    if ( ret !=0) {
        LOGE(LOGTAG "%s: gpCrypto_Encrypt failed with error %d",__func__, ret);
        qsee_free(obj);
        return -1;
    }
    /*
     * Protects the specified data by generating a hash
     * and/or HMAC into the specified containers. */
    LOGD(LOGTAG "gpCrypto_Integrity_Protect for %s obj->data_len+sizeof(obj->iv): %d",
                                                    path,obj->data_len+sizeof(obj->iv));
    ret = gpCrypto_Integrity_Protect(NULL, obj->iv, obj->data_len+sizeof(obj->iv),
                                    NULL,0, obj->hmac, sizeof(obj->hmac));
    if (ret !=0) {
        LOGE(LOGTAG "%s: gpCrypto_Integrity_Protect failed with error %d",	  __func__, ret);
        qsee_free(obj);
        return -1;
    }
    LOGD(LOGTAG "%s: gpFileIO_Write_File for %s size: %d", __func__, path, sizeof(*obj)+size);
    ret = gpFileIO_Write_File(NULL, path, obj, sizeof(*obj) + size, 0, (uint32_t *)&w_cnt);
    if ( ret !=0) {
        LOGE(LOGTAG "%s: gpFileIO_Write_File failed for %s with error %d",__func__, path,ret);
        qsee_free(obj);
        return -1;
    }
    else if((sizeof(*obj)+size) != w_cnt) {
        LOGE(LOGTAG "%s: requested size :%d for writing %s does not match written bytes %d ",
                                     __func__,sizeof(*obj)+size, path, w_cnt);
        qsee_free(obj);
        return -1;
    }
    LOGD(LOGTAG "%s FTS  ret: %d w_cnt: %d bytes",__func__, ret, w_cnt);
    qsee_free(obj);
#ifdef FP_QSEE_FTS_PERF
  t1 = qsee_get_uptime();
  uint32_t t = (uint32_t) (t1 - t0);
  LOGD("%s took %d ms for %d bytes", __func__, t, w_cnt);
#endif
    LOGD(LOGTAG "%s: Wrote %d bytes", __func__, w_cnt);
    return w_cnt;
}

int fp_tee_file_write(uint8_t *path, FP_ObjectHandle *so_handle, uint8_t *buffer, uint32_t size)
{
    int ret = 0;
    int real_w_bytes = size + sizeof(fp_fts_obj_t);
    
    ret = fp_fts_write((char *)path, buffer, size);
    // write ok
    if (ret == real_w_bytes) {
        LOGD(LOGTAG "%s write %d bytes data OK!", __func__, size);
        return 0;
    }
    // write error
    if (ret < 0) {
        LOGD(LOGTAG "%s write failed %d, check!!!\n", __func__, ret);
        return -1;
    }
    // write bytes error
    if (ret < real_w_bytes) {
        LOGD(LOGTAG "%s write byte %d is not correct, check!!!\n", __func__, ret);
        return -2;
    }
    LOGD(LOGTAG "%s write %d bytes data FAILED, ret %d, check!!!", __func__, size, ret);
    return -1;
}

int fp_tee_get_file_size(FP_ObjectHandle so_handle, uint32_t *size, uint8_t *path)
{
    int ret = 0;
    int fd = -1;

    fd = open((const char *)path, O_RDONLY);
    if (-1 == fd) {
        LOGE(LOGTAG "%s: failed to open the file : %s",__func__,path);
        return -2;
    }
    ret = lseek(fd, 0, SEEK_END);
    if(ret == -1) {
        LOGE(LOGTAG "%s:failed to get the file %s size",__func__,path);
        close(fd);
        return -1;
    }
    *size = ret;
    ret = close(fd);
    if(ret == -1) {
        LOGE(LOGTAG "%s:failed to close the file %s",__func__,path);
        return -1;
    }
    LOGD(LOGTAG "%s: file:%s size:%d",__func__ ,path, *size);
    return 0;
}

int fp_tee_file_rename(FP_ObjectHandle so_handle, uint8_t *path_old, uint8_t *path_new)
{
    int status = 0;

    status = frename((char *)path_old, (char *)path_new);
    LOGD(LOGTAG "%s: %s ---> %s, status:%d", __func__, path_old, path_new, status);

    return status;
}
