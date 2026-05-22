cmake_minimum_required(VERSION 3.18.1)

project(tomcrypt C)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_definitions(-DLTC_SOURCE)
add_definitions(-DLTC_NO_ASM)
add_definitions(-DLTC_NO_TEST)
add_definitions(-DLTC_NO_PROTOTYPES)

# Collect source files using GLOB
file(GLOB_RECURSE TOMCRYPT_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/src/ciphers/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/hashes/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/mac/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/modes/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/misc/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/math/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/pk/asn1/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/pk/pkcs1/*.c"
)

# Exclude PRNG and encauth (GCM/CCM/EAX/OCB/CHACHA20POLY1305)
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/prng/")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/encauth/")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/chachapoly/")
# Exclude conflicting symbols with OpenSSL
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "sha512_224.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "sha512_256.c")
# Exclude conflicting symbols with GmSSL (sha1, sha224, sha256, sha384, sha512, hmac)
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/sha2/sha1.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/sha2/sha224.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/sha2/sha256.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/sha2/sha384.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/sha2/sha512.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "/hmac/")
# Exclude conflicting symbols with libsodium
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "blake2b.c")
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "blake2s.c")
# Exclude register_all that references excluded hashes
list(FILTER TOMCRYPT_SOURCES EXCLUDE REGEX "crypt_register_all_hashes.c")

add_library(tomcrypt STATIC ${TOMCRYPT_SOURCES})

target_include_directories(tomcrypt
    PUBLIC
        "${CMAKE_CURRENT_SOURCE_DIR}/src/headers"
)
