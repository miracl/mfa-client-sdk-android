# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.
cmake_minimum_required(VERSION 3.4.1)

set(JNI_DIR ${CMAKE_SOURCE_DIR}/src/main/jni)
set(CORE_DIR ${CMAKE_SOURCE_DIR}/../mpin-sdk-core)
set(CORE_CRYPTO_DIR ${CMAKE_SOURCE_DIR}/../mpin-sdk-core/ext/milagro-crypto-c)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(
    ${CORE_CRYPTO_DIR}/include
    ${CORE_DIR}/src
    ${CORE_DIR}/ext/cvshared/cpp/include
)

add_library(AndroidMpinSDK SHARED
    ${CORE_DIR}/ext/cvshared/cpp/CvString.cpp
    ${CORE_DIR}/ext/cvshared/cpp/CvXcode.cpp
    ${CORE_CRYPTO_DIR}/src/aes.c
    ${CORE_CRYPTO_DIR}/src/amcl++.cpp
    ${CORE_CRYPTO_DIR}/src/big_256_28.c
    ${CORE_CRYPTO_DIR}/src/ecp2_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/ecp_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/fp12_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/fp2_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/fp4_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/fp_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/gcm.c
    ${CORE_CRYPTO_DIR}/src/hash.c
    ${CORE_CRYPTO_DIR}/src/mpin_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/oct.c
    ${CORE_CRYPTO_DIR}/src/pair_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/pbc_support.c
    ${CORE_CRYPTO_DIR}/src/rand.c
    ${CORE_CRYPTO_DIR}/src/randapi.c
    ${CORE_CRYPTO_DIR}/src/rom_curve_BN254CX.c
    ${CORE_CRYPTO_DIR}/src/rom_field_BN254CX.c
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