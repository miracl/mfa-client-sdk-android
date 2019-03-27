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
* M-Pin SDK Base interface
*/

#ifndef _MPIN_SDK_BASE_H_
#define _MPIN_SDK_BASE_H_

#include <string>
#include <map>
#include <vector>
#include <memory>

#include "utils.h"

#ifdef _WIN32
#include <windows.h>
#undef DELETE
#undef REGISTERED
#endif

class IMPinCrypto;

class MPinSDKBase
{
public:
    typedef util::String String;
    typedef util::StringMap StringMap;
    typedef std::vector<String> StringVector;
    class User;
    typedef std::shared_ptr<User> UserPtr;

    enum CryptoType
    {
        CRYPTO_TEE,
        CRYPTO_NON_TEE
    };

    class IHttpRequest
    {
    public:
        enum Method
        {
            GET,
            POST,
            PUT,
            DELETE,
            OPTIONS,
            PATCH,
        };

        static const char *CONTENT_TYPE_HEADER;
        static const char *ACCEPT_HEADER;
        static const char *JSON_CONTENT_TYPE;
        static const char *TEXT_PLAIN_CONTENT_TYPE;

        virtual ~IHttpRequest() {}
        virtual void SetHeaders(const StringMap& headers) = 0;
        virtual void SetQueryParams(const StringMap& queryParams) = 0;
        virtual void SetContent(const String& data) = 0;
        virtual void SetTimeout(int seconds) = 0;
        virtual bool Execute(Method method, const String& url) = 0;
        virtual const String& GetExecuteErrorMessage() const = 0;
        virtual int GetHttpStatusCode() const = 0;
        virtual const StringMap& GetResponseHeaders() const = 0;
        virtual const String& GetResponseData() const = 0;
    };

    typedef IHttpRequest::Method HttpMethod;

    class IStorage
    {
    public:
        enum Type
        {
            SECURE,
            NONSECURE,
        };

        virtual ~IStorage() {}
        virtual bool SetData(const String& data) = 0;
        virtual bool GetData(OUT String &data) = 0;
        virtual bool ClearData() = 0;
        virtual const String& GetErrorMessage() const = 0;
    };

    class IContext
    {
    public:
        virtual ~IContext() {}
        virtual IHttpRequest * CreateHttpRequest() const = 0;
        virtual void ReleaseHttpRequest(IN IHttpRequest *request) const = 0;
        virtual IStorage * GetStorage(IStorage::Type type) const = 0;
        virtual CryptoType GetMPinCryptoType() const = 0;
    };

    class Status
    {
    public:
        enum Code
        {
            OK,
            PIN_INPUT_CANCELED, // Local error, returned when user cancels pin entering
            CRYPTO_ERROR, // Local error in crypto functions
            STORAGE_ERROR, // Local storage related error
            NETWORK_ERROR, // Local error - cannot connect to remote server (no internet, or invalid server/port)
            RESPONSE_PARSE_ERROR, // Local error - cannot parse json response from remote server (invalid json or unexpected json structure)
            FLOW_ERROR, // Local error - unproper MPinSDK class usage
            IDENTITY_NOT_AUTHORIZED, // Remote error - the remote server refuses user registration or authentication
            IDENTITY_NOT_VERIFIED, // Remote error - the remote server refuses user registration because identity is not verified
            REQUEST_EXPIRED, // Remote error - the register/authentication request expired
            REVOKED, // Remote error - cannot get time permit (propably the user is temporary suspended)
            INCORRECT_PIN, // Remote error - user entered wrong pin
            INCORRECT_ACCESS_NUMBER, // Remote/local error - wrong access number (checksum failed or RPS returned 412)
            HTTP_SERVER_ERROR, // Remote error, that was not reduced to one of the above - the remote server returned internal server error status (5xx)
            HTTP_REQUEST_ERROR, // Remote error, that was not reduced to one of the above - invalid data sent to server, the remote server returned 4xx error status
            BAD_USER_AGENT, // Remote error - user agent not supported
            CLIENT_SECRET_EXPIRED, // Remote error - re-registration required because server master secret expired
            BAD_CLIENT_VERSION, // Remote error - wrong client app version
            UNTRUSTED_DOMAIN_ERROR, // Local error - a request to a domain, that is not in the trusted list was attempted
            REGISTRATION_EXPIRED, // Remote error - regOTT expired
            OPERATION_NOT_ALLOWED, // Remote error - RegCode generation not allowed for users, registered with RegCode
        };

