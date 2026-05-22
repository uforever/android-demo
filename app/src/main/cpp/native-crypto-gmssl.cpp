#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include <gmssl/sm3.h>
#include <gmssl/sm4.h>
#include <gmssl/sm2.h>
#include <gmssl/sm9.h>
#include <gmssl/hex.h>

#define LOG_TAG "NativeCrypto-GmSSL"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

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
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hashGmSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM3") != 0) {
        LOGE("hashGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return env->NewStringUTF("");
    }
    env->ReleaseStringUTFChars(algorithm, alg);

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);

    uint8_t digest[SM3_DIGEST_SIZE];
    sm3_digest(reinterpret_cast<const uint8_t *>(data_bytes), data_len, digest);

    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);

    return bytesToHex(env, digest, SM3_DIGEST_SIZE);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hmacGmSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray key) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM3") != 0) {
        LOGE("hmacGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return env->NewStringUTF("");
    }
    env->ReleaseStringUTFChars(algorithm, alg);

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);

    uint8_t mac[SM3_HMAC_SIZE];
    sm3_hmac(reinterpret_cast<const uint8_t *>(key_bytes), key_len,
             reinterpret_cast<const uint8_t *>(data_bytes), data_len, mac);

    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);

    return bytesToHex(env, mac, SM3_HMAC_SIZE);
}

// ===== SM2 密钥对生成 =====
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_generateKeyPairGmSSL(
        JNIEnv *env, jclass, jstring algorithm) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM2") != 0) {
        LOGE("generateKeyPairGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
    env->ReleaseStringUTFChars(algorithm, alg);

    SM2_KEY key;
    if (sm2_key_generate(&key) != 1) {
        LOGE("generateKeyPairGmSSL: sm2_key_generate failed");
        return nullptr;
    }

    // 返回公钥(65字节) + 私钥(32字节)
    uint8_t pub_key[65]; // 未压缩格式: 04 + x(32) + y(32)
    sm2_point_to_uncompressed_octets(&key.public_key, pub_key);

    int total_len = 65 + 32;
    jbyteArray result = env->NewByteArray(total_len);
    env->SetByteArrayRegion(result, 0, 65, reinterpret_cast<jbyte *>(pub_key));
    env->SetByteArrayRegion(result, 65, 32, reinterpret_cast<jbyte *>(key.private_key));

    return result;
}

