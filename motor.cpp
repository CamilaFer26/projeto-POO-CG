#include <jni.h>
#include <iostream>

extern "C" {

JNIEXPORT void JNICALL
Java_main_MotorGrafico_init(
    JNIEnv* env,
    jobject obj
){
    std::cout << "Motor iniciado" << std::endl;
}

JNIEXPORT void JNICALL
Java_main_MotorGrafico_update(
    JNIEnv* env,
    jobject obj,
    jfloat tx,
    jfloat ty,
    jfloat angle
){
    std::cout
        << "tx=" << tx
        << " ty=" << ty
        << " angle=" << angle
        << std::endl;
}

JNIEXPORT void JNICALL
Java_main_MotorGrafico_cleanup(
    JNIEnv* env,
    jobject obj
){
    std::cout << "Cleanup" << std::endl;
}

}
