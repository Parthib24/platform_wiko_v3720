#include "jni_util.h"
#include <stdio.h>
#include "fp_thread_task.h"
#include "fp_log.h"
#include "fp_common.h"
#define FPTAG "fpworker.cpp  "

static void *task_function(void *arg)
{
    bool resumed = false;
    worker_thread_t task;
    LOGD("task_function running . . .");

    thread_task *the_task = (thread_task *) arg;
    if (the_task->jvm_ptr)
    {
        if (the_task->jvm_ptr->AttachCurrentThread((JNIEnv **)&the_task->jni_env_ptr, NULL) == JNI_OK)
        {
            LOGD(FPTAG"worker thread attach jvm succeed");
        }
        else
        {
            LOGE(FPTAG"worker thread attach jvm failed");
        }
    }
    else
    {
        LOGD(FPTAG"jvm_ptr is null");
    }


    while (1)
    {
        pthread_mutex_lock(&the_task->mutex);
        the_task->is_idle = true;
        pthread_cond_signal(&the_task->cond);
        while (1)
        {
            if (the_task->requested == WORKER_THREAD_STATUS_STOP)
            {
                pthread_mutex_unlock(&the_task->mutex);
                goto out;
            }
            else if (the_task->requested == WORKER_THREAD_STATUS_RESUME)
            {
                break;
            }
            pthread_cond_wait(&the_task->cond, &the_task->mutex);
        }
        
        the_task->resumed = true;
        the_task->is_idle = false;

        pthread_cond_signal(&the_task->cond);

        memset(&task,0,sizeof(task));

        if (the_task->thread_work.function_ptr)
        {
            task = the_task->thread_work;
            the_task->thread_work.function_ptr = NULL;
        }
        else
        {
            ;//do nothing
        }

        pthread_mutex_unlock(&the_task->mutex);

        if (task.function_ptr) task.function_ptr(task.para);

        pthread_mutex_lock(&the_task->mutex);

        task = the_task->def_thread_work;
        resumed = (the_task->requested == WORKER_THREAD_STATUS_RESUME);

        the_task->requested = WORKER_THREAD_STATUS_PAUSE;
        pthread_mutex_unlock(&the_task->mutex);

        if (resumed && task.function_ptr)
        {
            LOGD(FPTAG"navigator task start");
            task.function_ptr(task.para);
        }
    }

out:
    if (the_task->jvm_ptr)
    {
        the_task->jvm_ptr->DetachCurrentThread();
    }
    LOGD(FPTAG"%s worker exit\n", __func__);
    return 0;
}


thread_task::thread_task(JavaVM *vm)
{
    int32_t ret = 0;
    pthread_cond_init(&cond, NULL);
    pthread_mutex_init(&mutex, NULL);
    requested = WORKER_THREAD_STATUS_PAUSE;
    is_idle = true;
    jvm_ptr = vm;
    
    ret = pthread_create(&the_thread, NULL, task_function, this);

    if (ret) LOGE(FPTAG"%s pthread_create failed %i\n", __func__, ret);        
}
thread_task::~thread_task()
{
    LOGD(FPTAG"~thread_task invoked\n");
    pthread_mutex_lock(&mutex);
    requested = WORKER_THREAD_STATUS_STOP;
    pthread_mutex_unlock(&mutex);
    
    pthread_cond_signal(&cond);

    if(the_thread != 0)
        pthread_join(the_thread, NULL);
    
    pthread_cond_destroy(&cond);
    pthread_mutex_destroy(&mutex);
    return;
}

bool thread_task::is_thread_idle(void)
{
    return is_idle;
}

void thread_task::resume_thread(void)
{
    pthread_mutex_lock(&mutex);
    requested = WORKER_THREAD_STATUS_RESUME;
    pthread_cond_signal(&cond);

    while (!resumed)
    {
        pthread_cond_wait(&cond, &mutex);
    }
    resumed = false;

    pthread_mutex_unlock(&mutex);
}

void thread_task::pause_thread(void)
{
    pthread_mutex_lock(&mutex);
    requested = WORKER_THREAD_STATUS_PAUSE;
    while (!is_idle)
    {
        pthread_cond_wait(&cond, &mutex);
    }
    pthread_mutex_unlock(&mutex);
}

void thread_task::set_thread_task(void (*func)(void *), void *arg)
{
    LOGD(FPTAG"%s", __func__);
    pthread_mutex_lock(&mutex);
    thread_work.function_ptr = func;
    thread_work.para = arg;
    pthread_mutex_unlock(&mutex);
}
void thread_task::set_thread_default_task(void (*func)(void *), void *arg)
{
    LOGD(FPTAG"%s", __func__);
    pthread_mutex_lock(&mutex);
    def_thread_work.function_ptr = func;
    def_thread_work.para = arg;
    pthread_mutex_unlock(&mutex);
}

