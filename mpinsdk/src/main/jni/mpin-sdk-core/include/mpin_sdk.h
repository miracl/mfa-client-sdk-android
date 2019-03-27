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
 * M-Pin SDK interface
 */

#ifndef _MPIN_SDK_H_
#define _MPIN_SDK_H_

#include "mpin_sdk_base.h"

class MPinSDK : public MPinSDKBase
{
public:
    MPinSDK();

    Status StartRegistration(INOUT UserPtr user, const String& activateCode = "", const String& userData = "");
    Status RestartRegistration(INOUT UserPtr user, const String& userData = "");
    Status ConfirmRegistration(INOUT UserPtr user, const String& pushToken = "");
    Status FinishRegistration(INOUT UserPtr user, const MultiFactor& factors);

    Status StartAuthentication(INOUT UserPtr user);
    Status CheckAccessNumber(const String& accessNumber);
    Status FinishAuthentication(INOUT UserPtr user, const MultiFactor& factors);
    Status FinishAuthentication(INOUT UserPtr user, const MultiFactor& factors, OUT String& authResultData);
    Status FinishAuthenticationOTP(INOUT UserPtr user, const MultiFactor& factors, OUT OTP& otp);
    Status FinishAuthenticationAN(INOUT UserPtr user, const MultiFactor& factors, const String& accessNumber);

    Status ListUsers(OUT std::vector<UserPtr>& users, const String& backend) const;
    Status ListUsers(OUT std::vector<UserPtr>& users) const;
    Status ListAllUsers(OUT std::vector<UserPtr>& users) const;
    Status ListBackends(OUT std::vector<String>& backends) const;

private:
    bool ValidateAccessNumber(const String& accessNumber);
    bool ValidateAccessNumberChecksum(const String& accessNumber);
};

#endif // _MPIN_SDK_H_