        Status();
        Status(Code statusCode);
        Status(Code statusCode, const String& error);
        Code GetStatusCode() const;
        String GetStatusCodeString() const;
        const String& GetErrorMessage() const;
        void SetStatusCode(Code statusCode);
        void SetErrorMessage(const String& error);
        bool operator==(Code statusCode) const;
        bool operator!=(Code statusCode) const;

    private:
        Code m_statusCode;
        String m_errorMessage;
    };

    static const String VERIFICATION_TYPE_EMAIL;
    static const String VERIFICATION_TYPE_CUSTOM;
    static const String VERIFICATION_TYPE_REG_CODE;
    static const String VERIFICATION_TYPE_DVS_REG_TOKEN;

protected:
    class TimePermitCache
    {
    public:
        TimePermitCache();
        const String& GetTimePermit() const;
        int GetDate() const;
        void Set(const String& timePermit, int date);
        void Invalidate();

    private:
        String m_timePermit;
        int m_date;
    };

public:
    class Expiration
    {
    public:
        Expiration();
        int expireTimeSeconds;
        int nowTimeSeconds;
    };

    class RegOTT : public std::pair<String, String>
    {
    public:
        ~RegOTT();
        void Split(const String& fullRegOTT);
        String Recombine() const;
        bool IsEmpty() const;
        bool IsFull() const;
        void Clear();
        void Overwrite();
    };

    class User
    {
    public:
        enum State
        {
            INVALID,
            STARTED_REGISTRATION,
            ACTIVATED,
            REGISTERED,
            BLOCKED,
        };

        const String& GetId() const;
        const String& GetBackend() const;
        const String& GetCustomerId() const;
        const String& GetAppId() const;
        const String& GetMPinId() const;
        const Expiration& GetRegistrationExpiration() const;
        bool CanSign() const;
        State GetState() const;
        String GetStateString() const;
        static String StateToString(State state);
        int GetPinLength() const;
        String GetVerificationType() const;

    private:
        friend class MPinSDKBase;
        friend class MPinCryptoNonTee;
        User(const String& id, const String& deviceName);
        String GetKey() const;
        const String& GetDeviceName() const;
        String GetRegOTT() const;
        const String& GetAccessCode() const;
        const TimePermitCache& GetTimePermitCache() const;
        void CacheTimePermit(const String& timePermit, int date);
        void SetBackend(const String& backend);
        void SetStartedRegistration(const String& mpinId, const RegOTT& regOTT, const String& accessCode, const String& customerId, const String& appId);
        void SetRegistrationExpiration(long expireTime, long nowTime);
        void SetActivated();
        void SetRegistered();
        void SetPinLength(int pinLength);
        void Invalidate();
        void Block();
        Status RestoreState(const String& stateString, const String& mpinId, const String& curve, const RegOTT& regOTT, const String& accessCode, const String& backend,
            const String& customerId, const String& appId, const String& signPublicKey, const String& signMPinId, const String& signCurve);
        static State StringToState(const String& stateString);
        static String MakeKey(const String& id, const String& backend, const String& customerId, const String& appId);

        String m_id;
        String m_backend;
        String m_customerId;
        String m_appId;
        String m_deviceName;
        State m_state;
        String m_mpinId;
        String m_curve;
        String m_dtas;
        RegOTT m_regOTT;
        Expiration m_regOTTExpiration;
        String m_accessCode;
        TimePermitCache m_timePermitCache;
        String m_timePermitShare1;
        String m_timePermitShare2;
        String m_clientSecret1;
        String m_clientSecret2;
        bool m_canSign;
        bool m_signRegStarted;
        String m_signPublicKey;
        String m_signPrivateKey;
        String m_signMPinId;
        String m_signCurve;
        String m_signDtas;
        String m_signClientSecret1;
        String m_signClientSecret2;
        int m_pinLength;
        String m_verificationType;
    };

    class MultiFactor : public StringVector
    {
    public:
        MultiFactor();
        MultiFactor(const char *pin);
        MultiFactor(const std::string& pin);
        bool IsEmpty() const;
        std::vector<int> ToInt(int pinLength) const;
    };

    class OTP
    {
    public:
        OTP();
        void ExtractFrom(const util::JsonObject& json);
        bool IsForRegistration() const;

        String otp;
        long expireTime;
        int ttlSeconds;
        long nowTime;
        Status status;

    protected:
        OTP(bool forRegistration);
        bool m_forRegistration;
    };

