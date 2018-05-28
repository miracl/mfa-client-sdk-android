# Milagro Mobile SDK for Android

## Building the Milagro Mobile SDK for Android

### Prerequisites

1. Download and install Android Studio or higher with Android SDK 16 or higher
1. Download or Clone the project and its submodule to \<milagro-sdk-android\>

### Building the Milagro Mobile SDK

#### From Android Studio
1. Import the project - File-> Open -> \<milagro-sdk-android\>
1. From Gradle Tool View select :mpinsdk -> Tasks -> build -> build
1. The assembled aars will be located in \<milagro-sdk-android\>/mpinsdk/build/outputs/aar
 
#### From Command Line
1. Navigate to \<milagro-sdk-android\>
1. Execute ./gradlew build
1. The assembled aars will be located in \<milagro-sdk-android\>/mpinsdk/build/outputs/aar

For further details, see [Milagro Mobile SDK for Android Documentation](http://docs.milagro.io/en/mfa/mobile-sdk-android/milagro-mfa-mobile-sdk-developer-guide.html)

## Android SDK API for Milagro (`com.miracl.mpinsdk.MPinSDK`)

The Android SDK API is used by Android application developers for integrating with the Milagro Mobile SDK.
The API resembles the SDK Core layer, but it exposes to the Application layer, only those methods that the application needs.
Most of the methods return a `Status` object which is defined as follows:

```java
public class Status {

    public enum Code {
        OK,
        CANCELED_BY_USER,        // Local error, returned when user cancels pin entering
        CRYPTO_ERROR,            // Local error in crypto functions
        STORAGE_ERROR,           // Local storage related error
        NETWORK_ERROR,           // Local error - cannot connect to remote server (no internet, or invalid server/port)
        RESPONSE_PARSE_ERROR,    // Local error - cannot parse json response from remote server (invalid json or unexpected json structure)
        FLOW_ERROR,              // Local error - improper MPinSDK class usage
        IDENTITY_NOT_AUTHORIZED, // Remote error - the remote server refuses user registration
        IDENTITY_NOT_VERIFIED,   // Remote error - the remote server refuses user registration because identity is not verified
        REQUEST_EXPIRED,         // Remote error - the register/authentication request expired
        REVOKED,                 // Remote error - cannot get time permit (probably the user is temporary suspended)
        INCORRECT_PIN,           // Remote error - user entered wrong pin
        INCORRECT_ACCESS_NUMBER, // Remote/local error - wrong access number (checksum failed or RPS returned 412)
        HTTP_SERVER_ERROR,       // Remote error, that was not reduced to one of the above - the remote server returned internal server error status (5xx)
        HTTP_REQUEST_ERROR,      // Remote error, that was not reduced to one of the above - invalid data sent to server, the remote server returned 4xx error status
        BAD_USER_AGENT,          // Remote error - user agent not supported
        CLIENT_SECRET_EXPIRED    // Remote error - re-registration required because server master secret expired
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

##### `MPinSDK()`
This method constructs an SDK instance.

##### `Status init(Map<String, String> config, Context context)`
This method initializes the SDK. It receives a key/value map of the configuration parameters.
The configuration is a key-value map into which different configuration options can be inserted. This is a flexible way of passing configurations to the SDK, as the method parameters will not change when new configuration parameters are added. 
Unsupported parameters are ignored. Currently, the SDK recognizes the following parameters:

* `backend` - the URL of the Milagro MFA back-end service (Mandatory)
* `rpsPrefix` - the prefix that should be added for requests to the RPS (Optional). The default value is `"rps"`.

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

##### `Status testBackend(String server)`
##### `Status testBackend(String server, String rpsPrefix)`
This method will test whether `server` is a valid back-end URL by trying to retrieve Client Settings from it.
Optionally, a custom RPS prefix might be specified if it was customized at the back-end and is different than the default `"rps"`.
If the back-end URL is a valid one, the method will return Status `OK`.

##### `Status setBackend(String server)`
##### `Status setBackend(String server, String rpsPrefix)`
This method will change the currently configured back-end in the SDK.
Initially the back-end might be set through the `init()` method, but then it might be change using this method.
`server` is the new back-end URL that should be used.
Optionally, a custom RPS prefix might be specified if it was customized at the back-end and is different than the default `"rps"`.
If successful, the method will return Status `OK`.

##### `User makeNewUser(String id)`
##### `User makeNewUser(String id, String deviceName)`
This method creates a new `User` object. The User object represents an end-user of the Milagro authentication.
The user has its own unique identity, which is passed as the id parameter to this method.
Additionally, an optional `deviceName` might be specified. The _Device Name_ is passed to the RPA, which might store it and use it later to determine which _M-Pin ID_ is associated with this device.
The returned value is a newly created `User` instance. The User class itself looks like this:

```java
public class User implements Closeable {
 
    public enum State {
        INVALID,
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

##### `boolean isUserExisting(String id)`
This method will return `true` if there is a user with the given identity, associated with the currently set backend.
If no such user is found, the method will return `false`.

##### `void deleteUser(User user)`
This method deletes a user from the users list that the SDK maintains.
All the user data including its _M-Pin ID_, its state and _M-Pin Token_ will be deleted.
A new user with the same identity can be created later with the `makeNewUser()` method.

##### `Status listUsers(List<User> users)`
This method populates the provided list with all the users that are associated with the currently set backend.
Different users might be in different states, reflecting their registration status.
The method will return Status `OK` on success and `FLOW_ERROR` if no backend is set through the `init()` or `setBackend()` methods.

##### `Status listUsers(List<User> users, String backend)`
This method populates the provided list with all the users that are associated with the provided `backend`.
Different users might be in different states, reflecting their registration status.
The method will return Status `OK` on success and `FLOW_ERROR` if the SDK was not initialized.

##### `Status listAllUsers(List<User> users)`
This method populates the provided list with all the users associated with all the backends know to the SDK.
Different users might be in different states, reflecting their registration status.
The user association to a backend could be retrieved through the `User.getBackend()` method.
The method will return Status `OK` on success and `FLOW_ERROR` if the SDK was not initialized.

##### `Status listBackends(List<String> backends)`
This method will populate the provided list with all the backends known to the SDK.
The method will return Status `OK` on success and `FLOW_ERROR` if the SDK was not initialized.


##### `Status startRegistration(User user)`
##### `Status startRegistration(User user, String activateCode)`
##### `Status startRegistration(User user, String activateCode, String userData)`
This method initializes the registration for a User that has already been created. The SDK starts the Milagro Setup flow, sending the necessary requests to the back-end service.
The State of the User instance will change to `STARTED_REGISTRATION`. The status will indicate whether the operation was successful or not.
During this call, an _M-Pin ID_ for the end-user will be issued by the RPS and stored within the user object.
The RPA could also start a user identity verification procedure, by sending a verification e-mail.

The optional `activateCode` parameter might be provided if the registration process requires such.
In cases when the user verification is done through a _One-Time-Code_ (OTC) or through an SMS that carries such code, this OTC should be passed as the `activateCode` parameter.
In those cases, the identity verification should be completed instantly and the User State will be set to `ACTIVATED`.
 
Optionally, the application might pass additional `userData` which might help the RPA to verify the user identity.
The RPA might decide to verify the identity without starting a verification process. In this case, the Status of the call will still be `OK`, but the User State will be set to `ACTIVATED`.

##### `Status restartRegistration(User user)`
##### `Status restartRegistration(User user, String userData)`
This method re-initializes the registration process for a user, where registration has already started.
The difference between this method and `startRegistration()` is that it will not generate a new _M-Pin ID_, but will use the one that was already generated.
Besides that, the methods follow the same procedures, such as getting the RPA to re-start the user identity verification procedure of sending a verification email to the user.

The application could also pass additional `userData` to help the RPA to verify the user identity.
The RPA might decide to verify the identity without starting a verification process. In this case, the Status of the call will still be `OK`, but the User State will be set to `ACTIVATED`.

##### `Status confirmRegistration(User user)`
##### `Status confirmRegistration(User user, String pushToken)`
This method allows the application to check whether the user identity verification process has been finalized or not.
The provided `user` object is expected to be either in the `STARTED_REGISTRATION` state or in the `ACTIVATED` state.
The latter is possible if the RPA activated the user immediately with the call to `startRegistration()` and no verification process was started.
During the call to `confirmRegistration()` the SDK will make an attempt to retrieve _Client Key_ for the user.
This attempt will succeed if the user has already been verified/activated but will fail otherwise.
The method will return Status `OK` if the Client Key has been successfully retrieved and `IDENTITY_NOT_VERIFIED` if the identity has not been verified yet.
If the method has succeeded, the application is expected to get the desired PIN/secret from the end-user and then call `finishRegistration()`, and provide the PIN.

**Note** Using the optional parameter `pushToken`, the application can provide a platform specific token for sending _Push Messages_ to the device.
Such push messages might be utilized as an alternative to the _Access Number_, as part of the authentication flow.

##### `Status finishRegistration(User user, String secret)`
This method finalizes the user registration process.
It extracts the _M-Pin Token_ from the _Client Key_ for the provided `secret`, and then stores the token in the secure storage.
On successful completion, the User state will be set to `REGISTERED` and the method will return Status `OK`.

##### `Status startAuthentication(User user)`
This method starts the authentication process for a given `user`.
It attempts to retrieve the _Time Permits_ for the user, and if successful, will return Status `OK`.
If they cannot be retrieved, the method will return Status `REVOKED`.
If this method is successfully completed, the app should read the PIN/secret from the end-user and call one of the `finishAuthentication()` variants to authenticate the user.

##### `Status checkAccessNumber(String accessNumber)`
This method is used only when a user needs to be authenticated to a remote (browser) session, using _Access Number_.
The access numbers might have a check-sum digit in them and this check-sum needs to be verified on the client side, in order to prevent calling the back-end with non-compliant access numbers.
The method will return Status `OK` if successful, and `INCORRECT_ACCESS_NUMBER` if not successful.

##### `Status finishAuthentication(User user, String secret)`
##### `Status finishAuthentication(User user, String secret, StringBuilder authResultData)`
This method performs end-user authentication where the `user` to be authenticated is passed as a parameter, along with his `secret` (secret).
The method performs the authentication against the _Milagro MFA Server_ using the provided `secret` and the stored _M-Pin Token_, and then logs into the RPA.
The RPA responds with the authentication _User Data_ which is returned to the application through the `authResultData` parameter.
If successful, the returned status will be `OK`, and if the authentication fails, the return status would be `INCORRECT_PIN`.
After the 3rd (configurable in the RPS) unsuccessful authentication attempt, the method will return `INCORRECT_PIN` and the User State will be set to `BLOCKED`.

##### `Status finishAuthenticationOTP(User user, String secret, OTP otp)`
This method performs end-user authentication for an OTP. The authentication process is similar to `finishAuthentication()`, but the RPA issues an OTP instead of logging the user into the application.
The returned status is analogical to the `finishAuthentication()` method, but in addition to that, an `OTP` object is returned. The `OTP` class looks like this:
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
* The `expireTime` is the Milagro MFA system time when the OTP will expire.
* The `ttlSeconds` is the expiration period in seconds.
* The `nowTime` is the current Milagro MFA system time.
* `status` is the status of the OTP generation. The status will be `OK` if the OTP was successfully generated, or `FLOW_ERROR` if not.

**NOTE** that OTP might be generated only by RPA that supports that functionality, such as the MIRACL M-Pin SSO. Other RPA's might not support OTP generation where the `status` inside the returned `otp` instance will be `FLOW_ERROR`.

##### `Status finishAuthenticationAN(User user, String secret, String accessNumber)`
This method authenticates the end-user using an _Access Number_, provided by a PC/Browser session.
After this authentication, the end-user can log into the PC/Browser which provided the Access Number, while the authentication itself is done on the Mobile Device.
`accessNumber` is the Access Number from the browser session. The returned status might be:
* `OK` - Successful authentication.
* `INCORRECT_PIN` - The authentication failed because of incorrect PIN/secret. After the 3rd (configurable in the RPS) unsuccessful authentication attempt, the method will still return `INCORRECT_PIN` but the User State will be set to `BLOCKED`.
* `INCORRECT_ACCESS_NUMBER` - The authentication failed because of incorrect Access Number. 

##### `boolean canLogout(User user)`
This method is used after authentication with an Access Number/Code through `finishAuthenticationAN()`.
After such an authentication, the Mobile Device can log out the end-user from the Browser session, if the RPA supports that functionality.
This method checks whether logout information was provided by the RPA and the remote (Browser) session can be terminated from the Mobile Device.
The method will return `true` if the user can be logged-out from the remote session, and `false` otherwise.

##### `boolean logout(User user)`
This method tries to log out the end-user from a remote (Browser) session after a successful authentication through `finishAuthenticationAN()`.
Before calling this method, it is recommended to ensure that logout data was provided by the RPA and that the logout operation can be actually performed.
The method will return `true` if the logged-out request to the RPA was successful, and `false` otherwise.

##### `String getClientParam(String key)`
This method returns the value for a _Client Setting_ with the given key.
The value is returned as a String always, i.e. when a numeric or a boolean value is expected, the conversion should be handled by the application. 
Client settings that might interest the applications are:
* `accessNumberDigits` - The number of Access Number digits that should be entered by the user, prior to calling `finishAuthenticationAN()`.
* `setDeviceName` - Indicator (`true/false`) whether the application should ask the user to insert a _Device Name_ and pass it to the `makeNewUser()` method.
* `appID` - The _App ID_ used by the backend. The App ID is a unique ID assigned to each customer or application. It is a hex-encoded long numeric value. The App ID can be used only for information purposes and it does not affect the application's behavior in any way.

## Android SDK API for MIRACL MFA (`com.miracl.mpinsdk.MPinMFA`)

This flavor of the SDK should be used to build apps that authenticate users against the _MIRACL MFA Platform_.
It massively resembles the _Apache Milagro_ flavor, while incorporating some functionality is specific to the MIRACL Platform.
Similarly to `MPinSDK`, the `MPinMFA` needs to be instantiated and initialized.
Most of the methods return a `Status` object, which is identical to the one used by `MPinSDK`.

The methods that return `Status`, will always return status `OK` if successful.
Many methods expect the provided `User` object to be in a certain state, and if it is not, the method will return status `FLOW_ERROR`

##### `Status init(Map<String, String> config, Context context)`
Identical and analogical to `MPinSDK`'s [`init()`](#status-initmapstring-string-config-context-context)

##### `void addCustomHeaders(Map<String, String> headers)`
Identical and analogical to `MPinSDK`'s [`addCustomHeaders()`](#void-addcustomheadersmapstring-string-headers)

##### `void clearCustomHeaders()`
Identical and analogical to `MPinSDK`'s [`clearCustomHeaders()`](#void-clearcustomheaders)

##### `void addTrustedDomain(String domain)`
Identical and analogical to `MPinSDK`'s [`addTrustedDomain()`](#void-addtrusteddomainstring-domain)

##### `void clearTrustedDomains()`
Identical and analogical to `MPinSDK`'s [`clearTrustedDomains()`](#void-cleartrusteddomains)

##### `void setCid(String cid)`
This method will set a specific _Client/Customer ID_ which the SDK should use when sending requests to the backend.
The MIRACL MFA Platform generates _Client IDs_ (sometimes also referred as _Customer IDs_) for the platform customers.
The customers can see those IDs through the _Platform Portal_.
When customers use the SDK to build their own applications to authenticate users using the Platform, the _Client ID_ has to be provided using this method. 

##### `Status testBackend(String server)`
##### `Status testBackend(String server, String rpsPrefix)`
Identical and analogical to `MPinSDK`'s [`testBackend()`](#status-testbackendstring-server)

##### `Status setBackend(String server)`
##### `Status setBackend(String server, String rpsPrefix)`
Identical and analogical to `MPinSDK`'s [`setBackend()`](#status-setbackendstring-server)

##### `User makeNewUser(String id)`
##### `User makeNewUser(String id, String deviceName)`
Identical and analogical to `MPinSDK`'s [`makeNewUser()`](#user-makenewuserstring-id)

##### `boolean isUserExisting(String id)`
##### `boolean isUserExisting(String id, String customerId)`
This method will return `true` if there is a user with the given properties.
If no such user is found, the method will return `false`.

In the MIRACL MFA Platform end-users are registered for a given Customer.
Therefor, same identity might be registered for two different customers, and two different User objects will be present for the two different customers, but with the same `id`.
When checking whether the user exists, one should specify also the `customerId`

##### `void deleteUser(User user)`
Identical and analogical to `MPinSDK`'s [`deleteUser()`](#void-deleteuseruser-user)

##### `Status listUsers(List<User> users)`
This method will populate the provided `users` vector with ALL the users known to the SDK. This is similar to the `MPinSDK`'s `listAllUsers()`]().
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
    public String rpsPrefix;
    public String logoUrl;
}
 ```
* `name` is the service readable name
* `backendUrl` is the URL of the service backend. This URL has to be set either via the SDK `init()` method or using  `SetBackend()`
* `rpsPrefix` is RPS prefix setting which is also provided together with `backendUrl` while setting a backend
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

##### `Status startRegistration(User user, String accessCode)`
##### `Status startRegistration(User user, String accessCode, String pushToken)`
##### `Status startRegistration(User user, String accessCode, String pushToken, String regCode)`
This method initializes the registration for a User that has already been created.
The SDK starts the Setup flow, sending the necessary requests to the back-end service.
The State of the User instance will change to `STARTED_REGISTRATION`.
The status will indicate whether the operation was successful or not.
During this call, an _M-Pin ID_ for the end-user will be issued by the Platform and stored within the user object.
The Platform will also start a user identity verification procedure, by sending a verification e-mail.

The `accessCode` should be obtained from a browser session, and session details are retrieved before starting the registration.
This way the mobile app can show to the end-user the respective details for the customer, which the identity is going to be associated to.
 
Optionally, the application might pass additional `pushToken`, which is a unique token for sending _Push Notifications_ to the mobile app.
When such token is provided, the Platform might use additional verification step by sending a Push Notification to the app.

Additional optional parameter is the `regCode`.
This is a _Registration Code_ that could be used to bypass the identity verification process.
A valid registration code could be generated by an already registered device, after authenticating the user.
This code could then be provided during the registration process on a device, and the Platform will let the user register, skipping the verification process for that identity.

##### `Status restartRegistration(User user)`
Identical and analogical to `MPinSDK`'s [`restartRegistration()`](#status-restartregistrationuser-user),
without the additional optional parameter that is not used by the MFA Platform.

##### `Status confirmRegistration(User user)`
Identical and analogical to `MPinSDK`'s [`confirmRegistration()`](#status-confirmregistrationuser-user),
without the additional optional parameter that is not used by the MFA Platform.

##### `Status finishRegistration(User user, String secret)`
##### `Status finishRegistration(User user, String[] multiFactor)`
The first form of this method is identical and analogical to `MPinSDK`'s [`finishRegistration()`](#status-finishregistrationuser-user-string-secret)

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
Identical and analogical to `MPinSDK`'s [`finishAuthenticationAN`](#status-finishauthenticationanuser-user-string-secret-string-accessnumber),
while the Access Code is used instead of an Access Number.

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

##### `Status startRegistrationDvs(User user, String token)`
This method starts the user registration for the _DVS (Designated Verifier Signature)_ functionality.

The DVS functionality allows a customer application to verify signatures of documents/transactions, signed by the end-user.

It is a separate process than the registration for authentication, while a user should be authenticated in order to register for DVS.
This separate process allows users to register for DVS only if they want/need to, and also to select a different PIN/secret for signing documents.

The expected `token` is the _Access Token_ issued for the user during the _Open ID Connect Authentication Process_.
This `token` has to be passed from the Relying Party Backend to the Mobile App in a way that is outside the scope of this Mobile SDK.

##### `Status finishRegistrationDvs(User user, String[] multiFactor)`
This method finalizes the user registration process for the DVS functionality.
Before calling it the application has to get from the end-user the authentication factors that need to be specified while signing (like PIN and possibly others).

The method receives an array of authentication factors, `multiFactor`, and if only a single secret needs to be passed, then `multiFactor` should be an array of a single item.

##### `boolean verifyDocumentHash(String document, byte[] hash)`
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

For more information you can refer to the [SDK Core](https://github.com/apache/incubator-milagro-mfa-sdk-core)