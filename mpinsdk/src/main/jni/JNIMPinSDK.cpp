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

#include "JNIMPinSDK.h"
#include "JNICommon.h"
#include "HTTPConnector.h"
#include "Storage.h"
#include "Context.h"


typedef sdk::Context Context;

static jlong nConstruct(JNIEnv* env, jobject jobj)
{
	return (jlong) new MPinSDK();
}

static void nDestruct(JNIEnv* env, jobject jobj, jlong jptr)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	delete sdk;
}


// MPinSDKBase

static jobject nInit(JNIEnv* env, jobject jobj, jlong jptr, jobject jconfig, jobject jcontext)
{
    MPinSDK::StringMap config;
    if(jconfig)
    {
        ReadJavaMap(env, jconfig, config);
    }

    MPinSDK* sdk = (MPinSDK*) jptr;
    return MakeJavaStatus(env, sdk->Init(config, Context::Instance(jcontext)));
}

static void nAddCustomHeaders(JNIEnv* env, jobject jobj, jlong jptr, jobject jcustomHeaders)
{
    MPinSDK::StringMap customHeaders;
    if(jcustomHeaders)
    {
        ReadJavaMap(env, jcustomHeaders, customHeaders);
    }

    MPinSDK* sdk = (MPinSDK*) jptr;
    sdk->AddCustomHeaders(customHeaders);
}

static void nClearCustomHeaders(JNIEnv* env, jobject jobj, jlong jptr)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    sdk->ClearCustomHeaders();
}

static void nAddTrustedDomain(JNIEnv* env, jobject jobj, jlong jptr, jstring domain)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    sdk->AddTrustedDomain(JavaToStdString(env, domain));
}

static void nClearTrustedDomains(JNIEnv* env, jobject jobj, jlong jptr)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    sdk->ClearTrustedDomains();
}

static jobject nTestBackend(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return MakeJavaStatus(env, sdk->TestBackend(JavaToStdString(env, jserver)));
}

static jobject nTestBackendRPS(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver, jstring jrpsPrefix)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return MakeJavaStatus(env, sdk->TestBackend(JavaToStdString(env, jserver), JavaToStdString(env, jrpsPrefix)));
}

static jboolean nIsUserExisting(JNIEnv* env, jobject jobj, jlong jptr, jstring jid, jstring jcustomerId, jstring jappId)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return sdk->IsUserExisting(JavaToStdString(env,jid), JavaToStdString(env,jcustomerId), JavaToStdString(env,jappId));
}

static jobject nSetBackend(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return MakeJavaStatus(env, sdk->SetBackend(JavaToStdString(env, jserver)));
}

static jobject nSetBackendRPS(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver, jstring jrpsPrefix)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return MakeJavaStatus(env, sdk->SetBackend(JavaToStdString(env, jserver), JavaToStdString(env, jrpsPrefix)));
}

static jobject nMakeNewUser(JNIEnv* env, jobject jobj, jlong jptr, jstring jid, jstring jdeviceName)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    MPinSDKBase::UserPtr user = sdk->MakeNewUser(JavaToStdString(env, jid), JavaToStdString(env, jdeviceName));
    jclass clsUser = env->FindClass("com/miracl/mpinsdk/model/User");
    jmethodID ctorUser = env->GetMethodID(clsUser, "<init>", "(J)V");
    return env->NewObject(clsUser, ctorUser, (jlong) new MPinSDKBase::UserPtr(user));
}

static void nDeleteUser(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    sdk->DeleteUser(JavaToMPinUser(env, juser));
}

static void nClearUsers(JNIEnv* env, jobject jobj, jlong jptr)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    sdk->ClearUsers();
}

static jboolean nCanLogout(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return sdk->CanLogout(JavaToMPinUser(env, juser));
}

static jboolean nLogout(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    return sdk->Logout(JavaToMPinUser(env, juser));
}

static jstring nGetClientParam(JNIEnv* env, jobject jobj, jlong jptr, jstring jkey)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    MPinSDK::String result = sdk->GetClientParam(JavaToStdString(env, jkey));
    return env->NewStringUTF(result.c_str());
}

// MPinSDK

static jobject nStartRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jactivateCode, jstring juserData)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->StartRegistration(JavaToMPinUser(env, juser), JavaToStdString(env, jactivateCode), JavaToStdString(env, juserData)));
}

static jobject nRestartRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring juserData)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->RestartRegistration(JavaToMPinUser(env, juser), JavaToStdString(env, juserData)));
}

static jobject nConfirmRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jpushToken)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->ConfirmRegistration(JavaToMPinUser(env, juser), JavaToStdString(env, jpushToken)));
}

static jobject nFinishRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jpin)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->FinishRegistration(JavaToMPinUser(env, juser), JavaToStdString(env, jpin)));
}

static jobject nStartAuthentication(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->StartAuthentication(JavaToMPinUser(env, juser)));
}