    class RegCode : public OTP
    {
    public:
        RegCode();
    };

    class Signature
    {
    public:
        String hash;
        String mpinId;
        String u;
        String v;
        String publicKey;
        String dtas;
    };

protected:
    MPinSDKBase();

public:
    ~MPinSDKBase();

    Status Init(const StringMap& config, IN IContext* ctx);
    void Destroy();
    bool IsInitilized() const;
    bool IsBackendSet() const;

    void AddCustomHeaders(const StringMap& customHeaders);
    void ClearCustomHeaders();

    void AddTrustedDomain(const String& domain);
    void ClearTrustedDomains();

    Status TestBackend(const String& backend) const;
    Status TestBackend(const String& backend, const String& rpsPrefix) const;
    Status SetBackend(const String& backend);
    Status SetBackend(const String& backend, const String& rpsPrefix);

    bool IsUserExisting(const String& id, const String& customerId = "", const String& appId = "");
    UserPtr MakeNewUser(const String& id, const String& deviceName = "") const;
    void DeleteUser(INOUT UserPtr user);
    void ClearUsers();

    bool CanLogout(IN UserPtr user);
    bool Logout(IN UserPtr user);

    String GetClientParam(const String& key);

    static const char *CONFIG_BACKEND;
    static const char *CONFIG_RPS_PREFIX;

protected:
    class HttpResponse
    {
    public:
        static const int NON_HTTP_ERROR = -1;
        static const int HTTP_OK = 200;
        static const int HTTP_BAD_REQUEST = 400;
        static const int HTTP_UNAUTHORIZED = 401;
        static const int HTTP_FORBIDDEN = 403;
        static const int HTTP_NOT_FOUND = 404;
        static const int HTTP_NOT_ACCEPTABLE = 406;
        static const int HTTP_REQUEST_TIMEOUT = 408;
        static const int HTTP_CONFLICT = 409;
        static const int HTTP_GONE = 410;
        static const int HTTP_PRECONDITION_FAILED = 412;

        enum Context
        {
            GET_SERVICE_DETAILS,
            GET_CLIENT_SETTINGS,
            REGISTER,
            REGISTER_DVS,
            GET_CLIENT_SECRET1,
            GET_CLIENT_SECRET2,
            GET_TIME_PERMIT1,
            GET_TIME_PERMIT2,
            AUTHENTICATE_PASS1,
            AUTHENTICATE_PASS2,
            AUTHENTICATE_RPA,
            GET_SESSION_DETAILS,
            ABORT_SESSION,
            GET_ACCESS_CODE,
        };

        enum DataType
        {
            JSON,
            RAW,
        };

        HttpResponse(const String& requestUrl, const String& requestBody);

        int GetStatus() const;
        bool SetData(const String& rawData, const StringMap& headers, DataType expectedType);
        const util::JsonObject& GetJsonData() const;
        const String& GetRawData() const;
        const StringMap& GetHeaders() const;
        void SetNetworkError(const String& error);
        void SetResponseJsonParseError(const String& responseJson, const String& jsonParseError);
        void SetHttpError(int httpStatus);
        void SetUntrustedDomainError(const String& error);
        Status TranslateToMPinStatus(Context context);

    private:
        int m_httpStatus;
        util::JsonObject m_jsonData;
        String m_rawData;
        StringMap m_headers;
        Status m_mpinStatus;
        String m_requestUrl;
        String m_requestBody;
    };

    enum State
    {
        NOT_INITIALIZED,
        INITIALIZED,
        BACKEND_SET,
    };

    class LogoutData
    {
    public:
        bool ExtractFrom(const util::JsonObject& json);

        String logoutData;
        String logoutURL;
    };

    enum AuthType
    {
        NORMAL_AUTH,
        DVS_REG_AUTH,
        DVS_SIGN_AUTH,
    };

    Status CheckIfIsInitialized() const;
    Status CheckIfBackendIsSet() const;
    Status CheckUrl(const String& url) const;
    HttpResponse MakeRequest(const String& url, HttpMethod method, const util::JsonObject& bodyJson, HttpResponse::DataType expectedResponseType = HttpResponse::JSON) const;
    HttpResponse MakeGetRequest(const String& url, HttpResponse::DataType expectedResponseType = HttpResponse::JSON) const;
    Status RewriteRelativeUrls();
    Status GetClientSettings(const String& backend, const String& rpsPrefix, OUT util::JsonObject *clientSettings) const;
    String MakeBackendKey(const String& backendServer) const;
    Status CheckUserState(IN UserPtr user, User::State expectedState);
    Status CheckUserIsAwaitingRegistration(IN UserPtr user);
    bool IsUserKeyExisting(const String& key);

