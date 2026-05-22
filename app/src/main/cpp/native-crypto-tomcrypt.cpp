#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include <ctype.h>
#include <tomcrypt.h>

#define LOG_TAG "NativeCrypto-TomCrypt"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static void ensure_ltc_registered() {
    static bool registered = false;
    if (!registered) {
        register_all_ciphers();
        // Manually register only non-conflicting hashes
        // SHA1/SHA256/SHA384/SHA512/HMAC are provided by GmSSL to avoid symbol conflicts
        register_hash(&md5_desc);
        registered = true;
    }
}

static jstring bytesToHex(JNIEnv *env, const unsigned char *data, int len) {
    const char hex_chars[] = "0123456789abcdef";
    char *hex = (char *)malloc(len * 2 + 1);
    for (int i = 0; i < len; i++) {
        hex[i * 2] = hex_chars[(data[i] >> 4) & 0x0f];
        hex[i * 2 + 1] = hex_chars[data[i] & 0x0f];
    }
    hex[len * 2] = '\0';
    jstring result = env->NewStringUTF(hex);
    free(hex);
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hashLibtomcrypt(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data) {
    ensure_ltc_registered();

    const char *alg = env->GetStringUTFChars(algorithm, nullptr);
    int hash_idx = -1;

    if (strcmp(alg, "SHA-256") == 0 || strcmp(alg, "SHA256") == 0) {
        hash_idx = find_hash("sha256");
    } else if (strcmp(alg, "SHA-512") == 0 || strcmp(alg, "SHA512") == 0) {
        hash_idx = find_hash("sha512");
    } else if (strcmp(alg, "MD5") == 0) {
        hash_idx = find_hash("md5");
    } else if (strcmp(alg, "SHA-1") == 0 || strcmp(alg, "SHA1") == 0) {
        hash_idx = find_hash("sha1");
    }

    env->ReleaseStringUTFChars(algorithm, alg);

    if (hash_idx == -1) {
        LOGE("hashLibtomcrypt: unsupported algorithm");
        return env->NewStringUTF("");
    }

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);

    unsigned char digest[MAXBLOCKSIZE];
    unsigned long digest_len = sizeof(digest);

    int err = hash_memory(hash_idx,
                          reinterpret_cast<const unsigned char *>(data_bytes),
                          data_len, digest, &digest_len);
    if (err != CRYPT_OK) {
        LOGE("hashLibtomcrypt: hash_memory failed: %s", error_to_string(err));
        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        return env->NewStringUTF("");
    }

    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);

    return bytesToHex(env, digest, (int)digest_len);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hmacLibtomcrypt(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray key) {
    // HMAC functions are now provided by GmSSL to avoid symbol conflicts
    return env->NewStringUTF("");
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_encryptLibtomcrypt(
        JNIEnv *env, jclass, jstring transformation, jbyteArray plaintext, jbyteArray key,
        jbyteArray iv) {
    ensure_ltc_registered();

    const char *alg = env->GetStringUTFChars(transformation, nullptr);

    // Parse algorithm string: "AES/CBC/PKCS7Padding", "AES/ECB", "DES/CBC", etc.
    char cipher_name[32] = {0};
    char mode[16] = {0};
    const char *slash = strchr(alg, '/');
    if (slash) {
        strncpy(cipher_name, alg, slash - alg);
        cipher_name[slash - alg] = '\0';
        // Extract mode (stop at next '/' or end)
        const char *mode_start = slash + 1;
        const char *second_slash = strchr(mode_start, '/');
        if (second_slash) {
            strncpy(mode, mode_start, second_slash - mode_start);
            mode[second_slash - mode_start] = '\0';
        } else {
            strncpy(mode, mode_start, sizeof(mode) - 1);
        }
    } else {
        strncpy(cipher_name, alg, sizeof(cipher_name) - 1);
        strcpy(mode, "CBC");
    }
    // Convert cipher_name to lowercase for libtomcrypt
    for (int i = 0; cipher_name[i]; i++) {
        cipher_name[i] = tolower(cipher_name[i]);
    }

    int cipher_idx = find_cipher(cipher_name);
    if (cipher_idx == -1) {
        LOGE("encryptLibtomcrypt: unsupported cipher: %s", cipher_name);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    jsize plaintext_len = env->GetArrayLength(plaintext);
    jbyte *plaintext_bytes = env->GetByteArrayElements(plaintext, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);
    jbyte *iv_bytes = nullptr;
    if (iv) {
        iv_bytes = env->GetByteArrayElements(iv, nullptr);
    }

    symmetric_CBC cbc_ctx;
    symmetric_ECB ecb_ctx;
    symmetric_CTR ctr_ctx;
    symmetric_CFB cfb_ctx;
    symmetric_OFB ofb_ctx;

    int block_len = cipher_descriptor[cipher_idx].block_length;
    // PKCS7 padding
    int pad_bytes = block_len - (plaintext_len % block_len);
    int padded_len = plaintext_len + pad_bytes;
    unsigned char *padded_input = (unsigned char *)calloc(padded_len, 1);
    memcpy(padded_input, plaintext_bytes, plaintext_len);
    // Fill padding bytes with pad value
    for (int i = plaintext_len; i < padded_len; i++) {
        padded_input[i] = (unsigned char)pad_bytes;
    }

    unsigned char *out = (unsigned char *)malloc(padded_len);
    int err = CRYPT_OK;

    if (strcmp(mode, "CBC") == 0) {
        err = cbc_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &cbc_ctx);
        if (err == CRYPT_OK) {
            err = cbc_encrypt(padded_input, out, padded_len, &cbc_ctx);
        }
        cbc_done(&cbc_ctx);
    } else if (strcmp(mode, "ECB") == 0) {
        err = ecb_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &ecb_ctx);
        if (err == CRYPT_OK) {
            err = ecb_encrypt(padded_input, out, padded_len, &ecb_ctx);
        }
        ecb_done(&ecb_ctx);
    } else if (strcmp(mode, "CTR") == 0) {
        err = ctr_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, CTR_COUNTER_BIG_ENDIAN, &ctr_ctx);
        if (err == CRYPT_OK) {
            err = ctr_encrypt(padded_input, out, padded_len, &ctr_ctx);
        }
        ctr_done(&ctr_ctx);
    } else if (strcmp(mode, "CFB") == 0) {
        err = cfb_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &cfb_ctx);
        if (err == CRYPT_OK) {
            err = cfb_encrypt(padded_input, out, padded_len, &cfb_ctx);
        }
        cfb_done(&cfb_ctx);
    } else if (strcmp(mode, "OFB") == 0) {
        err = ofb_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &ofb_ctx);
        if (err == CRYPT_OK) {
            err = ofb_encrypt(padded_input, out, padded_len, &ofb_ctx);
        }
        ofb_done(&ofb_ctx);
    } else {
        LOGE("encryptLibtomcrypt: unsupported mode: %s", mode);
        err = CRYPT_INVALID_CIPHER;
    }

    if (err != CRYPT_OK) {
        LOGE("encryptLibtomcrypt: encryption failed: %s", error_to_string(err));
        free(padded_input);
        free(out);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    jbyteArray result = env->NewByteArray(padded_len);
    env->SetByteArrayRegion(result, 0, padded_len, reinterpret_cast<jbyte *>(out));

    free(padded_input);
    free(out);
    env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(transformation, alg);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_decryptLibtomcrypt(
        JNIEnv *env, jclass, jstring transformation, jbyteArray ciphertext, jbyteArray key,
        jbyteArray iv) {
    ensure_ltc_registered();

    const char *alg = env->GetStringUTFChars(transformation, nullptr);

    char cipher_name[32] = {0};
    char mode[16] = {0};
    const char *slash = strchr(alg, '/');
    if (slash) {
        strncpy(cipher_name, alg, slash - alg);
        cipher_name[slash - alg] = '\0';
        // Extract mode (stop at next '/' or end)
        const char *mode_start = slash + 1;
        const char *second_slash = strchr(mode_start, '/');
        if (second_slash) {
            strncpy(mode, mode_start, second_slash - mode_start);
            mode[second_slash - mode_start] = '\0';
        } else {
            strncpy(mode, mode_start, sizeof(mode) - 1);
        }
    } else {
        strncpy(cipher_name, alg, sizeof(cipher_name) - 1);
        strcpy(mode, "CBC");
    }
    // Convert cipher_name to lowercase for libtomcrypt
    for (int i = 0; cipher_name[i]; i++) {
        cipher_name[i] = tolower(cipher_name[i]);
    }

    int cipher_idx = find_cipher(cipher_name);
    if (cipher_idx == -1) {
        LOGE("decryptLibtomcrypt: unsupported cipher: %s", cipher_name);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    jsize ciphertext_len = env->GetArrayLength(ciphertext);
    jbyte *ciphertext_bytes = env->GetByteArrayElements(ciphertext, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);
    jbyte *iv_bytes = nullptr;
    if (iv) {
        iv_bytes = env->GetByteArrayElements(iv, nullptr);
    }

    symmetric_CBC cbc_ctx;
    symmetric_ECB ecb_ctx;
    symmetric_CTR ctr_ctx;
    symmetric_CFB cfb_ctx;
    symmetric_OFB ofb_ctx;

    unsigned char *out = (unsigned char *)malloc(ciphertext_len);
    int err = CRYPT_OK;

    if (strcmp(mode, "CBC") == 0) {
        err = cbc_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &cbc_ctx);
        if (err == CRYPT_OK) {
            err = cbc_decrypt(reinterpret_cast<const unsigned char *>(ciphertext_bytes),
                              out, ciphertext_len, &cbc_ctx);
        }
        cbc_done(&cbc_ctx);
    } else if (strcmp(mode, "ECB") == 0) {
        err = ecb_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &ecb_ctx);
        if (err == CRYPT_OK) {
            err = ecb_decrypt(reinterpret_cast<const unsigned char *>(ciphertext_bytes),
                              out, ciphertext_len, &ecb_ctx);
        }
        ecb_done(&ecb_ctx);
    } else if (strcmp(mode, "CTR") == 0) {
        err = ctr_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, CTR_COUNTER_BIG_ENDIAN, &ctr_ctx);
        if (err == CRYPT_OK) {
            err = ctr_decrypt(reinterpret_cast<const unsigned char *>(ciphertext_bytes),
                              out, ciphertext_len, &ctr_ctx);
        }
        ctr_done(&ctr_ctx);
    } else if (strcmp(mode, "CFB") == 0) {
        err = cfb_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &cfb_ctx);
        if (err == CRYPT_OK) {
            err = cfb_decrypt(reinterpret_cast<const unsigned char *>(ciphertext_bytes),
                              out, ciphertext_len, &cfb_ctx);
        }
        cfb_done(&cfb_ctx);
    } else if (strcmp(mode, "OFB") == 0) {
        err = ofb_start(cipher_idx,
                        reinterpret_cast<const unsigned char *>(iv_bytes),
                        reinterpret_cast<const unsigned char *>(key_bytes), key_len,
                        0, &ofb_ctx);
        if (err == CRYPT_OK) {
            err = ofb_decrypt(reinterpret_cast<const unsigned char *>(ciphertext_bytes),
                              out, ciphertext_len, &ofb_ctx);
        }
        ofb_done(&ofb_ctx);
    } else {
        LOGE("decryptLibtomcrypt: unsupported mode: %s", mode);
        err = CRYPT_INVALID_CIPHER;
    }

    if (err != CRYPT_OK) {
        LOGE("decryptLibtomcrypt: decryption failed: %s", error_to_string(err));
        free(out);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    jbyteArray result = env->NewByteArray(ciphertext_len);
    env->SetByteArrayRegion(result, 0, ciphertext_len, reinterpret_cast<jbyte *>(out));

    free(out);
    env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(transformation, alg);

    return result;
}