// ===== SM2 加密 =====
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_encryptAsymGmSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray plaintext, jbyteArray publicKeyBytes) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM2") == 0) {
        env->ReleaseStringUTFChars(algorithm, alg);

        jsize pt_len = env->GetArrayLength(plaintext);
        jbyte *pt_bytes = env->GetByteArrayElements(plaintext, nullptr);
        jsize pub_len = env->GetArrayLength(publicKeyBytes);
        jbyte *pub_bytes = env->GetByteArrayElements(publicKeyBytes, nullptr);

        SM2_KEY key;
        memset(&key, 0, sizeof(SM2_KEY));

        // 从未压缩公钥字节解析
        if (pub_len == 65 && pub_bytes[0] == 0x04) {
            sm2_point_from_xy(&key.public_key,
                              reinterpret_cast<const uint8_t *>(pub_bytes + 1),
                              reinterpret_cast<const uint8_t *>(pub_bytes + 33));
        } else {
            LOGE("encryptAsymGmSSL: invalid SM2 public key format, len=%d", pub_len);
            env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(publicKeyBytes, pub_bytes, JNI_ABORT);
            return nullptr;
        }

        uint8_t outbuf[SM2_MAX_CIPHERTEXT_SIZE];
        size_t outlen = sizeof(outbuf);

        if (sm2_encrypt(&key, reinterpret_cast<const uint8_t *>(pt_bytes), pt_len, outbuf, &outlen) != 1) {
            LOGE("encryptAsymGmSSL: sm2_encrypt failed");
            env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(publicKeyBytes, pub_bytes, JNI_ABORT);
            return nullptr;
        }

        jbyteArray result = env->NewByteArray((jsize)outlen);
        env->SetByteArrayRegion(result, 0, (jsize)outlen, reinterpret_cast<jbyte *>(outbuf));

        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyBytes, pub_bytes, JNI_ABORT);
        return result;

    } else if (strcmp(alg, "SM9") == 0) {
        env->ReleaseStringUTFChars(algorithm, alg);

        jsize pt_len = env->GetArrayLength(plaintext);
        jbyte *pt_bytes = env->GetByteArrayElements(plaintext, nullptr);

        // SM9 加密需要主公钥，这里在 native 层生成临时密钥对
        SM9_ENC_MASTER_KEY master_key;
        if (sm9_enc_master_key_generate(&master_key) != 1) {
            LOGE("encryptAsymGmSSL: sm9_enc_master_key_generate failed");
            env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
            return nullptr;
        }

        const char *id = "alice";
        size_t idlen = strlen(id);

        uint8_t outbuf[SM9_MAX_CIPHERTEXT_SIZE];
        size_t outlen = sizeof(outbuf);

        if (sm9_encrypt(&master_key, id, idlen,
                        reinterpret_cast<const uint8_t *>(pt_bytes), pt_len,
                        outbuf, &outlen) != 1) {
            LOGE("encryptAsymGmSSL: sm9_encrypt failed");
            env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
            return nullptr;
        }

        jbyteArray result = env->NewByteArray((jsize)outlen);
        env->SetByteArrayRegion(result, 0, (jsize)outlen, reinterpret_cast<jbyte *>(outbuf));

        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        return result;

    } else {
        LOGE("encryptAsymGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
}

// ===== SM2 解密 =====
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_decryptAsymGmSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray ciphertext, jbyteArray publicKeyBytes, jbyteArray privateKeyBytes) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM2") == 0) {
        env->ReleaseStringUTFChars(algorithm, alg);

        jsize ct_len = env->GetArrayLength(ciphertext);
        jbyte *ct_bytes = env->GetByteArrayElements(ciphertext, nullptr);
        jsize pub_len = env->GetArrayLength(publicKeyBytes);
        jbyte *pub_bytes = env->GetByteArrayElements(publicKeyBytes, nullptr);
        jsize priv_len = env->GetArrayLength(privateKeyBytes);
        jbyte *priv_bytes = env->GetByteArrayElements(privateKeyBytes, nullptr);

        SM2_KEY key;
        memset(&key, 0, sizeof(SM2_KEY));

        // 设置私钥
        if (priv_len == 32) {
            sm2_key_set_private_key(&key, reinterpret_cast<const uint8_t *>(priv_bytes));
        } else {
            LOGE("decryptAsymGmSSL: invalid SM2 private key length: %d", priv_len);
            env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(publicKeyBytes, pub_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(privateKeyBytes, priv_bytes, JNI_ABORT);
            return nullptr;
        }

        uint8_t outbuf[SM2_MAX_PLAINTEXT_SIZE];
        size_t outlen = sizeof(outbuf);

        if (sm2_decrypt(&key, reinterpret_cast<const uint8_t *>(ct_bytes), ct_len, outbuf, &outlen) != 1) {
            LOGE("decryptAsymGmSSL: sm2_decrypt failed");
            env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(publicKeyBytes, pub_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(privateKeyBytes, priv_bytes, JNI_ABORT);
            return nullptr;
        }

        jbyteArray result = env->NewByteArray((jsize)outlen);
        env->SetByteArrayRegion(result, 0, (jsize)outlen, reinterpret_cast<jbyte *>(outbuf));

        env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyBytes, pub_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(privateKeyBytes, priv_bytes, JNI_ABORT);
        return result;

    } else if (strcmp(alg, "SM9") == 0) {
        env->ReleaseStringUTFChars(algorithm, alg);

        jsize ct_len = env->GetArrayLength(ciphertext);
        jbyte *ct_bytes = env->GetByteArrayElements(ciphertext, nullptr);

        // SM9 解密需要用户私钥，这里在 native 层生成临时密钥对
        SM9_ENC_MASTER_KEY master_key;
        if (sm9_enc_master_key_generate(&master_key) != 1) {
            LOGE("decryptAsymGmSSL: sm9_enc_master_key_generate failed");
            env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
            return nullptr;
        }

        const char *id = "alice";
        size_t idlen = strlen(id);

        SM9_ENC_KEY enc_key;
        if (sm9_enc_master_key_extract_key(&master_key, id, idlen, &enc_key) != 1) {
            LOGE("decryptAsymGmSSL: sm9_enc_master_key_extract_key failed");
            env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
            return nullptr;
        }

        uint8_t outbuf[SM9_MAX_PLAINTEXT_SIZE];
        size_t outlen = sizeof(outbuf);

        if (sm9_decrypt(&enc_key, id, idlen,
                        reinterpret_cast<const uint8_t *>(ct_bytes), ct_len,
                        outbuf, &outlen) != 1) {
            LOGE("decryptAsymGmSSL: sm9_decrypt failed");
            env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
            return nullptr;
        }

        jbyteArray result = env->NewByteArray((jsize)outlen);
        env->SetByteArrayRegion(result, 0, (jsize)outlen, reinterpret_cast<jbyte *>(outbuf));

        env->ReleaseByteArrayElements(ciphertext, ct_bytes, JNI_ABORT);
        return result;

    } else {
        LOGE("decryptAsymGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
}

// ===== SM2/SM9 签名 =====
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_signGmSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray privateKeyDer) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM2") == 0) {
        env->ReleaseStringUTFChars(algorithm, alg);

        jsize data_len = env->GetArrayLength(data);
        jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);
        jsize priv_len = env->GetArrayLength(privateKeyDer);
        jbyte *priv_bytes = env->GetByteArrayElements(privateKeyDer, nullptr);

        SM2_KEY key;
        memset(&key, 0, sizeof(SM2_KEY));

        if (priv_len == 32) {
            sm2_key_set_private_key(&key, reinterpret_cast<const uint8_t *>(priv_bytes));
        } else {
            LOGE("signGmSSL: invalid SM2 private key length: %d", priv_len);
            env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(privateKeyDer, priv_bytes, JNI_ABORT);
            return nullptr;
        }

        // 先计算 SM3 摘要（带 SM2 默认 ID）
        SM2_SIGN_CTX ctx;
        sm2_sign_init(&ctx, &key, SM2_DEFAULT_ID, SM2_DEFAULT_ID_LENGTH);
        sm2_sign_update(&ctx, reinterpret_cast<const uint8_t *>(data_bytes), data_len);

        uint8_t sig[SM2_MAX_SIGNATURE_SIZE];
        size_t siglen = sizeof(sig);

        if (sm2_sign_finish(&ctx, sig, &siglen) != 1) {
            LOGE("signGmSSL: sm2_sign_finish failed");
            env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(privateKeyDer, priv_bytes, JNI_ABORT);
            return nullptr;
        }

        jbyteArray result = env->NewByteArray((jsize)siglen);
        env->SetByteArrayRegion(result, 0, (jsize)siglen, reinterpret_cast<jbyte *>(sig));

        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(privateKeyDer, priv_bytes, JNI_ABORT);
        return result;

    } else {
        LOGE("signGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
}

// ===== SM2/SM9 验签 =====
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_verifyGmSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray signature, jbyteArray publicKeyDer) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "SM2") == 0) {
        env->ReleaseStringUTFChars(algorithm, alg);

        jsize data_len = env->GetArrayLength(data);
        jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);
        jsize sig_len = env->GetArrayLength(signature);
        jbyte *sig_bytes = env->GetByteArrayElements(signature, nullptr);
        jsize pub_len = env->GetArrayLength(publicKeyDer);
        jbyte *pub_bytes = env->GetByteArrayElements(publicKeyDer, nullptr);

        SM2_KEY key;
        memset(&key, 0, sizeof(SM2_KEY));

        if (pub_len == 65 && pub_bytes[0] == 0x04) {
            sm2_point_from_xy(&key.public_key,
                              reinterpret_cast<const uint8_t *>(pub_bytes + 1),
                              reinterpret_cast<const uint8_t *>(pub_bytes + 33));
        } else {
            LOGE("verifyGmSSL: invalid SM2 public key format");
            env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(signature, sig_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(publicKeyDer, pub_bytes, JNI_ABORT);
            return JNI_FALSE;
        }

        SM2_SIGN_CTX ctx;
        sm2_verify_init(&ctx, &key, SM2_DEFAULT_ID, SM2_DEFAULT_ID_LENGTH);
        sm2_verify_update(&ctx, reinterpret_cast<const uint8_t *>(data_bytes), data_len);

        int ret = sm2_verify_finish(&ctx, reinterpret_cast<const uint8_t *>(sig_bytes), sig_len);

        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(signature, sig_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyDer, pub_bytes, JNI_ABORT);

        return (ret == 1) ? JNI_TRUE : JNI_FALSE;

    } else {
        LOGE("verifyGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return JNI_FALSE;
    }
}

