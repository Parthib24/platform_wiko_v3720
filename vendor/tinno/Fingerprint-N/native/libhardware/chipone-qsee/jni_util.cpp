/*
 */
#include "jni_util.h"
#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <errno.h>
#include <unistd.h>
#include "fp_log.h"
#include "fp_common.h"

#define FPTAG "jni_util.cpp  "

int32_t swap_bytes(uint8_t *buf, int32_t size)
{
    uint8_t *first;
    uint8_t *last;
    uint8_t temp = 0;
    for (first = buf, last = buf + size - 1;
         first < last; ++first, --last)
    {
        temp = *first;
        *first = *last;
        *last = temp;
    }
    return 0;
}
//1 lzk TBD the rand is not available in app_platform  api_21
static unsigned int g_next_random = 1;

static void saRand(void)
{
    timeval cur_time = ca_timer_start(NULL);
    g_next_random = cur_time.tv_sec * 1000000 + cur_time.tv_usec;
}
static int32_t aRand()
{
    unsigned int next = g_next_random;
    int result;

    next *= 1103515245;
    next += 12345;
    result = (unsigned int) (next / 65536) % 2048;

    next *= 1103515245;
    next += 12345;
    result <<= 10;
    result ^= (unsigned int) (next / 65536) % 1024;

    next *= 1103515245;
    next += 12345;
    result <<= 10;
    result ^= (unsigned int) (next / 65536) % 1024;

    g_next_random = next;

    return result;
}

void fp_random_seed(void)
{
    saRand();
}

int32_t fp_random(uint8_t *buf, int32_t size)
{
    int32_t random_value = 0;
    for (int i = 0; i < size; i++)
    {
        random_value = aRand();
        buf[i] = random_value % 0xff;
    }
    return 0;
}

struct timeval ca_timer_start(const char *pInfo)
{
    struct timeval start;
    if (pInfo)
    {
        LOGI(" timer start %s :", pInfo);
    }

    gettimeofday(&start, NULL);
    return start;
}

int ca_timer_end(const char *fmt, struct timeval start)
{
    struct timeval time_end, time_delta;
    int time_elapsed = 0;
    gettimeofday(&time_end, NULL);
    timersub(&time_end, &start, &time_delta);
    time_elapsed = time_delta.tv_sec * 1000000 + time_delta.tv_usec;

    if (fmt)
    {
        LOGI(" %s the time delta is=%d ms", fmt , time_elapsed / 1000);
    }

    return time_elapsed / 1000;
}

int malloc_cnt = 0;
void *fp_malloc(size_t size)
{
    malloc_cnt++;
    return malloc(size);
}

void fp_free(void *buf)
{
    if (buf != NULL)
    {
        malloc_cnt--;
    }
    free(buf);
}

int get_malloc_cnt(void)
{
    return malloc_cnt;
}


static int fp_load(const char *id, const char *path,  const struct hw_module_t **pHmi)  
{
    int status = 0;  
    void *handle = NULL;  
    struct hw_module_t *hmi = NULL;  
    const char *sym = HAL_MODULE_INFO_SYM_AS_STR;

    handle = dlopen(path, RTLD_NOW);
    if (handle == NULL) {
        char const *err_str = dlerror();
        LOGE("fp_load: module=%s %s", path, err_str?err_str:"unknown");
        status = -EINVAL;
        goto done;
    }

    hmi = (struct hw_module_t *)dlsym(handle, sym);
    if (hmi == NULL) {
        LOGE("fp_load: couldn't find symbol %s", sym);
        status = -EINVAL;
        goto done;
    }

    /* Check that the id matches */
    if (strcmp(id, hmi->id) != 0) {
        LOGE("fp_load: id=%s != hmi->id=%s", id, hmi->id);
        status = -EINVAL;
        goto done;
    }

    hmi->dso = handle;
    /* success */
    status = 0;

done:
    if (status != 0)
    {
        hmi = NULL;
        if (handle != NULL)
        {
            dlclose(handle);
            handle = NULL;
        }
    }
    else
    {
        LOGI("loaded HAL id=%s path=%s hmi=%p handle=%p",id, path, *pHmi, handle);
    }
    *pHmi = hmi;
    return status;
}

int fp_hw_get_module(const char *id, const struct hw_module_t **module)
{
    char path[PATH_MAX];
    const char *sys_lib64_hw_path = "/system/lib64/hw";
    const char *sys_lib_hw_path = "/system/lib/hw";
    const char *final_hal_path = sys_lib_hw_path;

    if(is_64bit_system())
    {
        final_hal_path = sys_lib64_hw_path;
    }

    memset(path,0,sizeof(path));
    snprintf(path, sizeof(path), "%s/%s.default.so", final_hal_path, id);
    return fp_load(id, path, module);
}

namespace fp
{
    const char *JniUtil::kInterruptedException = "java/lang/InterruptedException";
    const char *JniUtil::kIOException = "java/io/IOException";
    const char *JniUtil::kException = "java/lang/Exception";
    const char *JniUtil::kTimeoutException = "java/lang/TimeoutException";
    const char *JniUtil::kOutOfMemoryError = "java/lang/OutOfMemoryError";
    const char *JniUtil::kUnsatisfiedLinkError = "java/lang/UnsatisfiedLinkError";


    int JniUtil::registerMethods(JNIEnv *env, const char *className,
                                 const JNINativeMethod *methodList, int length)
    {
        ScopedLocalRef clazz(env, env->FindClass(className));
        if (!clazz.ref())
        {
            LOGE(FPTAG"Can't find class %s\n", className);
            return -1;
        }

        jint result = env->RegisterNatives((jclass)clazz.ref(), methodList, length);

        if (result != JNI_OK)
        {
            LOGE(FPTAG"RegisterNatives failed (%s)\n", className);
            return -1;
        }

        return 0;
    }

    void  JniUtil::ThrowException(JNIEnv *env, const char *name, const char *format, ...)
    {
        jclass clazz = env->FindClass(name);

        if (!clazz)
        {
            LOGE(FPTAG"couldn't find class %s\n", name);
            return;
        }
        char message[256];
        va_list ap;
        va_start(ap, format);
        vsnprintf(message, sizeof(message), format, ap);
        va_end(ap);
        env->ThrowNew(clazz, message);
    }

    JavaString::JavaString(JNIEnv *env, jstring string)
    {
        env_ = env;
        string_ = string;
        utf_string_ = NULL;
    }

    JavaString::~JavaString()
    {
        if (utf_string_)
        {
            env_->ReleaseStringUTFChars(string_, utf_string_);
        }
    }

    const char *JavaString::c_str()
    {
        if (!utf_string_)
        {
            jboolean is_copy;
            utf_string_ = env_->GetStringUTFChars(string_, &is_copy);
        }

        return utf_string_;
    }
}
