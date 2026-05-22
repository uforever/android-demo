package com.example.demo.crypto.native_;

public class NativeCryptoManager {

    static {
        System.loadLibrary("demo");
    }

    // 哈希
    public static native String hashOpenSSL(String algorithm, byte[] data);
    public static native String hashLibtomcrypt(String algorithm, byte[] data);
    public static native String hashGmSSL(String algorithm, byte[] data);
    public static native String hashLibsodium(String algorithm, byte[] data);

    // HMAC
    public static native String hmacOpenSSL(String algorithm, byte[] data, byte[] key);
    public static native String hmacLibtomcrypt(String algorithm, byte[] data, byte[] key);
    public static native String hmacGmSSL(String algorithm, byte[] data, byte[] key);

    // 对称加密
    public static native byte[] encryptOpenSSL(String transformation, byte[] plaintext, byte[] key, byte[] iv);
    public static native byte[] decryptOpenSSL(String transformation, byte[] ciphertext, byte[] key, byte[] iv);
    public static native byte[] encryptLibtomcrypt(String transformation, byte[] plaintext, byte[] key, byte[] iv);
    public static native byte[] decryptLibtomcrypt(String transformation, byte[] ciphertext, byte[] key, byte[] iv);
    public static native byte[] encryptGmSSL(String transformation, byte[] plaintext, byte[] key, byte[] iv);
    public static native byte[] decryptGmSSL(String transformation, byte[] ciphertext, byte[] key, byte[] iv);
    public static native byte[] encryptLibsodium(String algorithm, byte[] plaintext, byte[] key, byte[] iv);
    public static native byte[] decryptLibsodium(String algorithm, byte[] ciphertext, byte[] key, byte[] iv);

    // 非对称加密（OpenSSL）
    public static native byte[][] generateKeyPairOpenSSL(String algorithm, int keySize);
    public static native byte[] encryptAsymOpenSSL(String algorithm, byte[] plaintext, byte[] publicKeyDer);
    public static native byte[] decryptAsymOpenSSL(String algorithm, byte[] ciphertext, byte[] privateKeyDer);

    // 非对称加密（GmSSL - SM2/SM9）
    // generateKeyPairGmSSL: 返回 byte[97] = 公钥(65字节未压缩) + 私钥(32字节)
    public static native byte[] generateKeyPairGmSSL(String algorithm);
    // encryptAsymGmSSL: SM2 需要 publicKeyBytes(65字节), SM9 不需要
    public static native byte[] encryptAsymGmSSL(String algorithm, byte[] plaintext, byte[] publicKeyBytes);
    // decryptAsymGmSSL: SM2 需要 publicKeyBytes(65字节) + privateKeyBytes(32字节), SM9 不需要
    public static native byte[] decryptAsymGmSSL(String algorithm, byte[] ciphertext, byte[] publicKeyBytes, byte[] privateKeyBytes);

    // 签名
    public static native byte[] signOpenSSL(String algorithm, byte[] data, byte[] privateKeyDer);
    public static native boolean verifyOpenSSL(String algorithm, byte[] data, byte[] signature, byte[] publicKeyDer);
    public static native byte[] signLibsodium(String algorithm, byte[] data, byte[] privateKey);
    public static native boolean verifyLibsodium(String algorithm, byte[] data, byte[] signature, byte[] publicKey);
    public static native byte[] signGmSSL(String algorithm, byte[] data, byte[] privateKeyDer);
    public static native boolean verifyGmSSL(String algorithm, byte[] data, byte[] signature, byte[] publicKeyDer);

    // KDF
    public static native byte[] pbkdf2OpenSSL(byte[] password, byte[] salt, int iterations, int outputLength);
    public static native byte[] argon2Libsodium(byte[] password, byte[] salt, int iterations, int memory, int parallelism, int outputLength);
    public static native byte[] scryptLibsodium(byte[] password, byte[] salt, int n, int r, int p, int outputLength);

    // 密钥交换
    public static native byte[][] keyExchangeX25519Libsodium();
}
