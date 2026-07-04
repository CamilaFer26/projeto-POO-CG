#include <jni.h>
#include <iostream>
#include "graphics_MotorGrafico.h"

extern "C" {

JNIEXPORT void JNICALL
Java_graphics_MotorGrafico_init(
    JNIEnv* env,
    jobject obj,
    jobject canvas)
{
    std::cout << "Motor iniciado" << std::endl;
}

JNIEXPORT void JNICALL
Java_graphics_MotorGrafico_update(
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
Java_graphics_MotorGrafico_cleanup(
    JNIEnv* env,
    jobject obj
){
    std::cout << "Cleanup" << std::endl;
}

}
