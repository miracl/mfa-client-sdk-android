/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

/*
* MFA SDK interface
*/

#ifndef _MFA_SDK_H_
#define _MFA_SDK_H_

#include "mpin_sdk_base.h"

class MfaSDK : public MPinSDKBase
{
public:
    class ServiceDetails
    {
    public:
        String name;
        String backendUrl;
        String rpsPrefix;
        String logoUrl;
    };

    class SessionDetails
    {
    public:
        void Clear();

        String prerollId;
        String appName;
        String appIconUrl;
        String customerId;
        String customerName;
        String customerIconUrl;
        bool registerOnly;
    };

    MfaSDK();
    Status Init(const StringMap& config, IN IContext* ctx);

    Status GetServiceDetails(const String& url, OUT ServiceDetails& serviceDetails);
    void SetCID(const String& cid);
    void ClearCustomHeaders();

    Status GetSessionDetails(const String& accessCode, OUT SessionDetails& sessionDetails);
    Status AbortSession(const String& accessCode);

    Status StartRegistration(INOUT UserPtr user, const String& accessCode, const String& pushToken);
    Status StartRegistration(INOUT UserPtr user, const String& accessCode, const String& pushToken, const String& regCode);
    Status RestartRegistration(INOUT UserPtr user);
    Status SetRegistrationToken(INOUT UserPtr user, const String& regToken);
	bool IsRegistrationTokenSet(INOUT UserPtr user) const;
    Status ConfirmRegistration(INOUT UserPtr user);
    Status FinishRegistration(INOUT UserPtr user, const MultiFactor& factors);

    Status GetAccessCode(const String& authzUrl, OUT String& accessCode);

    Status StartAuthentication(INOUT UserPtr user, const String& accessCode);
    Status FinishAuthentication(INOUT UserPtr user, const MultiFactor& factors, const String& accessCode);
    Status FinishAuthentication(INOUT UserPtr user, const MultiFactor& factors, const String& accessCode, OUT String& authzCode);
    Status StartAuthenticationOTP(INOUT UserPtr user);
    Status FinishAuthenticationOTP(INOUT UserPtr user, const MultiFactor& factors, OUT OTP& otp);
    Status StartAuthenticationRegCode(INOUT UserPtr user);
    Status FinishAuthenticationRegCode(INOUT UserPtr user, const MultiFactor& factors, OUT RegCode& regCode);

    Status StartRegistrationDVS(INOUT UserPtr user, const MultiFactor& authFactors);
    Status FinishRegistrationDVS(INOUT UserPtr user, const MultiFactor& factors);
    String HashDocument(const String& document) const;
    Status Sign(IN UserPtr user, const String& documentHash, const MultiFactor& factors, int epochTime, OUT Signature& result);

    Status ListUsers(OUT std::vector<UserPtr>& users) const;
};

#endif // _MFA_SDK_H_

#include "mpin_sdk_base.h"

