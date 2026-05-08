cmake_minimum_required(VERSION 3.22.1)

project("demo")

add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    native-http.cpp
)

if (ANDROID_ABI STREQUAL "armeabi-v7a")
    set(LIBCURL_DIR "${CMAKE_SOURCE_DIR}/libcurl/armeabi-v7a")
elseif (ANDROID_ABI STREQUAL "arm64-v8a")
    set(LIBCURL_DIR "${CMAKE_SOURCE_DIR}/libcurl/arm64-v8a")
elseif (ANDROID_ABI STREQUAL "x86_64")
    set(LIBCURL_DIR "${CMAKE_SOURCE_DIR}/libcurl/x86_64")
else ()
    message(FATAL_ERROR "Unsupported ABI: ${ANDROID_ABI}")
endif ()

include_directories(${LIBCURL_DIR}/include)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    c++_static
    log
    ${LIBCURL_DIR}/lib/libcurl.a
    ${LIBCURL_DIR}/lib/libssl.a
    ${LIBCURL_DIR}/lib/libcrypto.a
    ${LIBCURL_DIR}/lib/libz.a
)
