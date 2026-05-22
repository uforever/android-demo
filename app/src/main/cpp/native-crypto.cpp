#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/rand.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/err.h>

#define LOG_TAG "NativeCrypto"
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

static const EVP_CIPHER *get_cipher(const char *algorithm) {
    if (strcmp(algorithm, "AES-256-CBC") == 0) return EVP_aes_256_cbc();
    if (strcmp(algorithm, "AES-128-CBC") == 0) return EVP_aes_128_cbc();
    if (strcmp(algorithm, "AES-256-ECB") == 0) return EVP_aes_256_ecb();
    if (strcmp(algorithm, "AES-256-GCM") == 0) return EVP_aes_256_gcm();
    if (strcmp(algorithm, "AES-256-CTR") == 0) return EVP_aes_256_ctr();
    if (strcmp(algorithm, "AES-256-CFB") == 0) return EVP_aes_256_cfb();
    if (strcmp(algorithm, "AES-256-OFB") == 0) return EVP_aes_256_ofb();
    if (strcmp(algorithm, "SM4-CBC") == 0) return EVP_sm4_cbc();
    if (strcmp(algorithm, "SM4-ECB") == 0) return EVP_sm4_ecb();
    if (strcmp(algorithm, "SM4-CTR") == 0) return EVP_sm4_ctr();
    return nullptr;
}

