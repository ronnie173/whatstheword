#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#include <android_native_app_glue.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "heyzap-bridge", __VA_ARGS__))

extern "C"{
    jclass getHeyzapLib(JNIEnv * jni_env) {
        jobject classLoaderInstance = jni->CallObjectMethod(
            state->activity->clazz, 
            jni->GetMethodID(
                jni->FindClass("android/app/NativeActivity"),
                "getClassLoader", 
                "()Ljava/lang/ClassLoader;"
            )
        );
        jmethodID loadClassMethod = jni->GetMethodID(
            jni->FindClass("java/lang/ClassLoader"), 
            "loadClass", 
            "(Ljava/lang/String;)Ljava/lang/Class;"
        );
    
        return (jclass) jni->CallObjectMethod(
            classLoaderInstance, 
            loadClassMethod, 
            jni->NewStringUTF("com.heyzap.sdk.HeyzapLib")
        );
    }

    void heyzapLoadOld(struct android_app* state, int showToast) {
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);
        jobject classLoaderInstance = jni->CallObjectMethod(state->activity->clazz, jni->GetMethodID(jni->FindClass("android/app/NativeActivity"),"getClassLoader", "()Ljava/lang/ClassLoader;"));
        jmethodID loadClassMethod = jni->GetMethodID(jni->FindClass("java/lang/ClassLoader"), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    
        jclass heyzapLib = (jclass)jni->CallObjectMethod(classLoaderInstance, loadClassMethod, jni->NewStringUTF("com.heyzap.sdk.HeyzapLib"));
        jmethodID methodId = jni->GetStaticMethodID(heyzapLib, "load", "(Landroid/content/Context;Z)V");
    
        jni->CallStaticVoidMethod(heyzapLib, methodId, state->activity->clazz, (jboolean)showToast);
    }


    void heyzapLoad(struct android_app* state, int showToast) {
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);

        jclass cls_JavaClass = getHeyzapLib(jni);
        jmethodID methodId = jni->GetStaticMethodID(
            cls_JavaClass,
            "load", 
            "(Landroid/content/Context;Z)V"
        );
        
        jni->CallStaticVoidMethod(
            cls_JavaClass, methodId, 
            state->activity->clazz, (jboolean) showToast
        );
    }

    // Show the checkin form (with optional pre-filled message)
    void heyzapCheckin(struct android_app* state, const char *s) {
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);
        jobject classLoaderInstance = jni->CallObjectMethod(state->activity->clazz, jni->GetMethodID(jni->FindClass("android/app/NativeActivity"),"getClassLoader", "()Ljava/lang/ClassLoader;"));
        jmethodID loadClassMethod = jni->GetMethodID(jni->FindClass("java/lang/ClassLoader"), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    
        jclass heyzapLib = (jclass)jni->CallObjectMethod(classLoaderInstance, loadClassMethod, jni->NewStringUTF("com.heyzap.sdk.HeyzapLib"));
        jmethodID methodId = jni->GetStaticMethodID(heyzapLib, "checkin", "(Landroid/content/Context;Ljava/lang/String;)V");

        jni->CallStaticVoidMethod(heyzapLib, methodId, state->activity->clazz, jni->NewStringUTF(s));
    }

    // Check if we are running a supported version of android
    int heyzapIsSupported(struct android_app* state) {
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);
        jobject classLoaderInstance = jni->CallObjectMethod(state->activity->clazz, jni->GetMethodID(jni->FindClass("android/app/NativeActivity"),"getClassLoader", "()Ljava/lang/ClassLoader;"));
        jmethodID loadClassMethod = jni->GetMethodID(jni->FindClass("java/lang/ClassLoader"), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    
        jclass heyzapLib = (jclass)jni->CallObjectMethod(classLoaderInstance, loadClassMethod, jni->NewStringUTF("com.heyzap.sdk.HeyzapLib"));
        jmethodID methodId = jni->GetStaticMethodID(heyzapLib, "isSupported", "(Landroid/content/Context;)Z");
    
        jni->CallStaticBooleanMethod(heyzapLib, methodId, state->activity->clazz);
    }

    // Display a popup containing an ad
    // int heyzapShowAd(struct android_app* state) {
    //     JNIEnv *jni;
    //     state->activity->vm->AttachCurrentThread(&jni, 0);

    //     jclass cls_JavaClass = getHeyzapLib(jni);
    //     jmethodID methodId = jni->GetStaticMethodID(
    //         cls_JavaClass,
    //         "showAd", 
    //         "(Landroid/content/Context;)V"
    //     );
        
    //     jni->CallStaticVoidMethod(cls_JavaClass, methodId, state->activity->clazz);
    // }
    
    // void heyzapEnableAds(struct android_app* state) {
    //     JNIEnv *jni;
    //     state->activity->vm->AttachCurrentThread(&jni, 0);

    //     jclass cls_JavaClass = getHeyzapLib(jni);
    //     jmethodID methodId = jni->GetStaticMethodID(
    //         cls_JavaClass,
    //         "enableAds", 
    //         "(Landroid/content/Context;)V"
    //     );
        
    //     jni->CallStaticVoidMethod(cls_JavaClass, methodId, state->activity->clazz);
    // }

    void heyzapSeeAchievements(struct android_app* state, int includeLocked){
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);

        jclass cls_JavaClass = getHeyzapLib(jni);
        jmethodID methodId = jni->GetStaticMethodID(
            cls_JavaClass,
            "seeAchievements", 
            "(Landroid/content/Context;Z)V"
        );
        
        jni->CallStaticVoidMethod(
            cls_JavaClass, methodId, 
            state->activity->clazz,
            (jboolean) includeLocked
        );
    }

    void heyzapUnlockAchievement(struct android_app* state, const char *achievementIds){
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);
    

        jclass cls_JavaClass = getHeyzapLib(jni);
        jmethodID methodId = jni->GetStaticMethodID(
            cls_JavaClass,
            "unlockAchievement", 
            "(Landroid/content/Context;Ljava/lang/String;)V"
        );

        jni->CallStaticVoidMethod(
            cls_JavaClass, methodId,
            state->activity->clazz,
            jni->NewStringUTF(achievementIds)
        );
    }

    void heyzapShowLeaderboards(struct android_app* state, const char *levelId) {
        JNIEnv *jni;
        state->activity->vm->AttachCurrentThread(&jni, 0);

        jclass cls_JavaClass = getHeyzapLib(jni);
        jmethodID methodId = jni->GetStaticMethodID(
            cls_JavaClass,
            "showLeaderboards", 
            "(Landroid/content/Context;Ljava/lang/String;)V"
        );

        jni->CallStaticVoidMethod(
            cls_JavaClass, methodId,
            state->activity->clazz,
            jni->NewStringUTF(levelId)
        );
    }
}
