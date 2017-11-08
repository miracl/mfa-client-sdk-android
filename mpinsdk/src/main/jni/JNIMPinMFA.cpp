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
#include "Context.h"


typedef sdk::Context Context;

static jlong nConstruct(JNIEnv* env, jobject jobj)
{
	return (jlong) new MfaSDK();
}

static void nDestruct(JNIEnv* env, jobject jobj, jlong jptr)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	delete sdk;
}

// MPinSDKBase

static jobject nInit(JNIEnv* env, jobject jobj, jlong jptr, jobject jconfig, jobject jcontext)
{
	MfaSDK::StringMap config;
	if(jconfig)
	{
		ReadJavaMap(env, jconfig, config);
	}

	MfaSDK* sdk = (MfaSDK*) jptr;
	return MakeJavaStatus(env, sdk->Init(config, Context::Instance(jcontext)));
}

static void nAddCustomHeaders(JNIEnv* env, jobject jobj, jlong jptr, jobject jcustomHeaders)
{
	MfaSDK::StringMap customHeaders;
    if(jcustomHeaders)
    {
        ReadJavaMap(env, jcustomHeaders, customHeaders);
    }

	MfaSDK* sdk = (MfaSDK*) jptr;
	sdk->AddCustomHeaders(customHeaders);
}

static void nClearCustomHeaders(JNIEnv* env, jobject jobj, jlong jptr)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
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
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->TestBackend(JavaToStdString(env, jserver)));
}

static jobject nTestBackendRPS(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver, jstring jrpsPrefix)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->TestBackend(JavaToStdString(env, jserver), JavaToStdString(env, jrpsPrefix)));
}

static jboolean nIsUserExisting(JNIEnv* env, jobject jobj, jlong jptr, jstring jid, jstring jcustomerId, jstring jappId)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return sdk->IsUserExisting(JavaToStdString(env,jid), JavaToStdString(env,jcustomerId), JavaToStdString(env,jappId));
}

static jobject nSetBackend(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->SetBackend(JavaToStdString(env, jserver)));
}

static jobject nSetBackendRPS(JNIEnv* env, jobject jobj, jlong jptr, jstring jserver, jstring jrpsPrefix)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->SetBackend(JavaToStdString(env, jserver), JavaToStdString(env, jrpsPrefix)));
}

static jobject nMakeNewUser(JNIEnv* env, jobject jobj, jlong jptr, jstring jid, jstring jdeviceName)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    MPinSDKBase::UserPtr user = sdk->MakeNewUser(JavaToStdString(env, jid), JavaToStdString(env, jdeviceName));
    jclass clsUser = env->FindClass("com/miracl/mpinsdk/model/User");
    jmethodID ctorUser = env->GetMethodID(clsUser, "<init>", "(J)V");
    return env->NewObject(clsUser, ctorUser, (jlong) new MPinSDKBase::UserPtr(user));
}

static void nDeleteUser(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    sdk->DeleteUser(JavaToMPinUser(env, juser));
}

static void nClearUsers(JNIEnv* env, jobject jobj, jlong jptr)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    sdk->ClearUsers();
}

static jboolean nCanLogout(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return (jboolean) sdk->CanLogout(JavaToMPinUser(env, juser));
}

static jboolean nLogout(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return (jboolean) sdk->Logout(JavaToMPinUser(env, juser));
}

static jstring nGetClientParam(JNIEnv* env, jobject jobj, jlong jptr, jstring jkey)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    MfaSDK::String result = sdk->GetClientParam(JavaToStdString(env, jkey));
    return env->NewStringUTF(result.c_str());
}


// MfaSDK