static const EVP_MD *get_digest(const char *algorithm) {
    if (strcmp(algorithm, "SHA-256") == 0 || strcmp(algorithm, "SHA256") == 0) return EVP_sha256();
    if (strcmp(algorithm, "SHA-512") == 0 || strcmp(algorithm, "SHA512") == 0) return EVP_sha512();
    if (strcmp(algorithm, "SHA-1") == 0 || strcmp(algorithm, "SHA1") == 0) return EVP_sha1();
    if (strcmp(algorithm, "MD5") == 0) return EVP_md5();
    if (strcmp(algorithm, "SM3") == 0) return EVP_sm3();
    return nullptr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hashOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data) {
    const EVP_MD *md = get_digest(env->GetStringUTFChars(algorithm, nullptr));
    if (!md) {
        LOGE("hashOpenSSL: unsupported algorithm");
        return env->NewStringUTF("");
    }

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);

    unsigned char digest[EVP_MAX_MD_SIZE];
    unsigned int digest_len = 0;

    EVP_Digest(data_bytes, data_len, digest, &digest_len, md, nullptr);

    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(algorithm, env->GetStringUTFChars(algorithm, nullptr));

    return bytesToHex(env, digest, digest_len);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hmacOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray key) {
    const EVP_MD *md = get_digest(env->GetStringUTFChars(algorithm, nullptr));
    if (!md) {
        LOGE("hmacOpenSSL: unsupported algorithm");
        return env->NewStringUTF("");
    }

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);

    unsigned char mac[EVP_MAX_MD_SIZE];
    unsigned int mac_len = 0;

    HMAC(md, key_bytes, key_len,
         reinterpret_cast<const unsigned char *>(data_bytes), data_len,
         mac, &mac_len);

    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(algorithm, env->GetStringUTFChars(algorithm, nullptr));

    return bytesToHex(env, mac, mac_len);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_encryptOpenSSL(
        JNIEnv *env, jclass, jstring transformation, jbyteArray plaintext, jbyteArray key,
        jbyteArray iv) {
    const char *alg = env->GetStringUTFChars(transformation, nullptr);
    const EVP_CIPHER *cipher = get_cipher(alg);
    if (!cipher) {
        LOGE("encryptOpenSSL: unsupported algorithm: %s", alg);
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

    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        LOGE("encryptOpenSSL: failed to create cipher context");
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    int is_gcm = (strcmp(alg, "AES-256-GCM") == 0);

    if (EVP_EncryptInit_ex(ctx, cipher, nullptr,
                           reinterpret_cast<unsigned char *>(key_bytes),
                           reinterpret_cast<unsigned char *>(iv_bytes)) != 1) {
        LOGE("encryptOpenSSL: EVP_EncryptInit_ex failed");
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    int out_len = 0;
    int final_len = 0;
    unsigned char *out = (unsigned char *)malloc(plaintext_len + EVP_CIPHER_block_size(cipher) + (is_gcm ? 16 : 0));

    if (EVP_EncryptUpdate(ctx, out, &out_len,
                          reinterpret_cast<unsigned char *>(plaintext_bytes), plaintext_len) != 1) {
        LOGE("encryptOpenSSL: EVP_EncryptUpdate failed");
        free(out);
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    if (EVP_EncryptFinal_ex(ctx, out + out_len, &final_len) != 1) {
        LOGE("encryptOpenSSL: EVP_EncryptFinal_ex failed");
        free(out);
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    int total_len = out_len + final_len;

    jbyteArray result = env->NewByteArray(total_len);
    env->SetByteArrayRegion(result, 0, total_len, reinterpret_cast<jbyte *>(out));

    free(out);
    EVP_CIPHER_CTX_free(ctx);
    env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(transformation, alg);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_decryptOpenSSL(
        JNIEnv *env, jclass, jstring transformation, jbyteArray ciphertext, jbyteArray key,
        jbyteArray iv) {
    const char *alg = env->GetStringUTFChars(transformation, nullptr);
    const EVP_CIPHER *cipher = get_cipher(alg);
    if (!cipher) {
        LOGE("decryptOpenSSL: unsupported algorithm: %s", alg);
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

    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (!ctx) {
        LOGE("decryptOpenSSL: failed to create cipher context");
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    int is_gcm = (strcmp(alg, "AES-256-GCM") == 0);

    if (EVP_DecryptInit_ex(ctx, cipher, nullptr,
                           reinterpret_cast<unsigned char *>(key_bytes),
                           reinterpret_cast<unsigned char *>(iv_bytes)) != 1) {
        LOGE("decryptOpenSSL: EVP_DecryptInit_ex failed");
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    int out_len = 0;
    int final_len = 0;
    unsigned char *out = (unsigned char *)malloc(ciphertext_len + EVP_CIPHER_block_size(cipher));

    if (EVP_DecryptUpdate(ctx, out, &out_len,
                          reinterpret_cast<unsigned char *>(ciphertext_bytes), ciphertext_len) != 1) {
        LOGE("decryptOpenSSL: EVP_DecryptUpdate failed");
        free(out);
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    if (EVP_DecryptFinal_ex(ctx, out + out_len, &final_len) != 1) {
        LOGE("decryptOpenSSL: EVP_DecryptFinal_ex failed");
        free(out);
        EVP_CIPHER_CTX_free(ctx);
        env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
        if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
        env->ReleaseStringUTFChars(transformation, alg);
        return nullptr;
    }

    int total_len = out_len + final_len;

    jbyteArray result = env->NewByteArray(total_len);
    env->SetByteArrayRegion(result, 0, total_len, reinterpret_cast<jbyte *>(out));

    free(out);
    EVP_CIPHER_CTX_free(ctx);
    env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    if (iv_bytes) env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);
    env->ReleaseStringUTFChars(transformation, alg);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_pbkdf2OpenSSL(
        JNIEnv *env, jclass, jbyteArray password, jbyteArray salt,
        jint iterations, jint outputLength) {
    const EVP_MD *md = EVP_sha256();
    if (!md) {
        LOGE("pbkdf2OpenSSL: unsupported algorithm");
        return nullptr;
    }

    jsize passwd_len = env->GetArrayLength(password);
    jbyte *passwd_bytes = env->GetByteArrayElements(password, nullptr);
    jsize salt_len = env->GetArrayLength(salt);
    jbyte *salt_bytes = env->GetByteArrayElements(salt, nullptr);

    unsigned char *out = (unsigned char *)malloc(outputLength);

    if (PKCS5_PBKDF2_HMAC(reinterpret_cast<const char *>(passwd_bytes), passwd_len,
                           reinterpret_cast<unsigned char *>(salt_bytes), salt_len,
                           iterations, md, outputLength, out) != 1) {
        LOGE("pbkdf2OpenSSL: PKCS5_PBKDF2_HMAC failed");
        free(out);
        env->ReleaseByteArrayElements(password, passwd_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(salt, salt_bytes, JNI_ABORT);
        return nullptr;
    }

    jbyteArray result = env->NewByteArray(outputLength);
    env->SetByteArrayRegion(result, 0, outputLength, reinterpret_cast<jbyte *>(out));

    free(out);
    env->ReleaseByteArrayElements(password, passwd_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(salt, salt_bytes, JNI_ABORT);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_signOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray privateKeyDer) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);
    jsize key_len = env->GetArrayLength(privateKeyDer);
    jbyte *key_bytes = env->GetByteArrayElements(privateKeyDer, nullptr);

    const unsigned char *p = (const unsigned char *)key_bytes;
    EVP_PKEY *pkey = d2i_AutoPrivateKey(nullptr, &p, key_len);
    if (!pkey) {
        LOGE("signOpenSSL: failed to parse private key");
        env->ReleaseStringUTFChars(algorithm, alg);
        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(privateKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    EVP_MD_CTX *md_ctx = EVP_MD_CTX_new();
    EVP_PKEY_CTX *pk_ctx = nullptr;

    const EVP_MD *md = EVP_sha256();
    int padding = RSA_PKCS1_PADDING;

    if (strcmp(alg, "RSA-PSS") == 0) {
        padding = RSA_PKCS1_PSS_PADDING;
    }

    if (EVP_DigestSignInit(md_ctx, &pk_ctx, md, nullptr, pkey) <= 0) {
        LOGE("signOpenSSL: DigestSignInit failed");
    } else if (EVP_PKEY_id(pkey) == EVP_PKEY_RSA && pk_ctx) {
        EVP_PKEY_CTX_set_rsa_padding(pk_ctx, padding);
    }

    if (EVP_DigestSignUpdate(md_ctx, data_bytes, data_len) <= 0) {
        LOGE("signOpenSSL: DigestSignUpdate failed");
        EVP_MD_CTX_free(md_ctx);
        EVP_PKEY_free(pkey);
        env->ReleaseStringUTFChars(algorithm, alg);
        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(privateKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    size_t sig_len = 0;
    if (EVP_DigestSignFinal(md_ctx, nullptr, &sig_len) <= 0) {
        LOGE("signOpenSSL: DigestSignFinal (get length) failed");
        EVP_MD_CTX_free(md_ctx);
        EVP_PKEY_free(pkey);
        env->ReleaseStringUTFChars(algorithm, alg);
        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(privateKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    unsigned char *sig = (unsigned char *)malloc(sig_len);
    if (EVP_DigestSignFinal(md_ctx, sig, &sig_len) <= 0) {
        LOGE("signOpenSSL: DigestSignFinal failed");
        free(sig);
        EVP_MD_CTX_free(md_ctx);
        EVP_PKEY_free(pkey);
        env->ReleaseStringUTFChars(algorithm, alg);
        env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(privateKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    jbyteArray result = env->NewByteArray((jsize)sig_len);
    env->SetByteArrayRegion(result, 0, (jsize)sig_len, (jbyte *)sig);

    free(sig);
    EVP_MD_CTX_free(md_ctx);
    EVP_PKEY_free(pkey);
    env->ReleaseStringUTFChars(algorithm, alg);
    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(privateKeyDer, key_bytes, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_verifyOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray signature, jbyteArray publicKeyDer) {
    LOGD("verifyOpenSSL: stub - not implemented");
    return JNI_FALSE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_generateKeyPairOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jint keySize) {
    LOGD("generateKeyPairOpenSSL: stub - not implemented");
    return nullptr;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_encryptAsymOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray plaintext, jbyteArray publicKeyDer) {
    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "RSA") != 0) {
        LOGD("encryptAsymOpenSSL: %s not implemented", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
    env->ReleaseStringUTFChars(algorithm, alg);

    jsize pt_len = env->GetArrayLength(plaintext);
    jbyte *pt_bytes = env->GetByteArrayElements(plaintext, nullptr);
    jsize key_len = env->GetArrayLength(publicKeyDer);
    jbyte *key_bytes = env->GetByteArrayElements(publicKeyDer, nullptr);

    // Parse DER public key
    const unsigned char *p = (const unsigned char *)key_bytes;
    EVP_PKEY *pkey = d2i_PUBKEY(nullptr, &p, key_len);
    if (!pkey) {
        LOGE("encryptAsymOpenSSL: failed to parse public key");
        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    EVP_PKEY_CTX *ctx = EVP_PKEY_CTX_new(pkey, nullptr);
    if (!ctx) {
        EVP_PKEY_free(pkey);
        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    if (EVP_PKEY_encrypt_init(ctx) <= 0 ||
        EVP_PKEY_CTX_set_rsa_padding(ctx, RSA_PKCS1_OAEP_PADDING) <= 0 ||
        EVP_PKEY_CTX_set_rsa_oaep_md(ctx, EVP_sha256()) <= 0) {
        EVP_PKEY_CTX_free(ctx);
        EVP_PKEY_free(pkey);
        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    size_t out_len = 0;
    if (EVP_PKEY_encrypt(ctx, nullptr, &out_len, (const unsigned char *)pt_bytes, pt_len) <= 0) {
        EVP_PKEY_CTX_free(ctx);
        EVP_PKEY_free(pkey);
        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    unsigned char *out = (unsigned char *)malloc(out_len);
    if (EVP_PKEY_encrypt(ctx, out, &out_len, (const unsigned char *)pt_bytes, pt_len) <= 0) {
        free(out);
        EVP_PKEY_CTX_free(ctx);
        EVP_PKEY_free(pkey);
        env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(publicKeyDer, key_bytes, JNI_ABORT);
        return nullptr;
    }

    jbyteArray result = env->NewByteArray((jsize)out_len);
    env->SetByteArrayRegion(result, 0, (jsize)out_len, (jbyte *)out);

    free(out);
    EVP_PKEY_CTX_free(ctx);
    EVP_PKEY_free(pkey);
    env->ReleaseByteArrayElements(plaintext, pt_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(publicKeyDer, key_bytes, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_decryptAsymOpenSSL(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray ciphertext, jbyteArray privateKeyDer) {
    LOGD("decryptAsymOpenSSL: stub - not implemented");
    return nullptr;
}
