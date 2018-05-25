#ifndef __FP_INTERNAL_API_H__
#define __FP_INTERNAL_API_H__

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "fp_log.h"
#include "fp_qsee_fs.h"

typedef unsigned short u_int16_t;
typedef unsigned int   pthread_t;
typedef unsigned char  u_int8_t;

// those APIs for algorithm LIBs, defined in fp_qsee_malloc.c
extern void *malloc(size_t size);
extern void free(void *ptr);
extern void *realloc(void* ptr, size_t size);
extern void *zalloc(size_t size);
extern void *calloc(size_t num, size_t size);

// base APIs for TA LIB, defined in fp_qsee_base.c
extern void *fp_malloc(uint32_t size);
extern void fp_free(void *buffer);
extern void printMallocCnt(void);
extern int  getMallocCnt(void);
extern int  getPbMallocCnt(void);
extern void fp_get_timestamp (uint64_t *pTimestamp);
extern void fp_delay_us (uint32_t duration_us);
extern void fp_delay_ms (uint32_t duration_ms);
extern uint64_t tatimerStart(const char *pInfo);
extern int tatimerEnd(uint64_t startTime, const char *pInfo);
extern uint64_t fp_get_uptime(void);
extern int32_t fp_secure_random(uint8_t* data, uint32_t length);

// SPI related APIs, defined in fp_qsee_spi.c
extern int fpsensor_spi_init(int freq_low_khz, int freq_high_khz);
extern int fpsensor_spi_writeread_fifo(char *tx, char *rx, int tx_len, int send_len);
extern int fpsensor_spi_writeread_dma(char *tx, char *rx, int send_len);
int fpsensor_spi_clk_disable(void);

// Secure Pay related APIs, defined in fp_qsee_sec_pay.c
extern int32_t fp_sec_pay_init(void);
extern uint32_t fp_sec_get_pass_id(void);
extern int32_t fp_sec_set_pass_id(uint32_t tpl_id, uint64_t timestamp);

// HW AUTH related APIs, defined in fp_qsee_hw_auth.c
extern uint32_t qsee_auth_token_key_size;
extern uint8_t qsee_auth_token_key[160 * 160];
extern int fp_hmac_sha256(const uint8_t* data, uint32_t size_data,
                            const uint8_t* key, uint32_t size_key, uint8_t* hmac);
extern int fp_ta_hw_auth_unwrap_key(uint8_t *encrypted_key,
                             uint32_t size_encrypted_key,
                             uint8_t *key, uint32_t *size_key);

#endif /* __FP_INTERNAL_API_H__ */
