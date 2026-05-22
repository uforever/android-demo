#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include <sodium.h>
#include <sodium/core.h>

#define LOG_TAG "NativeCrypto-Sodium"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Provide a minimal sodium_init since we excluded core.c (which references aegis)
static int _sodium_initialized = 0;
extern "C" int sodium_init(void) {
    if (_sodium_initialized) {
        return 1;
    }
    _sodium_initialized = 1;
    return 0;
}

// Provide sodium_misuse since we excluded core.c
extern "C" void sodium_misuse(void) {
    __android_log_print(ANDROID_LOG_ERROR, "libsodium", "sodium_misuse called");
    abort();
}

static void ensure_sodium_init() {
    sodium_init();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_hashLibsodium(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data) {
    ensure_sodium_init();

    const char *alg = env->GetStringUTFChars(algorithm, nullptr);
    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);

    unsigned char digest[crypto_hash_sha512_BYTES];
    int digest_len = 0;
    int result = -1;

    if (strcmp(alg, "SHA-512") == 0 || strcmp(alg, "SHA512") == 0) {
        result = crypto_hash_sha512(digest,
                                    reinterpret_cast<const unsigned char *>(data_bytes),
                                    data_len);
        digest_len = crypto_hash_sha512_BYTES;
    } else if (strcmp(alg, "SHA-256") == 0 || strcmp(alg, "SHA256") == 0) {
        result = crypto_hash_sha256(digest,
                                    reinterpret_cast<const unsigned char *>(data_bytes),
                                    data_len);
        digest_len = crypto_hash_sha256_BYTES;
    } else if (strcmp(alg, "BLAKE2b") == 0 || strcmp(alg, "BLAKE2B") == 0) {
        result = crypto_generichash(digest, crypto_generichash_BYTES,
                                    reinterpret_cast<const unsigned char *>(data_bytes),
                                    data_len, nullptr, 0);
        digest_len = crypto_generichash_BYTES;
    }

    env->ReleaseStringUTFChars(algorithm, alg);
    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);

    if (result != 0) {
        LOGE("hashLibsodium: unsupported algorithm or hashing failed");
        return env->NewStringUTF("");
    }

    const char hex_chars[] = "0123456789abcdef";
    char *hex = (char *)malloc(digest_len * 2 + 1);
    for (int i = 0; i < digest_len; i++) {
        hex[i * 2] = hex_chars[(digest[i] >> 4) & 0x0f];
        hex[i * 2 + 1] = hex_chars[digest[i] & 0x0f];
    }
    hex[digest_len * 2] = '\0';
    jstring hex_result = env->NewStringUTF(hex);
    free(hex);
    return hex_result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_encryptLibsodium(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray plaintext, jbyteArray key,
        jbyteArray iv) {
    ensure_sodium_init();

    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "ChaCha20-Poly1305") != 0) {
        LOGE("encryptLibsodium: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
    env->ReleaseStringUTFChars(algorithm, alg);

    jsize plaintext_len = env->GetArrayLength(plaintext);
    jbyte *plaintext_bytes = env->GetByteArrayElements(plaintext, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);
    jbyte *iv_bytes = env->GetByteArrayElements(iv, nullptr);

    unsigned long long out_len = 0;
    unsigned char *out = (unsigned char *)malloc(plaintext_len + crypto_aead_chacha20poly1305_ABYTES);

    int result = crypto_aead_chacha20poly1305_encrypt(
            out, &out_len,
            reinterpret_cast<const unsigned char *>(plaintext_bytes), plaintext_len,
            nullptr, 0,          // no additional data
            nullptr,             // nsec (unused)
            reinterpret_cast<const unsigned char *>(iv_bytes),
            reinterpret_cast<const unsigned char *>(key_bytes));

    env->ReleaseByteArrayElements(plaintext, plaintext_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);

    if (result != 0) {
        LOGE("encryptLibsodium: encryption failed");
        free(out);
        return nullptr;
    }

    jbyteArray result_arr = env->NewByteArray((jsize)out_len);
    env->SetByteArrayRegion(result_arr, 0, (jsize)out_len, reinterpret_cast<jbyte *>(out));

    free(out);
    return result_arr;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_decryptLibsodium(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray ciphertext, jbyteArray key,
        jbyteArray iv) {
    ensure_sodium_init();

    const char *alg = env->GetStringUTFChars(algorithm, nullptr);

    if (strcmp(alg, "ChaCha20-Poly1305") != 0) {
        LOGE("decryptLibsodium: unsupported algorithm: %s", alg);
        env->ReleaseStringUTFChars(algorithm, alg);
        return nullptr;
    }
    env->ReleaseStringUTFChars(algorithm, alg);

    jsize ciphertext_len = env->GetArrayLength(ciphertext);
    jbyte *ciphertext_bytes = env->GetByteArrayElements(ciphertext, nullptr);
    jsize key_len = env->GetArrayLength(key);
    jbyte *key_bytes = env->GetByteArrayElements(key, nullptr);
    jbyte *iv_bytes = env->GetByteArrayElements(iv, nullptr);

    unsigned long long out_len = 0;
    unsigned char *out = (unsigned char *)malloc(ciphertext_len);

    int result = crypto_aead_chacha20poly1305_decrypt(
            out, &out_len,
            nullptr,             // nsec (unused)
            reinterpret_cast<const unsigned char *>(ciphertext_bytes), ciphertext_len,
            nullptr, 0,          // no additional data
            reinterpret_cast<const unsigned char *>(iv_bytes),
            reinterpret_cast<const unsigned char *>(key_bytes));

    env->ReleaseByteArrayElements(ciphertext, ciphertext_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(key, key_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(iv, iv_bytes, JNI_ABORT);

    if (result != 0) {
        LOGE("decryptLibsodium: decryption failed");
        free(out);
        return nullptr;
    }

    jbyteArray result_arr = env->NewByteArray((jsize)out_len);
    env->SetByteArrayRegion(result_arr, 0, (jsize)out_len, reinterpret_cast<jbyte *>(out));

    free(out);
    return result_arr;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_signLibsodium(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray privateKey) {
    ensure_sodium_init();

    jsize data_len = env->GetArrayLength(data);
    jbyte *data_bytes = env->GetByteArrayElements(data, nullptr);

    unsigned char pk[crypto_sign_ed25519_PUBLICKEYBYTES];
    unsigned char sk[crypto_sign_ed25519_SECRETKEYBYTES];
    crypto_sign_ed25519_keypair(pk, sk);

    unsigned char *signed_msg = (unsigned char *)malloc(data_len + crypto_sign_ed25519_BYTES);
    unsigned long long signed_len = 0;

    crypto_sign_ed25519(signed_msg, &signed_len,
                        reinterpret_cast<const unsigned char *>(data_bytes), data_len, sk);

    // Return only the signature portion (first 64 bytes)
    jbyteArray result = env->NewByteArray(crypto_sign_ed25519_BYTES);
    env->SetByteArrayRegion(result, 0, crypto_sign_ed25519_BYTES,
                            reinterpret_cast<jbyte *>(signed_msg));

    free(signed_msg);
    env->ReleaseByteArrayElements(data, data_bytes, JNI_ABORT);

    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_verifyLibsodium(
        JNIEnv *env, jclass, jstring algorithm, jbyteArray data, jbyteArray signature, jbyteArray publicKey) {
    LOGD("verifyLibsodium: stub - not implemented");
    return JNI_FALSE;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_argon2Libsodium(
        JNIEnv *env, jclass, jbyteArray password, jbyteArray salt,
        jint iterations, jint memory, jint parallelism, jint outputLength) {
    ensure_sodium_init();

    jsize passwd_len = env->GetArrayLength(password);
    jbyte *passwd_bytes = env->GetByteArrayElements(password, nullptr);
    jsize salt_len = env->GetArrayLength(salt);
    jbyte *salt_bytes = env->GetByteArrayElements(salt, nullptr);

    unsigned char *out = (unsigned char *)malloc(outputLength);

    int result = crypto_pwhash_argon2id(
            out, outputLength,
            reinterpret_cast<const char *>(passwd_bytes), passwd_len,
            reinterpret_cast<const unsigned char *>(salt_bytes),
            (unsigned long long)iterations,
            (size_t)(memory * 1024),
            crypto_pwhash_ALG_ARGON2ID13);

    if (result != 0) {
        LOGE("argon2Libsodium: crypto_pwhash_argon2id failed");
        free(out);
        env->ReleaseByteArrayElements(password, passwd_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(salt, salt_bytes, JNI_ABORT);
        return nullptr;
    }

    jbyteArray result_arr = env->NewByteArray(outputLength);
    env->SetByteArrayRegion(result_arr, 0, outputLength, reinterpret_cast<jbyte *>(out));

    free(out);
    env->ReleaseByteArrayElements(password, passwd_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(salt, salt_bytes, JNI_ABORT);

    return result_arr;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_scryptLibsodium(
        JNIEnv *env, jclass, jbyteArray password, jbyteArray salt,
        jint n, jint r, jint p, jint outputLength) {
    ensure_sodium_init();

    jsize passwd_len = env->GetArrayLength(password);
    jbyte *passwd_bytes = env->GetByteArrayElements(password, nullptr);
    jsize salt_len = env->GetArrayLength(salt);
    jbyte *salt_bytes = env->GetByteArrayElements(salt, nullptr);

    unsigned char *out = (unsigned char *)malloc(outputLength);

    int result = crypto_pwhash_scryptsalsa208sha256(
            out, outputLength,
            reinterpret_cast<const char *>(passwd_bytes), passwd_len,
            reinterpret_cast<const unsigned char *>(salt_bytes),
            (unsigned long long)n,
            (size_t)(r * p * 1024));

    if (result != 0) {
        LOGE("scryptLibsodium: crypto_pwhash_scryptsalsa208sha256 failed");
        free(out);
        env->ReleaseByteArrayElements(password, passwd_bytes, JNI_ABORT);
        env->ReleaseByteArrayElements(salt, salt_bytes, JNI_ABORT);
        return nullptr;
    }

    jbyteArray result_arr = env->NewByteArray(outputLength);
    env->SetByteArrayRegion(result_arr, 0, outputLength, reinterpret_cast<jbyte *>(out));

    free(out);
    env->ReleaseByteArrayElements(password, passwd_bytes, JNI_ABORT);
    env->ReleaseByteArrayElements(salt, salt_bytes, JNI_ABORT);

    return result_arr;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_demo_crypto_native_1_NativeCryptoManager_keyExchangeX25519Libsodium(
        JNIEnv *env, jclass) {
    ensure_sodium_init();

    unsigned char pk1[crypto_box_PUBLICKEYBYTES];
    unsigned char sk1[crypto_box_SECRETKEYBYTES];
    unsigned char pk2[crypto_box_PUBLICKEYBYTES];
    unsigned char sk2[crypto_box_SECRETKEYBYTES];

    crypto_box_keypair(pk1, sk1);
    crypto_box_keypair(pk2, sk2);

    unsigned char shared1[crypto_scalarmult_curve25519_BYTES];
    unsigned char shared2[crypto_scalarmult_curve25519_BYTES];

    crypto_scalarmult_curve25519(shared1, sk1, pk2);
    crypto_scalarmult_curve25519(shared2, sk2, pk1);

    // Verify both sides computed the same shared secret
    if (memcmp(shared1, shared2, crypto_scalarmult_curve25519_BYTES) != 0) {
        LOGE("keyExchangeX25519Libsodium: shared secret mismatch");
        return nullptr;
    }

    // Return byte[][] with 2 elements: publicKey and sharedSecret
    jclass byteArrayClass = env->FindClass("[B");
    jobjectArray result = env->NewObjectArray(2, byteArrayClass, nullptr);

    // Element 0: publicKey (pk1)
    jbyteArray publicKey = env->NewByteArray(crypto_box_PUBLICKEYBYTES);
    env->SetByteArrayRegion(publicKey, 0, crypto_box_PUBLICKEYBYTES,
                            reinterpret_cast<jbyte *>(pk1));
    env->SetObjectArrayElement(result, 0, publicKey);

    // Element 1: sharedSecret
    jbyteArray sharedSecret = env->NewByteArray(crypto_scalarmult_curve25519_BYTES);
    env->SetByteArrayRegion(sharedSecret, 0, crypto_scalarmult_curve25519_BYTES,
                            reinterpret_cast<jbyte *>(shared1));
    env->SetObjectArrayElement(result, 1, sharedSecret);

    return result;
}