// ===== 对称加密（SM4） =====
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_encryptGmSSL(
        JNIEnv *env, jclass, jstring transformation, jbyteArray plaintext, jbyteArray key,
        jbyteArray iv) {
    const char *alg = env->GetStringUTFChars(transformation, nullptr);

    // SM2 和 SM9 走 encryptAsymGmSSL
    if (strcmp(alg, "SM2") == 0 || strcmp(alg, "SM9") == 0) {
        LOGD("encryptGmSSL: %s should use encryptAsymGmSSL", alg);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    jsize plaintext_len = env->GetArrayLength(plaintext);
    jbyte *plaintext_bytes = env->GetByteArrayElements(plaintext, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);

    if (strncmp(alg, "SM4/CBC", 7) == 0 || strcmp(alg, "SM4-CBC") == 0 || strcmp(alg, "SM4") == 0) {
        jbyte *iv_bytes = env->GetByteArrayElements(iv, nullptr);

        SM4_KEY sm4_key;
        sm4_set_encrypt_key(&sm4_key, reinterpret_cast<const uint8_t *>(key_bytes));

        size_t outlen = 0;
        unsigned char *out = (unsigned char *)malloc(plaintext_len + SM4_BLOCK_SIZE);

        sm4_cbc_padding_encrypt(&sm4_key,
                                reinterpret_cast<const uint8_t *>(iv_bytes),
                                reinterpret_cast<const uint8_t *>(plaintext_bytes), plaintext_len,
                                out, &outlen);

        jbyteArray result = env->NewByteArray((jsize)outlen);
        env->SetByteArrayRegion(result, 0, (jsize)outlen, reinterpret_cast<jbyte *>(out));

        free(out);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);

        return result;

    } else if (strncmp(alg, "SM4/ECB", 7) == 0 || strcmp(alg, "SM4-ECB") == 0) {
        SM4_KEY sm4_key;
        sm4_set_encrypt_key(&sm4_key, reinterpret_cast<const uint8_t *>(key_bytes));

        int pad_len = SM4_BLOCK_SIZE - (plaintext_len % SM4_BLOCK_SIZE);
        int padded_len = plaintext_len + pad_len;
        unsigned char *padded = (unsigned char *)malloc(padded_len);
        memcpy(padded, plaintext_bytes, plaintext_len);
        memset(padded + plaintext_len, pad_len, pad_len);

        unsigned char *out = (unsigned char *)malloc(padded_len);

        for (int i = 0; i < padded_len; i += SM4_BLOCK_SIZE) {
            sm4_encrypt(&sm4_key, padded + i, out + i);
        }

        jbyteArray result = env->NewByteArray(padded_len);
        env->SetByteArrayRegion(result, 0, padded_len, reinterpret_cast<jbyte *>(out));

        free(padded);
        free(out);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);

        return result;

    } else if (strncmp(alg, "SM4/CTR", 7) == 0 || strcmp(alg, "SM4-CTR") == 0) {
        jbyte *iv_bytes = env->GetByteArrayElements(iv, nullptr);

        SM4_KEY sm4_key;
        sm4_set_encrypt_key(&sm4_key, reinterpret_cast<const uint8_t *>(key_bytes));

        unsigned char *out = (unsigned char *)malloc(plaintext_len);
        uint8_t ctr[SM4_BLOCK_SIZE];
        memcpy(ctr, iv_bytes, SM4_BLOCK_SIZE);

        sm4_ctr_encrypt(&sm4_key, ctr,
                        reinterpret_cast<const uint8_t *>(plaintext_bytes), plaintext_len,
                        out);

        jbyteArray result = env->NewByteArray(plaintext_len);
        env->SetByteArrayRegion(result, 0, plaintext_len, reinterpret_cast<jbyte *>(out));

        free(out);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);

        return result;

    } else {
        LOGE("encryptGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_decryptGmSSL(
        JNIEnv *env, jclass, jstring transformation, jbyteArray ciphertext, jbyteArray key,
        jbyteArray iv) {
    const char *alg = env->GetStringUTFChars(transformation, nullptr);

    jsize ciphertext_len = env->GetArrayLength(ciphertext);
    jbyte *ciphertext_bytes = env->GetByteArrayElements(ciphertext, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);

    if (strncmp(alg, "SM4/CBC", 7) == 0 || strcmp(alg, "SM4-CBC") == 0 || strcmp(alg, "SM4") == 0) {
        jbyte *iv_bytes = env->GetByteArrayElements(iv, nullptr);

        SM4_KEY sm4_key;
        sm4_set_decrypt_key(&sm4_key, reinterpret_cast<const uint8_t *>(key_bytes));

        size_t outlen = 0;
        unsigned char *out = (unsigned char *)malloc(ciphertext_len + SM4_BLOCK_SIZE);

        sm4_cbc_padding_decrypt(&sm4_key,
                                reinterpret_cast<const uint8_t *>(iv_bytes),
                                reinterpret_cast<const uint8_t *>(ciphertext_bytes), ciphertext_len,
                                out, &outlen);

        jbyteArray result = env->NewByteArray((jsize)outlen);
        env->SetByteArrayRegion(result, 0, (jsize)outlen, reinterpret_cast<jbyte *>(out));

        free(out);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);

        return result;

    } else if (strncmp(alg, "SM4/ECB", 7) == 0 || strcmp(alg, "SM4-ECB") == 0) {
        SM4_KEY sm4_key;
        sm4_set_decrypt_key(&sm4_key, reinterpret_cast<const uint8_t *>(key_bytes));

        unsigned char *out = (unsigned char *)malloc(ciphertext_len);

        for (int i = 0; i < ciphertext_len; i += SM4_BLOCK_SIZE) {
            sm4_encrypt(&sm4_key,
                        reinterpret_cast<const uint8_t *>(ciphertext_bytes) + i,
                        out + i);
        }

        int pad_val = out[ciphertext_len - 1];
        if (pad_val < 1 || pad_val > SM4_BLOCK_SIZE) {
            LOGE("decryptGmSSL: invalid PKCS7 padding value: %d", pad_val);
            free(out);
            env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
            env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
            env->ReleaseStringUTFChars(transformation, alg);
            return nullptr;
        }
        int unpadded_len = ciphertext_len - pad_val;

        jbyteArray result = env->NewByteArray(unpadded_len);
        env->SetByteArrayRegion(result, 0, unpadded_len, reinterpret_cast<jbyte *>(out));

        free(out);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);

        return result;

    } else if (strncmp(alg, "SM4/CTR", 7) == 0 || strcmp(alg, "SM4-CTR") == 0) {
        jbyte *iv_bytes = env->GetByteArrayElements(iv, nullptr);

        SM4_KEY sm4_key;
        sm4_set_encrypt_key(&sm4_key, reinterpret_cast<const uint8_t *>(key_bytes));

        unsigned char *out = (unsigned char *)malloc(ciphertext_len);
        uint8_t ctr[SM4_BLOCK_SIZE];
        memcpy(ctr, iv_bytes, SM4_BLOCK_SIZE);

        sm4_ctr_encrypt(&sm4_key, ctr,
                        reinterpret_cast<const uint8_t *>(ciphertext_bytes), ciphertext_len,
                        out);

        jbyteArray result = env->NewByteArray(ciphertext_len);
        env->SetByteArrayRegion(result, 0, ciphertext_len, reinterpret_cast<jbyte *>(out));

        free(out);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);

        return result;

    } else {
        LOGE("decryptGmSSL: unsupported algorithm: %s", alg);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }
}
