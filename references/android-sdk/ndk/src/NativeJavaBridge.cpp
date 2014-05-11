#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

// __android_log_print(ANDROID_LOG_INFO, "JavaBridge", "[%s] JNI Environment is = %08x\n", __FUNCTION__, jni_env);

extern "C" {
    JavaVM* java_vm;

    // Executed when JNI is loaded
    jint JNI_OnLoad(JavaVM* vm, void* reserved) {
        java_vm = vm;

        // minimum JNI version
        return JNI_VERSION_1_6;
    }

    // Load and attach our thread to the jvm
    JNIEnv *getEnv() {
        JNIEnv *jni_env = 0;
        java_vm->AttachCurrentThread(&jni_env, 0);

        return jni_env;
    }

    // Load the heyzap tools (actually shows notification and toast)
    void load(int showHeyzapInstallSplash) {
        JNIEnv *jni_env = getEnv();
        
        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "load", "(Z)V");
        
        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId, (jboolean) showHeyzapInstallSplash);
    }


    void enableAds() {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/ads/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "start", "()V");

        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId);
    }

    // Show the checkin form (with optional pre-filled message)
    void checkin(const char *s) {
        JNIEnv *jni_env = getEnv();
        
        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "checkin", "(Ljava/lang/String;)V");
        
        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId, jni_env->NewStringUTF(s));
    }

    // Show the checkin form (with optional pre-filled message)
    void showLeaderboards(const char *levelId) {
        JNIEnv *jni_env = getEnv();
        
        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId = jni_env->GetStaticMethodID(cls_JavaClass, "showLeaderboards", "(Ljava/lang/String;)V");
    
        jni_env->CallStaticVoidMethod(
            cls_JavaClass,
            methodId,
            jni_env->NewStringUTF(levelId)
        );
    }

    void unlockAchievement(const char *achievementIds){
        JNIEnv *jni_env = getEnv();
        
        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId = jni_env->GetStaticMethodID(cls_JavaClass, "unlockAchievement", "(Ljava/lang/String;)V");
    
        jni_env->CallStaticVoidMethod(
            cls_JavaClass,
            methodId,
            jni_env->NewStringUTF(achievementIds)
        );
    }

    void showAchievements(){
        JNIEnv *jni_env = getEnv();
        
        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "showAchievements", "()V");
        
        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId);
    }

    // Show the checkin form (with optional pre-filled message)
    void submitScore(const char *score, const char *displayScore, const char *levelId) {
        JNIEnv *jni_env = getEnv();
        
        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId = jni_env->GetStaticMethodID(cls_JavaClass, "submitScore", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    
        jni_env->CallStaticVoidMethod(
            cls_JavaClass,
            methodId,
            jni_env->NewStringUTF(score),
            jni_env->NewStringUTF(displayScore),
            jni_env->NewStringUTF(levelId)
        );
    }

    void setFlags(int flags) {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "setFlags", "(I)V");

        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId, flags);
    }

    // Check if we are running a supported version of android
    bool isSupported() {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "isSupported", "()Z");

        return jni_env->CallStaticBooleanMethod(cls_JavaClass, methodId);
    }

    void showBannerAdForTag(const char *position, const char *tag) {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/ads/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "showBanner", "(Ljava/lang/String;Ljava/lang/String;)V");

        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId, jni_env->NewStringUTF(position), jni_env->NewStringUTF(tag));
    }

    // Display a popup containing an ad
    void showBannerAd(const char *position) {
        showBannerAdForTag(position, "default");
    }

    void hideBannerAd() {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/ads/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "hideBanner", "()V");

        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId);
    }

    void showInterstitialAdForTag(const char *tag) {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/ads/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "showInterstitial", "(Ljava/lang/String;)V");

        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId, jni_env->NewStringUTF(tag));
    }

    void showInterstitialAd() {
        showInterstitialAdForTag("default");
    }

    void hideInterstitialAd() {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/ads/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "hideInterstitial", "()V");

        jni_env->CallStaticVoidMethod(cls_JavaClass, methodId);
    }

    bool isInterstitialAdAvailable() {
        JNIEnv *jni_env = getEnv();

        jclass cls_JavaClass = jni_env->FindClass("com/heyzap/sdk/ads/UnityHelper");
        jmethodID methodId   = jni_env->GetStaticMethodID(cls_JavaClass, "isInterstitialAdAvailable", "()Z");

        return jni_env->CallStaticBooleanMethod(cls_JavaClass, methodId);
    }

    //
    // Deprecated functions
    //

    // Prompt the user to checkin (deprecated)
    void promptCheckin() {
        load(true);
    }
}