    Status StartRegistration(INOUT UserPtr user, const String& activateCode, const String& userData, const String& accessCode, const String& pushToken);
    Status RestartRegistration(INOUT UserPtr user, const String& userData);
    Status RequestRegistration(INOUT UserPtr user, const String& activateCode, const String& accessCode, const String& pushToken, const String& userData);
    Status SetRegistrationToken(INOUT UserPtr user, const String& regToken);
    bool IsRegistrationTokenSet(INOUT UserPtr user) const;
    Status ConfirmRegistration(INOUT UserPtr user, const String& pushToken);
    Status FinishRegistration(INOUT UserPtr user, const MultiFactor& factors);

    Status StartAuthentication(INOUT UserPtr user, const String& accessCode);
    Status AuthenticateImpl(AuthType authType, INOUT UserPtr user, const MultiFactor& factors,
        const String& accessCode, OUT OTP *otp, OUT util::JsonObject& authResultData, bool authzRequest);
    Status FinishAuthenticationImpl(INOUT UserPtr user, const MultiFactor& factors,
        const String& accessCode, OUT OTP *otp, OUT util::JsonObject& authResultData, bool authzRequest = false);
    Status GetCertivoxTimePermitShare(INOUT UserPtr user, const util::JsonObject& cutomerTimePermitData, OUT String& resultTimePermit);
    Status FinishAuthenticationAC(INOUT UserPtr user, const MultiFactor& factors, const String& accessCode);
    Status FinishAuthenticationOTP(INOUT UserPtr user, const MultiFactor& factors, OUT OTP& otp);
    void RenewSecret(INOUT UserPtr user, const MultiFactor& factors, const util::JsonObject& renewSecretJson);

    Status DvsRegAuthenticate(INOUT UserPtr user, const MultiFactor& factors, OUT String& dvsRegToken, OUT String& dvsCurve);
    Status DvsSignAuthenticate(INOUT UserPtr user, const MultiFactor& factors);
    Status DoDvsRegister(INOUT UserPtr user, const String& dvsRegToken);
    Status StartRegistrationDVS(INOUT UserPtr user, const MultiFactor& authFactors);
    Status FinishRegistrationDVS(INOUT UserPtr user, const MultiFactor& factors);
    String HashDocument(const String& document) const;
    Status Sign(IN UserPtr user, const String& documentHash, const MultiFactor& factors, int epochTime, OUT Signature& result);
    void RenewDVSSecret(INOUT UserPtr user, const MultiFactor& factors, const util::JsonObject& renewSecretJson);

    void BlockUser(UserPtr user);
    template <typename Filter>
    Status FilterUsers(OUT std::vector<UserPtr>& users, const Filter& filter) const;
    Status ListAllUsers(OUT std::vector<UserPtr>& users) const;

    Status WriteUsersToStorage() const;
    Status LoadUsersFromStorage();

    static void AddScope(AuthType authType, const OTP *otp, json::Object& dst);

    static const char *DEFAULT_RPS_PREFIX;
    static const int AN_WITH_CHECKSUM_LEN = 7;

protected:
    typedef std::map<String, UserPtr> UsersMap;
    typedef std::map<UserPtr, LogoutData> LogoutDataMap;
    typedef std::vector<util::Url> UrlVector;
    State m_state;
    IContext *m_context;
    IMPinCrypto *m_crypto;
    String m_RPAServer;
    String m_rpsPrefix;
    util::JsonObject m_clientSettings;
    UsersMap m_users;
    LogoutDataMap m_logoutData;
    StringMap m_customHeaders;
    UrlVector m_trustedDomains;
};

template <typename Filter>
MPinSDKBase::Status MPinSDKBase::FilterUsers(OUT std::vector<UserPtr>& users, const Filter& filter) const
{
    Status s = CheckIfIsInitialized();
    if (s != Status::OK)
    {
        return s;
    }

    users.clear();
    users.reserve(m_users.size());

    for (UsersMap::const_iterator i = m_users.begin(); i != m_users.end(); ++i)
    {
        if (filter(i->second))
        {
            users.push_back(i->second);
        }
    }

    return Status::OK;
}

#endif // _MPIN_SDK_BASE_H_