static jobject nCheckAccessNumber(JNIEnv* env, jobject jobj, jlong jptr, jstring jaccessNumber)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->CheckAccessNumber(JavaToStdString(env, jaccessNumber)));
}

static jobject nFinishAuthentication(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jpin)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->FinishAuthentication(JavaToMPinUser(env, juser), JavaToStdString(env, jpin)));
}

static jobject nFinishAuthenticationResultData(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jpin, jobject jresultData)
{
	MPinSDK* sdk = (MPinSDK*) jptr;

	MPinSDK::String authResultData;
	MPinSDK::Status status = sdk->FinishAuthentication(JavaToMPinUser(env, juser), JavaToStdString(env, jpin), authResultData);

	jclass clsStringBuilder = env->FindClass("java/lang/StringBuilder");
	jmethodID midSetLength = env->GetMethodID(clsStringBuilder, "setLength", "(I)V");
	env->CallVoidMethod(jresultData, midSetLength, authResultData.size());
	jmethodID midReplace = env->GetMethodID(clsStringBuilder, "replace", "(IILjava/lang/String;)Ljava/lang/StringBuilder;");
	env->CallObjectMethod(jresultData, midReplace, 0, authResultData.size(), env->NewStringUTF(authResultData.c_str()));

	return MakeJavaStatus(env, status);
}

static jobject nFinishAuthenticationOTP(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jpin, jobject jotp)
{
	MPinSDK* sdk = (MPinSDK*) jptr;

	MPinSDK::OTP otp;
	MPinSDK::Status status = sdk->FinishAuthenticationOTP(JavaToMPinUser(env, juser), JavaToStdString(env, jpin), otp);

	if(status == MPinSDK::Status::OK)
	{
		jclass clsOTP = env->FindClass("com/miracl/mpinsdk/model/OTP");
		jfieldID fidOtp = env->GetFieldID(clsOTP, "otp", "Ljava/lang/String;");
		jfieldID fidExpireTime = env->GetFieldID(clsOTP, "expireTime", "J");
		jfieldID fidTtlSeconds = env->GetFieldID(clsOTP, "ttlSeconds", "I");
		jfieldID fidNowTime = env->GetFieldID(clsOTP, "nowTime", "J");
		jfieldID fidStatus = env->GetFieldID(clsOTP, "status", "Lcom/miracl/mpinsdk/model/Status;");
		env->SetObjectField(jotp, fidOtp, env->NewStringUTF(otp.otp.c_str()));
		env->SetLongField(jotp, fidExpireTime, otp.expireTime);
		env->SetIntField(jotp, fidTtlSeconds, otp.ttlSeconds);
		env->SetLongField(jotp, fidNowTime, otp.nowTime);
		env->SetObjectField(jotp, fidStatus, MakeJavaStatus(env, otp.status));
	}

	return MakeJavaStatus(env, status);
}

static jobject nFinishAuthenticationAN(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jpin, jstring jaccessNumber)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	return MakeJavaStatus(env, sdk->FinishAuthenticationAN(JavaToMPinUser(env, juser), JavaToStdString(env, jpin), JavaToStdString(env, jaccessNumber)));
}

static jobject nListUsers(JNIEnv* env, jobject jobj, jlong jptr, jobject jusersList)
{
	MPinSDK* sdk = (MPinSDK*) jptr;
	std::vector<MPinSDK::UserPtr> users;
	MPinSDK::Status status = sdk->ListUsers(users);

    if(status == MPinSDK::Status::OK)
    {
        jclass clsList = env->FindClass("java/util/List");
        jmethodID midAdd = env->GetMethodID(clsList, "add", "(Ljava/lang/Object;)Z");

        jclass clsUser = env->FindClass("com/miracl/mpinsdk/model/User");
        jmethodID ctorUser = env->GetMethodID(clsUser, "<init>", "(J)V");

        for (std::vector<MPinSDK::UserPtr>::iterator i = users.begin(); i != users.end(); ++i) {
            MPinSDK::UserPtr user = *i;
            jobject juser = env->NewObject(clsUser, ctorUser, (jlong) new MPinSDK::UserPtr(user));
            env->CallBooleanMethod(jusersList, midAdd, juser);
        }
    }

    return MakeJavaStatus(env,status);
}

static jobject nListUsersForBackend(JNIEnv* env, jobject jobj, jlong jptr, jobject jusersList, jstring jbackend)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    	std::vector<MPinSDK::UserPtr> users;
    	MPinSDK::Status status = sdk->ListUsers(users, JavaToStdString(env, jbackend));

        if(status == MPinSDK::Status::OK)
        {
            jclass clsList = env->FindClass("java/util/List");
            jmethodID midAdd = env->GetMethodID(clsList, "add", "(Ljava/lang/Object;)Z");

            jclass clsUser = env->FindClass("com/miracl/mpinsdk/model/User");
            jmethodID ctorUser = env->GetMethodID(clsUser, "<init>", "(J)V");

            for (std::vector<MPinSDK::UserPtr>::iterator i = users.begin(); i != users.end(); ++i) {
                MPinSDK::UserPtr user = *i;
                jobject juser = env->NewObject(clsUser, ctorUser, (jlong) new MPinSDK::UserPtr(user));
                env->CallBooleanMethod(jusersList, midAdd, juser);
            }
        }

        return MakeJavaStatus(env,status);
}

