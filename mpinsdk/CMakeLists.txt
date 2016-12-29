# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.
cmake_minimum_required(VERSION 3.4.1)

set(JNI_DIR ${CMAKE_SOURCE_DIR}/src/main/jni)
set(CORE_DIR ${CMAKE_SOURCE_DIR}/../mpin-sdk-core)
set(CORE_CRYPTO_DIR ${CMAKE_SOURCE_DIR}/../mpin-sdk-core/src/crypto)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(
    ${CORE_DIR}/src
    ${CORE_DIR}/ext/cvshared/cpp/include
)

add_library(AndroidMpinSDK SHARED
    ${CORE_DIR}/ext/cvshared/cpp/CvString.cpp
    ${CORE_DIR}/ext/cvshared/cpp/CvXcode.cpp
    ${CORE_CRYPTO_DIR}/aes.c
    ${CORE_CRYPTO_DIR}/big.c
    ${CORE_CRYPTO_DIR}/clint.h
    ${CORE_CRYPTO_DIR}/DLLDefines.h
    ${CORE_CRYPTO_DIR}/ecp.c
    ${CORE_CRYPTO_DIR}/ecp2.c
    ${CORE_CRYPTO_DIR}/ff.c
    ${CORE_CRYPTO_DIR}/fp.c
    ${CORE_CRYPTO_DIR}/fp12.c
    ${CORE_CRYPTO_DIR}/fp2.c
    ${CORE_CRYPTO_DIR}/fp4.c
    ${CORE_CRYPTO_DIR}/gcm.c
    ${CORE_CRYPTO_DIR}/hash.c
    ${CORE_CRYPTO_DIR}/mpin.c
    ${CORE_CRYPTO_DIR}/mpin.h
    ${CORE_CRYPTO_DIR}/oct.c
    ${CORE_CRYPTO_DIR}/pair.c
    ${CORE_CRYPTO_DIR}/platform.h
    ${CORE_CRYPTO_DIR}/rand.c
    ${CORE_CRYPTO_DIR}/rom.c
    ${CORE_CRYPTO_DIR}/version.c
    ${CORE_CRYPTO_DIR}/version.h
    ${CORE_DIR}/src/mfa_sdk.cpp
    ${CORE_DIR}/src/mpin_sdk.cpp
    ${CORE_DIR}/src/mpin_sdk_base.cpp
    ${CORE_DIR}/src/utils.cpp
    ${CORE_DIR}/src/mpin_crypto_non_tee.cpp
    ${JNI_DIR}/JNICommon.cpp
    ${JNI_DIR}/JNIMPinSDK.cpp
    ${JNI_DIR}/JNIMPinMFA.cpp
    ${JNI_DIR}/JNIUser.cpp
    ${JNI_DIR}/HTTPConnector.cpp
    ${JNI_DIR}/Storage.cpp
    ${JNI_DIR}/Context.cpp
)

target_link_libraries(AndroidMpinSDK log)