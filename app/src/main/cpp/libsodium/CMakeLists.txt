cmake_minimum_required(VERSION 3.18.1)

project(sodium C)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Collect all .c source files from src/libsodium
file(GLOB_RECURSE SODIUM_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_aead/chacha20poly1305/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_aead/xchacha20poly1305/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_auth/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_box/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_core/ed25519/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_core/hchacha20/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_core/hsalsa20/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_core/salsa/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_core/softaes/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_generichash/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_hash/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_kdf/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_kx/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_onetimeauth/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_pwhash/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_scalarmult/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_secretbox/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_secretstream/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_shorthash/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_sign/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_stream/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/crypto_verify/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/randombytes/*.c"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/sodium/*.c"
)

# Exclude x86 SIMD files and hardware-accelerated algorithms
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/sse2/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/ssse3/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/sse41/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/avx/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/aesni/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/armcrypto/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/aegis")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/aes256gcm/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/dolbeau/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/xmm6int/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "/xmm6/")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "crypto_aead/aes")
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "crypto_core/ed25519/ref10/fe_")
# Exclude core.c that references aegis symbols
list(FILTER SODIUM_SOURCES EXCLUDE REGEX "sodium/core.c")

add_library(sodium STATIC ${SODIUM_SOURCES})

target_include_directories(sodium
    PUBLIC
        "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/include"
    PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/src/libsodium/include/sodium"
)

target_compile_definitions(sodium
    PUBLIC
        NATIVE_LITTLE_ENDIAN=1
        HAVE_INLINE_ASM=1
        SODIUM_STATIC=1
        _GNU_SOURCE=1
)
