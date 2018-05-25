#ifndef FP_THREAD_TASK_H
#define FP_THREAD_TASK_H

#include <inttypes.h>
#include <jni.h>
#include <pthread.h>

typedef struct
{
    void (*function_ptr)(void *);
    void *para;
} worker_thread_t;

class thread_task
{
  public:
    
    thread_task(JavaVM *jvm);
    virtual ~thread_task();
    void pause_thread(void);
    bool is_thread_idle(void);
    void set_thread_task(void (*function_ptr)(void *), void *arg);
    void resume_thread(void);
    void set_thread_default_task(void (*function_ptr)(void *), void *arg);
    void *get_jni_env(void)
    {
        return jni_env_ptr;
    }

    worker_thread_t def_thread_work;
    JavaVM *jvm_ptr;
    void *jni_env_ptr;
    worker_thread_t thread_work;
    int32_t requested;
    pthread_mutex_t mutex;
    pthread_t the_thread;
    bool is_idle;
    bool resumed;
    pthread_cond_t cond;
};

enum
{
    WORKER_THREAD_STATUS_STOP = 0,
    WORKER_THREAD_STATUS_PAUSE,
    WORKER_THREAD_STATUS_RESUME,
    WORKER_THREAD_STATUS_MAX,
};

#endif
