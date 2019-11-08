# Android Mobile SDK for MIRACL MFA Platform

## Building the MFA Mobile SDK for Android

### Prerequisites

1. Download and install Android Studio or higher with Android SDK 16 or higher
1. Download or Clone the project

### Building the MFA Mobile SDK

#### From Android Studio
1. Import the project - File-> Open -> \<mfa-client-sdk-android\>
1. From Gradle Tool View select :mpinsdk -> Tasks -> build -> build
1. The assembled aars will be located in \<mfa-client-sdk-android\>/mpinsdk/build/outputs/aar

#### From Command Line
1. Navigate to \<mfa-client-sdk-android\>
1. Execute ./gradlew build
1. The assembled aar's will be located in \<mfa-client-sdk-android\>/mpinsdk/build/outputs/aar

For further details, see [MIRACL MFA Mobile SDK for Android Documentation](https://devdocs.trust.miracl.cloud/mobile-sdk-instructions/)

## Android SDK API for MIRACL MFA (`com.miracl.mpinsdk.MPinMFA`)

This flavor of the SDK should be used to build apps that authenticate users against the _MIRACL MFA Platform_.
Most of the methods return a `Status` object which is defined as follows:

```java
public class Status {

    public enum Code {
        OK,
        PIN_INPUT_CANCELED,      // Local error, returned when user cancels pin entering
        CRYPTO_ERROR,            // Local error in crypto functions
        STORAGE_ERROR,           // Local storage related error
        NETWORK_ERROR,           // Local error - cannot connect to remote server (no internet, or invalid server/port)
        RESPONSE_PARSE_ERROR,    // Local error - cannot parse json response from remote server (invalid json or unexpected json structure)
        FLOW_ERROR,              // Local error - improper MPinMFA class usage
        IDENTITY_NOT_AUTHORIZED, // Remote error - the remote server refuses user registration or authentication
        IDENTITY_NOT_VERIFIED,   // Remote error - the remote server refuses user registration because identity is not verified
        REQUEST_EXPIRED,         // Remote error - the register/authentication request expired
        REVOKED,                 // Remote error - cannot get time permit (probably the user is temporary suspended)
        INCORRECT_PIN,           // Remote error - user entered wrong pin
        INCORRECT_ACCESS_NUMBER, // Remote/local error - wrong access number (checksum failed or RPS returned 412)
        HTTP_SERVER_ERROR,       // Remote error, that was not reduced to one of the above - the remote server returned internal server error status (5xx)
        HTTP_REQUEST_ERROR,      // Remote error, that was not reduced to one of the above - invalid data sent to server, the remote server returned 4xx error status
        BAD_USER_AGENT,          // Remote error - user agent not supported
        CLIENT_SECRET_EXPIRED,   // Remote error - re-registration required because server master secret expired
        BAD_CLIENT_VERSION,      // Remote error - wrong client app version
        UNTRUSTED_DOMAIN_ERROR,  // Local error - a request to a domain, that is not in the trusted list was attempted
        REGISTRATION_EXPIRED,    // Remote error - regOTT expired
        OPERATION_NOT_ALLOWED,   // Remote error - RegCode generation not allowed for users, registered with RegCode
        VERIFICATION_FAILED      // Remote error - Verification failed because of server error or invalid user id
    }

    public Status(int statusCode, String error) {
        ...
    }

    public Code getStatusCode() {
        ...
    }

    public String getErrorMessage() {
        ...
    }

    @Override
    public String toString() {
        ...
    }

    ...
}
```

The methods that return `Status`, will always return status `OK` if successful.
Many methods expect the provided `User` object to be in a certain state, and if it is not, the method will return status `FLOW_ERROR`

##### `MPinMFA(Context context)`
This method constructs an SDK instance.

##### `Status init(Map<String, String> config, Context context)`
This method initializes the SDK. It receives a key/value map of the configuration parameters.
The configuration is a key-value map into which different configuration options can be inserted. This is a flexible way of passing configurations to the SDK, as the method parameters will not change when new configuration parameters are added.
Unsupported parameters are ignored. Currently, the SDK recognizes the following parameter:

* `backend` - the URL of the MFA back-end service (Mandatory)

##### `void addCustomHeaders(Map<String, String> headers)`
This method allows the SDK user to set a map of custom headers, which will be added to any HTTP request that the SDK executes.
The `headers` parameter is a map of header names mapped to their respective value.
Subsequent calls of this method will add headers on top of the already added ones.

##### `void clearCustomHeaders()`
This method will clear all the currently set custom headers.

##### `void addTrustedDomain(String domain)`
For better security, the SDK user might want to limit the SDK to make outgoing requests only to URLs that belong to one or more trusted domains.
This method can be used to add such trusted domains, one by one.
When trusted domаins are added, the SDK will verify that any outgoing request is done over the `https` protocol and the host belongs to one of the trusted domains.
If for some reason a request is about to be done to a non-trusted domain, the SDK will return Status `UNTRUSTED_DOMAIN_ERROR`.

##### `void clearTrustedDomains()`
This method will clear all the currently set trusted domains.

##### `void setCid(String cid)`
This method will set a specific _Client/Customer ID_ which the SDK should use when sending requests to the backend.
The MIRACL MFA Platform generates _Client IDs_ (sometimes also referred as _Customer IDs_) for the platform customers.
The customers can see those IDs through the _Platform Portal_.
When customers use the SDK to build their own applications to authenticate users using the Platform, the _Client ID_ has to be provided using this method.

##### `Status testBackend(String server)`
This method will test whether `server` is a valid back-end URL by trying to retrieve Client Settings from it.
If the back-end URL is a valid one, the method will return Status `OK`.

##### `Status setBackend(String server)`
This method will change the currently configured back-end in the SDK.
Initially the back-end might be set through the `init()` method, but then it might be change using this method.
`server` is the new back-end URL that should be used.
If successful, the method will return Status `OK`.

##### `User makeNewUser(String id)`
##### `User makeNewUser(String id, String deviceName)`
This method creates a new `User` object. The User object represents an end-user of the authentication.
The user has its own unique identity, which is passed as the id parameter to this method.
Additionally, an optional `deviceName` might be specified. The _Device Name_ is passed to the RPA, which might store it and use it later to determine which _M-Pin ID_ is associated with this device.
The returned value is a newly created `User` instance. The User class itself looks like this:

```java
public class User implements Closeable {

    public enum State {
        INVALID,
        STARTED_VERIFICATION,
        STARTED_REGISTRATION,
        ACTIVATED,
        REGISTERED,
        BLOCKED
    };

    public String getId() {
        ...
    }

    public State getState() {
        ...
    }

    public VerificationType getVerificationType() {
        ...
    }

    public String getBackend() {
        ...
    }

    public String getCustomerId() {
        ...
    }

    public String getAppId() {
        ...
    }

    public boolean canSign() {
        ...
    }

    @Override
    public String toString() {
        return getId();
    }

    ...
}
```

The newly created user is in the `INVALID` state.

The VerificationType enumeration returned from getVerificationType method looks like:

```java
public enum VerificationType {
    NONE,     // NONE verification type denotes the MPinID is not verified
    EMAIL,    // MAIL verification type denotes the MPinID is verified through email
    REG_CODE, // REG_CODE verification type denotes the MPinID is verified through registration code
    DVS,      // DVS verification type denotes the MPinID is DVS MPinID and verification was done on the auth MPinID
    PLUGGABLE // PLUGGABLE verification type denotes custom process of verifying MPinID
};
```

##### `boolean isUserExisting(String id)`
##### `boolean isUserExisting(String id, String customerId)`
This method will return `true` if there is a user with the given properties.
If no such user is found, the method will return `false`.

In the MIRACL MFA Platform end-users are registered for a given Customer.
Therefor, same identity might be registered for two different customers, and two different User objects will be present for the two different customers, but with the same `id`.
When checking whether the user exists, one should specify also the `customerId`

##### `void deleteUser(User user)`
This method deletes a user from the users list that the SDK maintains.
All the user data including its _M-Pin ID_, its state and _M-Pin Token_ will be deleted.
A new user with the same identity can be created later with the `makeNewUser()` method.

##### `Status listUsers(List<User> users)`
This method will populate the provided `users` vector with ALL the users known to the SDK.
After the list is returned to the caller, the users might be filtered out using their properties `User.getBackend()`, `User.getCustomerId()` and `User.getAppId()`.

##### `Status getServiceDetails(String serviceUrl, ServiceDetails serviceDetails)`
This method is provided for applications working with the _MIRACL MFA Platform_.
After scanning a QR Code from the platform login page, the app should extract the URL from it and call this method to retrieve the service details.
The service details include the _backend URL_ which needs to be set back to the SDK in order connect it to the platform.
This method could be called even before the SDK has been initialized, or alternatively the SDK could be initialized without setting a backend, and `setBackend()` could be used after the backend URL has been retrieved through this method.
The returned `ServiceDetails` look as follows:
```java
public class ServiceDetails {
    public String name;
    public String backendUrl;
    public String logoUrl;
}
 ```
* `name` is the service readable name
* `backendUrl` is the URL of the service backend. This URL has to be set either via the SDK `init()` method or using  `SetBackend()`
* `logoUrl` is the URL of the service logo. The logo is a UI element that could be used by the app.

##### `Status getSessionDetails(String accessCode, SessionDetails sessionDetails)`
This method could be optionally used to retrieve details regarding a browser session when the SDK is used to authenticate users to an online service, such as the _MIRACL MFA Platform_.
In this case an `accessCode` is transferred to the mobile device out-of-band e.g. via scanning a graphical code. The code is then provided to this method to get the session details.
This method will also notify the backend that the `accessCode` was retrieved from the browser session.
The returned `SessionDetails` look as follows:
```java
public class SessionDetails {
    public String prerollId;
    public String appName;
    public String appIconUrl;
}
```
During the online browser session an optional user identity might be provided meaning that this is the user that wants to register/authenticate to the online service.
* The `prerollId` will carry that user ID, or it will be empty if no such ID was provided.
* `appName` is the name of the web application to which the service will authenticate the user.
* `appIconUrl` is the URL from which the icon for web application could be downloaded.

##### `Status abortSession(String accessCode)`
This method should be used to inform the Platform that the current authentication/registration session has been aborted.
A session starts with obtaining the _Access Code_, usually after scanning and decoding a graphical image, such as QR Code.
Then the mobile client might retrieve the session details using `getSessionDetails()`, after which it can either start registering a new end-user or start authentication.
This process might be interrupted by either the end-user disagreeing on the consent page, or by just hitting a Back button on the device, or by even closing the app.
For all those cases, it is recommended to use `abortSession()` to inform the Platform.

##### `Status getAccessCode(String authzUrl, StringBuilder accessCode)`
This method should be used when the mobile app needs to login an end-user into the app itself.
In this case there's no browser session involved and the Access Code cannot be obtained by scanning an image from the browser.
Instead, the mobile app should initially get from its backend an _Authorization URL_. This URL could be formed at the app backend using one of the _MFA Platform SDK_ flavors.
When the mobile app has the Authorization URL, it can pass it to this method as `authzUrl`, and get back an `accessCode` that can be further used to register or authenticate end-users.
Note that the Authorization URL contains a parameter that identifies the app.
This parameter is validated by the Platform and it should correspond to the Customer ID, set via `setCid()`.

##### `Status startVerification(User user, String clientId, String redirectUri, String accessCode)`
This method initializes the default user identity verification process. The verification confirms that the user identity is owned by the user.
The default user identity verification in the _MIRACL MFA Platform_ sends an email message that contains a confirmation URL. When clicked it opens the authentication application (deep linking needs to be configured in the application) and `finishVerification()` should be called to finalize the verification. Note that the identity is created on the device where the email URL is opened.

The SDK sends the necessary requests to the back-end service.
The State of the User instance will change to `STARTED_VERIFICATION`.
The status will indicate whether the operation is successful or not.

##### `Status finishVerification(User user, String verificationCode, VerificationResult verificationResult)`
This method is used to finalize the process of the default user identity verification.
The `verificationCode` has to be obtained from the verification URL received in the confirmation email as a query parameter.

The `VerificationResult` class returned as a reference variable has the following form:

```
public class VerificationResult {
    public String accessCode;
    public String activationToken;
}
```

The `accessCode` is a session identifier which you could control the session with by `abortSession()`, `getSessionDetails()`.
The `activationToken` is a code which indicates that the user identity is already verified and is used to start the identity registration.

##### `Status startRegistration(User user, String accessCode)`
##### `Status startRegistration(User user, String accessCode, String pushToken)`
##### `Status startRegistration(User user, String accessCode, String pushToken, String regCode)`
This method initializes the registration for a User that has already been verified.
The SDK starts the Setup flow, sending the necessary requests to the back-end service.
The State of the User instance will change to `STARTED_REGISTRATION`.
The status will indicate whether the operation was successful or not.
During this call, an _M-Pin ID_ for the end-user will be issued by the Platform and stored within the user object.
The Platform will also start a user identity verification procedure, by sending a verification e-mail.

The `accessCode` should be obtained from a browser session, and session details are retrieved before starting the registration.
This way the mobile app can show to the end-user the respective details for the customer, which the identity is going to be associated to.

Optionally, the application might pass additional `pushToken`, which is a unique token for sending _Push Notifications_ to the mobile app.
When such token is provided, the Platform might use additional verification step by sending a Push Notification to the app.

The `regCode` is a code which value indicates that the user identity is already verified. It could be obtained as `activationToken` value of `VerificationResult` object from a successfull call to `finishVerification()` method using the default identity verification or using a bootstrap code.

##### `Status restartRegistration(User user)`
This method re-initializes the registration process for a user, where registration has already started.
The difference between this method and `startRegistration()` is that it will not generate a new _M-Pin ID_, but will use the one that was already generated.
Besides that, the methods follow the same procedures, such as getting the RPA to re-start the user identity verification procedure of sending a verification email to the user.

The RPA might decide to verify the identity without starting a verification process. In this case, the Status of the call will still be `OK`, but the User State will be set to `ACTIVATED`.


##### `Status confirmRegistration(User user)`
This method allows the application to check whether the user identity verification process has been finalized or not.
The provided `user` object is expected to be either in the `STARTED_REGISTRATION` state or in the `ACTIVATED` state.
The latter is possible if the RPA activated the user immediately with the call to `startRegistration()` and no verification process was started.
During the call to `confirmRegistration()` the SDK will make an attempt to retrieve _Client Key_ for the user.
This attempt will succeed if the user has already been verified/activated but will fail otherwise.
The method will return Status `OK` if the Client Key has been successfully retrieved and `IDENTITY_NOT_VERIFIED` if the identity has not been verified yet.
If the method has succeeded, the application is expected to get the desired PIN/secret from the end-user and then call `finishRegistration()`, and provide the PIN.

##### `Status finishRegistration(User user, String secret)`
##### `Status finishRegistration(User user, String[] multiFactor)`
The first form of this method finalizes the user registration process.
It extracts the _M-Pin Token_ from the _Client Key_ for the provided `secret`, and then stores the token in the secure storage.
On successful completion, the User state will be set to `REGISTERED` and the method will return Status `OK`.

The second form of the method allows passing more authentication factors to the SDK, as an array of `String`s.
Passing a single-item `multiFactor` array would be equivalent to calling the first form of the method.

##### `Status startAuthentication(User user, String accessCode)`
This method starts the authentication process for a given `user`.
It attempts to retrieve the _Time Permits_ for the user, and if successful, will return Status `OK`.
If they cannot be retrieved, the method will return Status `REVOKED`.
If this method is successfully completed, the app should read the PIN/secret from the end-user and call one of the `finishAuthentication()` variants to authenticate the user.

Optionally, an `accessCode` could be provided. This code is retrieved out-of-band from a browser session when the user has to be authenticated to an online service, such as the _MIRACL MFA Platform_.
When this code is provided, the SDK will notify the service that authentication associated with the given `accessCode` has started for the provided user.

##### `Status startAuthenticationOtp(User user)`
This method will start the authentication for OTP generation.
It resembles the `startAuthentication()` method, but the difference is that in this case no `accessCode` is required.
OTP generation is not tied to a specific Customer Application session.

##### `Status startAuthenticationRegCode(User user)`
This method will start the authentication for _Registration Code_ generation.
It resembles the `startAuthentication()` method, but the difference is that in this case no `accessCode` is required.
Registration Code generation is not tied to a specific Customer Application session.

##### `Status finishAuthentication(User user, String secret, String accessCode)`
##### `Status finishAuthentication(User user, String[] multiFactor, String accessCode)`
This method authenticates the end-user for logging into a Web App in a browser session.
The `user` to be authenticated is passed as a parameter, along with his/her `secret`.
The `accessCode` associates the authentication with the browser session from which it was obtained.

The second form of the method allows passing more authentication factors to the SDK, as an array of `String`s.
Passing a single-item `multiFactor` array would be equivalent to calling the first form of the method.

The returned status might be:
* `OK` - Successful authentication.
* `INCORRECT_PIN` - The authentication failed because of incorrect PIN/secret.
After the 3rd unsuccessful authentication attempt, the method will still return `INCORRECT_PIN` but the User State will be set to `BLOCKED`.

##### `Status finishAuthentication(User user, String secret, String accessCode, StringBuilder authzCode)`
##### `Status finishAuthentication(User user, String[] multiFactor, String accessCode, StringBuilder authCode)`
This method authenticates an end-user in a way that allows the mobile app to log the user into the app itself after verifying the authentication against its own backend.
When using this flow, the mobile app would first retrieve the `accessCode` using the `getAccessCode()` method,
and when authentication the user it will receive an _Authorization Code_, `authzCode`.
Using this Authorization Code, the mobile app can make a request to its own backend, so the backend can validate it using one of the _MFA Platform SDK_ flavors,
and create a session token.
This token could be used further as an authentication element in the communication between the app and its backend.

The second form of the method allows passing more authentication factors to the SDK, as an array of `String`s.
Passing a single-item `multiFactor` array would be equivalent to calling the first form of the method.

##### `Status finishAuthenticationOtp(User user, String singleFactor, OTP otp)`
##### `Status finishAuthenticationOtp(User user, String[] multiFactor, OTP otp)`
This method performs end-user authentication for OTP generation.
The authentication process is similar to `finishAuthentication()`, but as a result the MFA Platform issues an OTP instead of logging the user into an application.
The returned status is analogical to the `finishAuthentication()` method, but in addition to that, an `OTP` object is returned.
The `OTP` class looks like this:
```java
public class OTP {
    public String otp;
    public long expireTime;
    public int ttlSeconds;
    public long nowTime;
    public Status status;
}
```
* The `otp` string is the issued OTP.
* The `expireTime` is the MIRACL MFA system time when the OTP will expire.
* The `ttlSeconds` is the expiration period in seconds.
* The `nowTime` is the current MIRACL MFA system time.
* `status` is the status of the OTP generation. The status will be `OK` if the OTP was successfully generated, or `FLOW_ERROR` if not.

The second form of the method allows passing more authentication factors to the SDK, as an array of `String`s.
Passing a single-item `multiFactor` array would be equivalent to calling the first form of the method.

##### `Status finishAuthenticationRegCode(User user, String[] multiFactor, RegCode regCode)`
This method performs end-user authentication for _Registration Code_ generation.
The authentication process is similar to `finishAuthentication()`, but as a result the MFA Platform issues a Registration Code instead of logging the user into an application.
The returned status is analogical to the `finishAuthentication()` method, but in addition to that, an `RegCode` object is returned.
The `RegCode` class is basically identical to the `OTP` class, and looks like this:
```java
public class RegCode {
    public String otp;
    public long expireTime;
    public int ttlSeconds;
    public long nowTime;
    public Status status;
}
```
* The `otp` string is the issued Registration Code, which is a one-time code in its nature.
* The `expireTime` is the MIRACL MFA system time when the code will expire.
* The `ttlSeconds` is the expiration period in seconds.
* The `nowTime` is the current MIRACL MFA system time.
* `status` is the status of the OTP generation. The status will be `OK` if the OTP was successfully generated, or `FLOW_ERROR` if not.

The method receives an array of authentication factors, `multiFactor`, and if only a single secret needs to be passed, then `multiFactor` should be an array of a single item.

##### `Status startRegistrationDvs(User user, String[] multiFactor)`
This method starts the user registration for the _DVS (Designated Verifier Signature)_ functionality.

The DVS functionality allows a customer application to verify signatures of documents/transactions, signed by the end-user.

It is a separate process than the registration for authentication, while a user should be authenticated in order to register for DVS.
This separate process allows users to register for DVS only if they want/need to, and also to select a different PIN/secret for signing documents.

The method receives an array of authentication identity factors, `multiFactor`, and if only a single secret needs to be passed, then `multiFactor` should be an array of a single item.

##### `Status finishRegistrationDvs(User user, String[] multiFactor)`
This method finalizes the user registration process for the DVS functionality.
Before calling it the application has to get from the end-user the authentication factors that need to be specified while signing (like PIN and possibly others).

The method receives an array of authentication factors, `multiFactor`, and if only a single secret needs to be passed, then `multiFactor` should be an array of a single item.

##### `boolean verifyDocumentHash(byte[] document, byte[] documentHash)`
This method relates to the _DVS (Designated Verifier Signature)_ functionality of the MFA Platform.

It verifies that the `hash` value is correct for the given `document`.
The method returns `true` or `false` respectively, if the hash is correct or not.

The DVS functionality allows a customer application to verify signatures of documents/transactions, signed by the end-user.
The `document` is any free form text that needs to be signed.
Typically, the customer application will generate a `hash` value for the document that needs to be signed, and will send it to the mobile client app.
The client app can then verify the correctness of the hash value using this method.

##### `Status sign(User user, byte[] documentHash, String secret, int epochTime, String authzToken, Signature signature)`
##### `Status sign(User user, byte[] documentHash, String[] multiFactor, int epochTime, String authzToken, Signature signature)`
This method relates to the _DVS (Designated Verifier Signature)_ functionality of the MFA Platform.

It signs a given `documentHash` for the provided `user`.
The `user` should have the ability to sign documents, i.e. it has to have possession of a signing client key and a public/private key-pair.
Those are issued for the user during registration, but users that have registered prior to the DVS feature availability, might lack those keys.
To check whether a user has signing capability, use the method `User.canSign()`.
The end-user `secret` or authentication factor/s should be provided as well, since signing a document (its hash, in fact) is very similar to authenticating.

The second form of the method allows passing more authentication factors to the SDK, as an array of `String`s.
Passing a single-item `multiFactor` array would be equivalent to calling the first form of the method.

`epochTime` is the time, in Epoch format, for the document/transaction signature.
Both the `documentHash` and the `epochTime` should be generated and provided by the customer application back-end.

`authzToken` is a token that the SDK needs in order to be able to generate the signature and verify against the platform the correctness of the provided authentication factors.
This token should also be generated by the application back-end, using one of the MFA Backend SDK variants.
Generally, the token has the format:
```
"MAAS-HMAC-SHA256  <token>"
```
as `<token>` is Base64-encoded `<client-id>:<hmac-sha256-signature>`.

`<hmac-sha256-signature>` is an _HMAC-SHA256_ signature of the hex-encoded document hash, using the _Client Secret_ as a _key_. _Client ID_ and _Client Secret_ are issued by the Platform when creating an application.

The generated signature is returned in the `signature` parameter.
The `Signature` class has the following form:
```java
public class Signature {
    public byte[] hash;
    public byte[] mpinId;
    public byte[] u;
    public byte[] v;
    public byte[] publicKey;
}
```
* `hash` is the document hash. It should be identical to the provided `documentHash`.
* `mpinId` is the end-user's _M-Pin ID_.
* `u` and `v` are the actual values that represent the signature.
* `publicKey` is the _Public Key_ associated with the end-user.
All of those parameters should be sent to the customer application back-end, so it can verify the signature.

The returned `Status` could be one of:
* `OK` - document hash was successfully signed for the provided `user` and `secret`.
* `INCORRECT_PIN` - The method failed due to incorrect `secret`. If this status is returned, the `user` State might be changed to `BLOCKED` in case several consecutive unsuccessful attempts were performed.
* `FLOW_ERROR` - The provided `user` doesn't have the ability to sign documents.
* `CRYPTO_ERROR` - an error has occurred at the crypto layer of the SDK. Call the status's `getErrorMessage()` method for more info.