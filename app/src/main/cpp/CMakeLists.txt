cmake_minimum_required(VERSION 3.22.1)

project("demo")

# Add native crypto libraries
add_subdirectory(libtomcrypt)
add_subdirectory(libsodium)
add_subdirectory(gmssl)

add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    native-http.cpp
    native-crypto.cpp
    native-crypto-tomcrypt.cpp
    native-crypto-sodium.cpp
    native-crypto-gmssl.cpp
)

# OLLVM 混淆编译选项（仅对 native-*.cpp 源文件生效）
set(OLLVM "-mllvm -irobf-indbr -mllvm -irobf-icall -mllvm -irobf-indgv -mllvm -irobf-cse -mllvm -irobf-fla -mllvm -irobf-cie -mllvm -irobf-cfe")
set_source_files_properties(
    native-lib.cpp
    native-http.cpp
    native-crypto.cpp
    native-crypto-tomcrypt.cpp
    native-crypto-sodium.cpp
    native-crypto-gmssl.cpp
    PROPERTIES COMPILE_FLAGS "${OLLVM}"
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
    tomcrypt
    sodium
    gmssl
)
