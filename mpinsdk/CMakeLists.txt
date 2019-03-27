# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.
cmake_minimum_required(VERSION 3.4.1)

set(JNI_DIR ${CMAKE_SOURCE_DIR}/src/main/jni)
set(MPIN_SDK_CORE ${JNI_DIR}/mpin-sdk-core)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(
    ${MPIN_SDK_CORE}/include
    ${MPIN_SDK_CORE}/include/ext/cvshared/cpp/include
    ${MPIN_SDK_CORE}/include/ext/milagro-crypto-c/include
)

add_library(MPinSDKCore STATIC IMPORTED)
set_target_properties(MPinSDKCore PROPERTIES IMPORTED_LOCATION
        ${MPIN_SDK_CORE}/lib/${ANDROID_ABI}/libMpinSDKCore.a)

add_library(AndroidMpinSDK SHARED
    ${JNI_DIR}/JNICommon.cpp
    ${JNI_DIR}/JNIMPinSDK.cpp
    ${JNI_DIR}/JNIMPinMFA.cpp
    ${JNI_DIR}/JNIUser.cpp
    ${JNI_DIR}/HTTPConnector.cpp
    ${JNI_DIR}/Storage.cpp
    ${JNI_DIR}/Context.cpp
)

target_link_libraries(
    AndroidMpinSDK
    MPinSDKCore
    log
)