static jobject nListAllUsers(JNIEnv* env, jobject jobj, jlong jptr, jobject jusersList)
{
        MPinSDK* sdk = (MPinSDK*) jptr;
        std::vector<MPinSDK::UserPtr> users;
        MPinSDK::Status status = sdk->ListAllUsers(users);

        if(status == MPinSDK::Status::OK)
        {
            jclass clsList = env->FindClass("java/util/List");
            jmethodID midAdd = env->GetMethodID(clsList, "add", "(Ljava/lang/Object;)Z");

            jclass clsUser = env->FindClass("com/miracl/mpinsdk/model/User");
            jmethodID ctorUser = env->GetMethodID(clsUser, "<init>", "(J)V");

            for (std::vector<MPinSDK::UserPtr>::iterator i = users.begin(); i != users.end(); ++i) {
                MPinSDK::UserPtr user = *i;
                jobject juser = env->NewObject(clsUser, ctorUser, (jlong) new MPinSDK::UserPtr(user));
                env->CallBooleanMethod(jusersList, midAdd, juser);
            }
        }

        return MakeJavaStatus(env,status);
}

static jobject nListBackends(JNIEnv* env, jobject jobj, jlong jptr,jobject jBackendsList)
{
    MPinSDK* sdk = (MPinSDK*) jptr;
    std::vector<MPinSDK::String> backends;
    MPinSDK::Status status = sdk->ListBackends(backends);

    if(status == MPinSDK::Status::OK)
    {
        jclass clsList = env->FindClass("java/util/List");
        jmethodID midAdd = env->GetMethodID(clsList, "add", "(Ljava/lang/Object;)Z");

        for (std::vector<MPinSDK::String>::iterator i = backends.begin(); i != backends.end(); ++i) {
            MPinSDK::String backend = *i;
            env->CallBooleanMethod(jBackendsList, midAdd, env->NewStringUTF(backend.c_str()));
        }
    }

    return MakeJavaStatus(env,status);
}

static JNINativeMethod g_methodsMPinSDK[] =
{
	NATIVE_METHOD(nConstruct, "()J"),
    NATIVE_METHOD(nDestruct, "(J)V"),
    NATIVE_METHOD(nInit, "(JLjava/util/Map;Landroid/content/Context;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nAddCustomHeaders, "(JLjava/util/Map;)V"),
    NATIVE_METHOD(nClearCustomHeaders, "(J)V"),
    NATIVE_METHOD(nAddTrustedDomain, "(JLjava/lang/String;)V"),
    NATIVE_METHOD(nClearTrustedDomains, "(J)V"),
    NATIVE_METHOD(nTestBackend, "(JLjava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nTestBackendRPS, "(JLjava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nIsUserExisting, "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z"),
    NATIVE_METHOD(nSetBackend, "(JLjava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nSetBackendRPS, "(JLjava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nMakeNewUser, "(JLjava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/User;"),
    NATIVE_METHOD(nDeleteUser, "(JLcom/miracl/mpinsdk/model/User;)V"),
    NATIVE_METHOD(nClearUsers, "(J)V"),
    NATIVE_METHOD(nCanLogout, "(JLcom/miracl/mpinsdk/model/User;)Z"),
    NATIVE_METHOD(nLogout, "(JLcom/miracl/mpinsdk/model/User;)Z"),
    NATIVE_METHOD(nGetClientParam, "(JLjava/lang/String;)Ljava/lang/String;"),
	NATIVE_METHOD(nStartRegistration, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nRestartRegistration, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nConfirmRegistration, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishRegistration, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nStartAuthentication, "(JLcom/miracl/mpinsdk/model/User;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nCheckAccessNumber, "(JLjava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishAuthentication, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishAuthenticationResultData, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Ljava/lang/StringBuilder;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishAuthenticationOTP, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Lcom/miracl/mpinsdk/model/OTP;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishAuthenticationAN, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nListUsers, "(JLjava/util/List;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nListAllUsers, "(JLjava/util/List;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nListUsersForBackend, "(JLjava/util/List;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nListBackends, "(JLjava/util/List;)Lcom/miracl/mpinsdk/model/Status;")
};

void RegisterMPinSDKJNI(JNIEnv* env)
{
	RegisterNativeMethods(env, "com/miracl/mpinsdk/MPinSDK", g_methodsMPinSDK, ARR_LEN(g_methodsMPinSDK));
}