static jobject nGetServiceDetails(JNIEnv* env, jobject jobj, jlong jptr, jstring jurl, jobject jserviceDetails)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    MfaSDK::ServiceDetails serviceDetails;

    MfaSDK::Status status = sdk->GetServiceDetails(JavaToStdString(env, jurl), serviceDetails);

    if(status == MPinSDK::Status::OK)
    {
        jclass clsServiceDetails = env->FindClass("com/miracl/mpinsdk/model/ServiceDetails");
        jfieldID fIdName = env->GetFieldID(clsServiceDetails, "name", "Ljava/lang/String;");
        jfieldID fIdBackendUrl = env->GetFieldID(clsServiceDetails, "backendUrl", "Ljava/lang/String;");
        jfieldID fIdRpsPrefix = env->GetFieldID(clsServiceDetails, "rpsPrefix", "Ljava/lang/String;");
        jfieldID fIdLogoUrl = env->GetFieldID(clsServiceDetails, "logoUrl", "Ljava/lang/String;");

        env->SetObjectField(jserviceDetails, fIdName, env->NewStringUTF(serviceDetails.name.c_str()));
        env->SetObjectField(jserviceDetails, fIdBackendUrl, env->NewStringUTF(serviceDetails.backendUrl.c_str()));
        env->SetObjectField(jserviceDetails, fIdRpsPrefix, env->NewStringUTF(serviceDetails.rpsPrefix.c_str()));
        env->SetObjectField(jserviceDetails, fIdLogoUrl, env->NewStringUTF(serviceDetails.logoUrl.c_str()));
    }

    return MakeJavaStatus(env, status);
}

static void nSetCID(JNIEnv* env, jobject jobj, jlong jptr, jstring jcid)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    sdk->SetCID(JavaToStdString(env, jcid));
}

static jobject nGetSessionDetails(JNIEnv* env, jobject jobj, jlong jptr, jstring jaccessCode, jobject jsessionDetails)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    MfaSDK::SessionDetails sessionDetails;

    MfaSDK::Status status = sdk->GetSessionDetails(JavaToStdString(env,jaccessCode), sessionDetails);

    if(status == MfaSDK::Status::OK)
    {
        jclass clsSessionDetails = env->FindClass("com/miracl/mpinsdk/model/SessionDetails");
        jfieldID fIdPrerollId = env->GetFieldID(clsSessionDetails, "prerollId", "Ljava/lang/String;");
        jfieldID fIdAppName = env->GetFieldID(clsSessionDetails, "appName", "Ljava/lang/String;");
        jfieldID fIdAppIconUrl = env->GetFieldID(clsSessionDetails, "appIconUrl", "Ljava/lang/String;");
        jfieldID fIdCustomerId = env->GetFieldID(clsSessionDetails, "customerId", "Ljava/lang/String;");
        jfieldID fIdCustomerName = env->GetFieldID(clsSessionDetails, "customerName", "Ljava/lang/String;");
        jfieldID fIdCustomerIconUrl = env->GetFieldID(clsSessionDetails, "customerIconUrl", "Ljava/lang/String;");

        env->SetObjectField(jsessionDetails, fIdPrerollId, env->NewStringUTF(sessionDetails.prerollId.c_str()));
        env->SetObjectField(jsessionDetails, fIdAppName, env->NewStringUTF(sessionDetails.appName.c_str()));
        env->SetObjectField(jsessionDetails, fIdAppIconUrl, env->NewStringUTF(sessionDetails.appIconUrl.c_str()));
        env->SetObjectField(jsessionDetails, fIdCustomerId, env->NewStringUTF(sessionDetails.customerId.c_str()));
        env->SetObjectField(jsessionDetails, fIdCustomerName, env->NewStringUTF(sessionDetails.customerName.c_str()));
        env->SetObjectField(jsessionDetails, fIdCustomerIconUrl, env->NewStringUTF(sessionDetails.customerIconUrl.c_str()));
    }

    return MakeJavaStatus(env, status);
}

static jobject nAbortSession(JNIEnv* env, jobject jobj, jlong jptr, jstring jaccessCode)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->AbortSession(JavaToStdString(env, jaccessCode)));
}


static jobject nStartRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jaccessCode, jstring jpushToken)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	return MakeJavaStatus(env, sdk->StartRegistration(JavaToMPinUser(env, juser), JavaToStdString(env, jaccessCode), JavaToStdString(env, jpushToken)));
}

