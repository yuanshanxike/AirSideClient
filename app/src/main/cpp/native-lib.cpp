#include <jni.h>
#include <string>
#include "include/x264/x264.h"
#include "include/rtmp/rtmp_sys.h"

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_lewis_liveclient_MainActivity_stringFromJNI(
    JNIEnv *env,
    jobject /* this */) {
  std::string hello = "Hello from C++";



  return env->NewStringUTF(hello.c_str());
}
