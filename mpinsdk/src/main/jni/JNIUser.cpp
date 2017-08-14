/***************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ***************************************************************/

#include "JNIUser.h"
#include "JNICommon.h"


static void nDestruct(JNIEnv *env, jobject jobj, jlong jptr) {
    delete (MPinSDKBase::UserPtr *) jptr;
}

static jstring nGetId(JNIEnv *env, jobject jobj, jlong jptr) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    return env->NewStringUTF(userPtr->get()->GetId().c_str());
}

static void nGetRegistrationExpiration(JNIEnv *env, jobject jobj, jlong jptr, jobject jexpiration) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    MPinSDKBase::Expiration expiration = userPtr->get()->GetRegistrationExpiration();

    jclass clsExpiration = env->FindClass("com/miracl/mpinsdk/model/Expiration");
    jfieldID fidExpireTimeSeconds = env->GetFieldID(clsExpiration, "expireTimeSeconds", "I");
    jfieldID fidNowTimeSeconds = env->GetFieldID(clsExpiration, "nowTimeSeconds", "I");

    env->SetIntField(jexpiration, fidExpireTimeSeconds, expiration.expireTimeSeconds);
    env->SetIntField(jexpiration, fidNowTimeSeconds, expiration.nowTimeSeconds);
}

static jint nGetState(JNIEnv *env, jobject jobj, jlong jptr) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    return userPtr->get()->GetState();
}

static jstring nGetBackend(JNIEnv *env, jobject jobj, jlong jptr) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    return env->NewStringUTF(userPtr->get()->GetBackend().c_str());
}

static jstring nGetCustomerId(JNIEnv *env, jobject jobj, jlong jptr) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    return env->NewStringUTF(userPtr->get()->GetCustomerId().c_str());
}

static jstring nGetAppId(JNIEnv *env, jobject jobj, jlong jptr) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    return env->NewStringUTF(userPtr->get()->GetAppId().c_str());
}

static jboolean nCanSign(JNIEnv *env, jobject jobj, jlong jptr) {
    const MPinSDKBase::UserPtr *userPtr = (const MPinSDKBase::UserPtr *) jptr;
    return (jboolean) userPtr->get()->CanSign();
}

static JNINativeMethod g_methodsUser[] =
        {
                NATIVE_METHOD(nDestruct, "(J)V"),
                NATIVE_METHOD(nGetId, "(J)Ljava/lang/String;"),
                NATIVE_METHOD(nGetRegistrationExpiration,
                              "(JLcom/miracl/mpinsdk/model/Expiration;)V"),
                NATIVE_METHOD(nGetState, "(J)I"),
                NATIVE_METHOD(nGetBackend, "(J)Ljava/lang/String;"),
                NATIVE_METHOD(nGetCustomerId, "(J)Ljava/lang/String;"),
                NATIVE_METHOD(nGetAppId, "(J)Ljava/lang/String;"),
                NATIVE_METHOD(nCanSign, "(J)Z")
        };

void RegisterUserJNI(JNIEnv *env) {
    RegisterNativeMethods(env, "com/miracl/mpinsdk/model/User", g_methodsUser,
                          ARR_LEN(g_methodsUser));
}