static jobject nRestartRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	return MakeJavaStatus(env, sdk->RestartRegistration(JavaToMPinUser(env, juser)));
}

static jobject nConfirmRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	return MakeJavaStatus(env, sdk->ConfirmRegistration(JavaToMPinUser(env, juser)));
}

static jobject nFinishRegistration(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jfactor)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	return MakeJavaStatus(env, sdk->FinishRegistration(JavaToMPinUser(env, juser), MPinSDK::MultiFactor(JavaToStdString(env, jfactor))));
}

static jobject nFinishRegistrationMultiFactor(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jobjectArray jmultiFactor)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->FinishRegistration(JavaToMPinUser(env, juser), JavaStringArrayToMultiFactor(env, jmultiFactor)));
}

static jobject nGetAccessCode(JNIEnv* env, jobject jobj, jlong jptr, jstring jauthUrl, jobject jaccessCode)
{
    MfaSDK* sdk = (MfaSDK*) jptr;

	MfaSDK::String accessCode;
	MfaSDK::Status status = sdk->GetAccessCode(JavaToStdString(env, jauthUrl), accessCode);

	jclass clsStringBuilder = env->FindClass("java/lang/StringBuilder");
	jmethodID midSetLength = env->GetMethodID(clsStringBuilder, "setLength", "(I)V");
	env->CallVoidMethod(jaccessCode, midSetLength, accessCode.size());
	jmethodID midReplace = env->GetMethodID(clsStringBuilder, "replace", "(IILjava/lang/String;)Ljava/lang/StringBuilder;");
	env->CallObjectMethod(jaccessCode, midReplace, 0, accessCode.size(), env->NewStringUTF(accessCode.c_str()));

	return MakeJavaStatus(env, status);
}

static jobject nStartAuthenticationOTP(JNIEnv* env, jobject jobj, jlong jptr, jobject juser)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->StartAuthenticationOTP(JavaToMPinUser(env, juser)));
}

static jobject nStartAuthentication(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring accessCode)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->StartAuthentication(JavaToMPinUser(env, juser),JavaToStdString(env,accessCode)));
}

static jobject nFinishAuthentication(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jfactor, jstring jaccessCode)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	return MakeJavaStatus(env, sdk->FinishAuthentication(JavaToMPinUser(env, juser), MPinSDK::MultiFactor(JavaToStdString(env, jfactor)), JavaToStdString(env, jaccessCode)));
}

static jobject nFinishAuthenticationMultiFactor(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jobjectArray jmultiFactor, jstring jaccessCode)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return MakeJavaStatus(env, sdk->FinishAuthentication(JavaToMPinUser(env, juser), JavaStringArrayToMultiFactor(env, jmultiFactor), JavaToStdString(env, jaccessCode)));
}

static jobject FinishAuthenticationAuthCode(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, MPinSDK::MultiFactor multiFactor, jstring jaccessCode, jobject jauthCode)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    MfaSDK::String authCodeData;
    MfaSDK::Status status = sdk->FinishAuthentication(JavaToMPinUser(env, juser), multiFactor, JavaToStdString(env, jaccessCode), authCodeData);

    jclass clsStringBuilder = env->FindClass("java/lang/StringBuilder");
    jmethodID midSetLength = env->GetMethodID(clsStringBuilder, "setLength", "(I)V");
    env->CallVoidMethod(jauthCode, midSetLength, authCodeData.size());
    jmethodID midReplace = env->GetMethodID(clsStringBuilder, "replace", "(IILjava/lang/String;)Ljava/lang/StringBuilder;");
    env->CallObjectMethod(jauthCode, midReplace, 0, authCodeData.size(), env->NewStringUTF(authCodeData.c_str()));

    return MakeJavaStatus(env, status);
}

static jobject nFinishAuthenticationAuthCode(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jfactor, jstring jaccessCode, jobject jauthCode)
{
    return FinishAuthenticationAuthCode(env, jobj, jptr, juser, MPinSDK::MultiFactor(JavaToStdString(env, jfactor)), jaccessCode, jauthCode);
}

