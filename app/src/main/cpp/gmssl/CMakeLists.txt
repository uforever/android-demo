cmake_minimum_required(VERSION 3.18.1)

project(gmssl C)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_library(gmssl STATIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/version.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/debug.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm4_common.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm4_enc.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm4_modes.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm4_setkey.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm3.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm3_hmac.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm3_kdf.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm2_alg.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm2_key.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm2_lib.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm9_alg.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm9_key.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sm9_lib.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/aes.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/aes_modes.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/block_cipher.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/digest.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sha1.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sha256.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/sha512.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/md5.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/hmac.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/gcm.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/gf128.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/aead.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/asn1.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/hex.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/base64.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/pem.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/pkcs8.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/ec.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/x509_alg.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/x509_cer.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/x509_ext.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/x509_new.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/x509_req.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/x509_crl.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/pbkdf2.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/hkdf.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/hash_drbg.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/rand.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/rand_unix.c
)

target_include_directories(gmssl
    PUBLIC
        "${CMAKE_CURRENT_SOURCE_DIR}/include"
)
