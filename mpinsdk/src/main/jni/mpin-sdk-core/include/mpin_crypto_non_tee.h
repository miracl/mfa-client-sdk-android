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
 * Internal M-Pin Crypto interface Non-TEE implementation
 */

#ifndef _MPIN_CRYPTO_NON_TEE_H_
#define _MPIN_CRYPTO_NON_TEE_H_

#include "mpin_crypto.h"

namespace amcl
{
    class MPin;
}

class MPinCryptoNonTee : public IMPinCrypto
{
public:
    typedef MPinSDKBase::IStorage IStorage;
    typedef util::JsonObject JsonObject;
    typedef MPinSDKBase::UserPtr UserPtr;

    MPinCryptoNonTee();
    ~MPinCryptoNonTee();

    Status Init(IN IStorage *storage);
    void Destroy();

    virtual Status OpenSession();
    virtual void CloseSession();
    virtual String DvsHash(const String& id);
    virtual Status GenerateSignKeypair(IN UserPtr user, OUT String& publicKey, OUT String& privateKey);
    virtual Status Register(IN UserPtr user, const MultiFactor& factors);
    virtual Status RegisterTmp(IN UserPtr user, const MultiFactor& factors);
    virtual bool PersistTmpRegistration();
    virtual void DiscardTmpRegistration();
    virtual Status AuthenticatePass1(IN UserPtr user, const MultiFactor& factors, int date, const StringVector& timePermitShares, bool dvs,
        OUT String& commitmentU, OUT String& commitmentUT);
    virtual Status AuthenticatePass2(IN UserPtr user, const String& challenge, bool dvs, OUT String& validator);
    virtual Status RegisterDVS(IN UserPtr user, const MultiFactor& factors);
    virtual Status RegisterDVSTmp(IN UserPtr user, const MultiFactor& factors);
    virtual Status Sign(IN UserPtr user, const MultiFactor& factors, const String& hash, IN int epochTime, OUT String& U, OUT String& V);
    virtual void DeleteToken(const String& mpinId);
    virtual void ClearTokens();

    virtual Status SaveRegOTT(const String& mpinId, const RegOTT& regOTT, const String& accessCode);
    virtual Status LoadRegOTT(const String& mpinId, OUT RegOTT& regOTT, OUT String& accessCode);
    virtual Status DeleteRegOTT(const String& mpinId);

private:
    Status Register(IN UserPtr user, const MultiFactor& factors, bool tmp);
    Status RegisterDVS(IN UserPtr user, const MultiFactor& factors, bool tmp);
    bool StoreToken(const String& mpinId, const String& token);
    String GetToken(const String& mpinId);
    void SaveDataForPass2(const String& mpinId, const String& clientSecret, const String& x);
    void ForgetPass2Data();
    static bool GenerateRandomSeed(OUT char *buf, size_t len);

private:
    IStorage *m_storage;
    bool m_initialized;
    bool m_sessionOpened;
    String m_mpinId;
    String m_clientSecret;
    String m_x;
    JsonObject m_tokens;
    std::shared_ptr<amcl::MPin> m_crypto;
};


#endif // _MPIN_CRYPTO_NON_TEE_H_
