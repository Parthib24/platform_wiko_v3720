#ifndef FP_JNIUTIL_H
#define FP_JNIUTIL_H

#include <jni.h>
#include <android/bitmap.h>

namespace fp
{
    class JniUtil
    {
      public:
        static int registerMethods(JNIEnv *env, const char *className,
                                   const JNINativeMethod *methodList, int length);

        static void ThrowException(JNIEnv *env, const char *name, const char *format, ...);

        static const char *kException;
        static const char *kIOException;
        static const char *kInterruptedException;
        static const char *kTimeoutException;
        static const char *kOutOfMemoryError;
        static const char *kUnsatisfiedLinkError;
    };

    class ScopedLocalRef
    {
      public:
        ScopedLocalRef(JNIEnv *env, jobject ref) : ref_(ref), env_(env) {}

        ~ScopedLocalRef()
        {
            if (ref_)
            {
                env_->DeleteLocalRef(ref_);
            }
        }

        jobject ref() const
        {
            return ref_;
        }
      private:
        jobject ref_;
        JNIEnv *env_;
    };

    class JavaString
    {
      public:
        JavaString(JNIEnv *env, jstring string);

        ~JavaString();

        const char *c_str();

      private:
        JNIEnv *env_;
        jstring string_;
        const char *utf_string_;
    };
}
#endif