static jobject nFinishAuthenticationAuthCodeMultiFactor(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jobjectArray jmultiFactor, jstring jaccessCode, jobject jauthCode)
{
    return FinishAuthenticationAuthCode(env, jobj, jptr, juser, JavaStringArrayToMultiFactor(env, jmultiFactor), jaccessCode, jauthCode);
}

static jobject FinishAuthenticationOTP(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, MPinSDK::MultiFactor multiFactor, jobject jotp)
{
    MfaSDK* sdk = (MfaSDK*) jptr;

    MfaSDK::OTP otp;
    MfaSDK::Status status = sdk->FinishAuthenticationOTP(JavaToMPinUser(env, juser), multiFactor, otp);

    if(status == MfaSDK::Status::OK)
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

static jobject nFinishAuthenticationOTP(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jstring jfactor, jobject jotp)
{
    return FinishAuthenticationOTP(env, jobj, jptr, juser, MPinSDK::MultiFactor(JavaToStdString(env, jfactor)), jotp);
}

static jobject nFinishAuthenticationOTPMultiFactor(JNIEnv* env, jobject jobj, jlong jptr, jobject juser, jobjectArray jMultiFactor, jobject jotp)
{
    return FinishAuthenticationOTP(env, jobj, jptr, juser, JavaStringArrayToMultiFactor(env, jMultiFactor), jotp);
}

static jboolean nVerifyDocumentHash(JNIEnv* env, jobject jobj,jlong jptr, jstring jdocument, jbyteArray jhash)
{
    MfaSDK* sdk = (MfaSDK*) jptr;
    return (jboolean) sdk->VerifyDocumentHash(JavaToStdString(env, jdocument), JavaByteArrayToStdString(env, jhash));
}

static jobject Sign(JNIEnv* env, jobject jobj,jlong jptr, jobject juser, jbyteArray jdocumentHash, MPinSDK::MultiFactor multiFactor, jint jepochTime, jobject jsignature)
{
    MfaSDK* sdk = (MfaSDK*) jptr;

    MfaSDK::Signature signature;
    MfaSDK::Status status = sdk->Sign(JavaToMPinUser(env, juser), JavaByteArrayToStdString(env, jdocumentHash), multiFactor, jepochTime, signature);

    if(status == MfaSDK::Status::OK)
    {
        jclass clsSignature = env->FindClass("com/miracl/mpinsdk/model/Signature");
        jfieldID fidHash = env->GetFieldID(clsSignature, "hash", "[B");
        jfieldID fidMpinId = env->GetFieldID(clsSignature, "mpinId", "[B");
        jfieldID fidU = env->GetFieldID(clsSignature, "u", "[B");
        jfieldID fidV = env->GetFieldID(clsSignature, "v", "[B");
        jfieldID fidPublicKey = env->GetFieldID(clsSignature, "publicKey", "[B");

        env->SetObjectField(jsignature, fidHash, StdStringToJavaByteArray(env, signature.hash));
        env->SetObjectField(jsignature, fidMpinId, StdStringToJavaByteArray(env, signature.mpinId));
        env->SetObjectField(jsignature, fidU, StdStringToJavaByteArray(env, signature.u));
        env->SetObjectField(jsignature, fidV, StdStringToJavaByteArray(env, signature.v));
        env->SetObjectField(jsignature, fidPublicKey, StdStringToJavaByteArray(env, signature.publicKey));
    }

    return MakeJavaStatus(env, status);
}

static jobject nSign(JNIEnv* env, jobject jobj,jlong jptr, jobject juser, jbyteArray jdocumentHash, jstring jfactor, jint jepochTime, jobject jsignature)
{
    return Sign(env, jobj, jptr, juser, jdocumentHash, MPinSDK::MultiFactor(JavaToStdString(env, jfactor)), jepochTime, jsignature);
}

static jobject nSignMultiFactor(JNIEnv* env, jobject jobj,jlong jptr, jobject juser, jbyteArray jdocumentHash, jobjectArray jMultiFactor, jint jepochTime, jobject jsignature)
{
    return Sign(env, jobj, jptr, juser, jdocumentHash, JavaStringArrayToMultiFactor(env, jMultiFactor), jepochTime, jsignature);
}

static jobject nListUsers(JNIEnv* env, jobject jobj, jlong jptr, jobject jusersList)
{
	MfaSDK* sdk = (MfaSDK*) jptr;
	std::vector<MPinSDKBase::UserPtr> users;
	MfaSDK::Status status = sdk->ListUsers(users);

    if(status == MfaSDK::Status::OK)
    {
        jclass clsList = env->FindClass("java/util/List");
        jmethodID midAdd = env->GetMethodID(clsList, "add", "(Ljava/lang/Object;)Z");

        jclass clsUser = env->FindClass("com/miracl/mpinsdk/model/User");
        jmethodID ctorUser = env->GetMethodID(clsUser, "<init>", "(J)V");

        for (std::vector<MPinSDKBase::UserPtr>::iterator i = users.begin(); i != users.end(); ++i) {
            MPinSDKBase::UserPtr user = *i;
            jobject juser = env->NewObject(clsUser, ctorUser, (jlong) new MPinSDKBase::UserPtr(user));
            env->CallBooleanMethod(jusersList, midAdd, juser);
        }
    }

    return MakeJavaStatus(env,status);
}

// Mappings for java native methods
static JNINativeMethod g_methodsMfaSDK[] =
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
    NATIVE_METHOD(nGetServiceDetails, "(JLjava/lang/String;Lcom/miracl/mpinsdk/model/ServiceDetails;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nGetSessionDetails, "(JLjava/lang/String;Lcom/miracl/mpinsdk/model/SessionDetails;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nSetCID, "(JLjava/lang/String;)V"),
    NATIVE_METHOD(nAbortSession, "(JLjava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nStartRegistration, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nRestartRegistration, "(JLcom/miracl/mpinsdk/model/User;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nConfirmRegistration, "(JLcom/miracl/mpinsdk/model/User;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishRegistration, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nFinishRegistrationMultiFactor, "(JLcom/miracl/mpinsdk/model/User;[Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nGetAccessCode, "(JLjava/lang/String;Ljava/lang/StringBuilder;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nStartAuthenticationOTP, "(JLcom/miracl/mpinsdk/model/User;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nStartAuthentication, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishAuthentication, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nFinishAuthenticationMultiFactor, "(JLcom/miracl/mpinsdk/model/User;[Ljava/lang/String;Ljava/lang/String;)Lcom/miracl/mpinsdk/model/Status;"),
	NATIVE_METHOD(nFinishAuthenticationAuthCode, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Ljava/lang/String;Ljava/lang/StringBuilder;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nFinishAuthenticationAuthCodeMultiFactor, "(JLcom/miracl/mpinsdk/model/User;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/StringBuilder;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nFinishAuthenticationOTP, "(JLcom/miracl/mpinsdk/model/User;Ljava/lang/String;Lcom/miracl/mpinsdk/model/OTP;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nFinishAuthenticationOTPMultiFactor, "(JLcom/miracl/mpinsdk/model/User;[Ljava/lang/String;Lcom/miracl/mpinsdk/model/OTP;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nVerifyDocumentHash, "(JLjava/lang/String;[B)Z"),
    NATIVE_METHOD(nSign, "(JLcom/miracl/mpinsdk/model/User;[BLjava/lang/String;ILcom/miracl/mpinsdk/model/Signature;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nSignMultiFactor, "(JLcom/miracl/mpinsdk/model/User;[B[Ljava/lang/String;ILcom/miracl/mpinsdk/model/Signature;)Lcom/miracl/mpinsdk/model/Status;"),
    NATIVE_METHOD(nListUsers, "(JLjava/util/List;)Lcom/miracl/mpinsdk/model/Status;")
};

void RegisterMPinMFAJNI(JNIEnv* env)
{
	RegisterNativeMethods(env, "com/miracl/mpinsdk/MPinMFA", g_methodsMfaSDK, ARR_LEN(g_methodsMfaSDK));
}
