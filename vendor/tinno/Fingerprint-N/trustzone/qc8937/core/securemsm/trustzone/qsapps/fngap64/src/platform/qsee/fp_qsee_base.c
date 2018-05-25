#include <qsee_log.h>
#include <qsee_heap.h>
#include <qsee_timer.h>
#include "qsee_prng.h"
#include "qsee_hmac.h"

#include "fp_log.h"

#define LOGTAG

int iMallocCnt = 0;
int iPbMallocCnt = 0;

void* fp_malloc(uint32_t size)
{
	void *mem = NULL;
    if(size > 0)
        iMallocCnt++;
	
    LOGD(LOGTAG"Malloc: %dbytes ", size);
	mem = qsee_malloc(size);
    if (!mem)
		LOGD(LOGTAG"Malloc: failed");

	return mem;
}

void fp_free(void *buffer)
{
    if(buffer != NULL){
        iMallocCnt--;
    }
	qsee_free(buffer);
}

void printMallocCnt(void)
{
    LOGD(LOGTAG"MallocCnt: %d", iMallocCnt);
}

int getMallocCnt(void)
{
    return iMallocCnt;
}

int getPbMallocCnt(void)
{
    return iPbMallocCnt;
}

void fp_delay_us(uint32_t duration_us)
{
    uint64_t delay_ms = 0;
    uint64_t begin, end;

    if (duration_us / 1000 == 0)
        delay_ms = 1;
    else
        delay_ms = duration_us / 1000;

    begin = qsee_get_uptime();
    end = begin + delay_ms;

    while (end > qsee_get_uptime())
        ;

    return ;
}

void fp_delay_ms(uint32_t duration_ms)
{
    uint64_t begin, end;

    begin = qsee_get_uptime();
    end = begin + duration_ms;

    while (end > qsee_get_uptime())
        ;

    return ;
}

void fp_get_timestamp(uint64_t *pTimestamp)
{
    *pTimestamp = qsee_get_uptime() * 1000; //ms to us
}

uint64_t fp_get_uptime(void)
{
    return qsee_get_uptime();
}

int32_t fp_secure_random(uint8_t* data, uint32_t length)
{
    if (qsee_prng_getdata(data, length) != length) {
        LOGE("qsee_prng_getdata() failed");
        return -1;
    }

    return 0;
}
