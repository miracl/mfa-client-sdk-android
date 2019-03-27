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
 * Internal M-Pin Crypto interface
 */

#ifndef _MPIN_CRYPTO_H_
#define _MPIN_CRYPTO_H_

#include <string>
#include <vector>

#include "mpin_sdk_base.h"


class IMPinCrypto
{
public:
    typedef MPinSDKBase::String String;
    typedef MPinSDKBase::Status Status;
    typedef MPinSDKBase::UserPtr UserPtr;
    typedef MPinSDKBase::RegOTT RegOTT;
    typedef MPinSDKBase::StringVector StringVector;
    typedef MPinSDKBase::MultiFactor MultiFactor;

    virtual ~IMPinCrypto() {}

    virtual Status OpenSession() = 0;
    virtual void CloseSession() = 0;
    virtual String DvsHash(const String& id) = 0;
    virtual Status GenerateSignKeypair(IN UserPtr user, OUT String& publicKey, OUT String& privateKey) = 0;
    virtual Status Register(IN UserPtr user, const MultiFactor& factors) = 0;
    virtual Status RegisterTmp(IN UserPtr user, const MultiFactor& factors) = 0;
    virtual bool PersistTmpRegistration() = 0;
    virtual void DiscardTmpRegistration() = 0;
    virtual Status AuthenticatePass1(IN UserPtr user, const MultiFactor& factors, int date, const StringVector& timePermitShares, bool dvs,
        OUT String& commitmentU, OUT String& commitmentUT) = 0;
    virtual Status AuthenticatePass2(IN UserPtr user, const String& challenge, bool dvs, OUT String& validator) = 0;
    virtual Status RegisterDVS(IN UserPtr user, const MultiFactor& factors) = 0;
    virtual Status RegisterDVSTmp(IN UserPtr user, const MultiFactor& factors) = 0;
    virtual Status Sign(IN UserPtr user, const MultiFactor& factors, const String& hash, IN int epochTime, OUT String& U, OUT String& V) = 0;
    virtual void DeleteToken(const String& mpinId) = 0;
    virtual void ClearTokens() = 0;

    virtual Status SaveRegOTT(const String& mpinId, const RegOTT& regOTT, const String& accessCode) = 0;
    virtual Status LoadRegOTT(const String& mpinId, OUT RegOTT& regOTT, OUT String& accessCode) = 0;
    virtual Status DeleteRegOTT(const String& mpinId) = 0;
};


#endif // _MPIN_CRYPTO_H_
