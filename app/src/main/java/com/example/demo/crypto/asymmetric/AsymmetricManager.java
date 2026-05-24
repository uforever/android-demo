package com.example.demo.crypto.asymmetric;

import com.example.demo.crypto.hash.HashManager;
import com.example.demo.crypto.native_.NativeCryptoManager;

import java.security.KeyPair;
import java.security.SecureRandom;

import org.lsposed.lsparanoid.Obfuscate;

import javax.crypto.Cipher;

@Obfuscate
public class AsymmetricManager {

    // ===== RSA =====

    public static KeyPair generateRSAKeyPairJCA(int keySize) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    public static String encryptRSAJCA(String transformation, String plaintextHex, java.security.PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(HashManager.hexToBytes(plaintextHex));
        return HashManager.bytesToHex(encrypted);
    }

    public static String decryptRSAJCA(String transformation, String ciphertextHex, java.security.PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(HashManager.hexToBytes(ciphertextHex));
        return HashManager.bytesToHex(decrypted);
    }

    // ===== SM2 =====

    // SM2 密钥对（GmSSL native 生成，存储为原始字节）
    // publicKey: 65字节 (04 + x(32) + y(32))
    // privateKey: 32字节
    public static byte[] sm2PublicKey;
    public static byte[] sm2PrivateKey;

    public static void generateSM2KeyPairGmSSL() throws Exception {
        byte[] keyBytes = NativeCryptoManager.generateKeyPairGmSSL("SM2");
        if (keyBytes == null || keyBytes.length != 97) {
            throw new Exception("GmSSL SM2 密钥生成失败");
        }
        sm2PublicKey = new byte[65];
        sm2PrivateKey = new byte[32];
        System.arraycopy(keyBytes, 0, sm2PublicKey, 0, 65);
        System.arraycopy(keyBytes, 65, sm2PrivateKey, 0, 32);
    }

    // SM2 加密（GmSSL native）
    public static byte[] encryptSM2GmSSL(byte[] plaintext) throws Exception {
        if (sm2PublicKey == null) throw new Exception("请先生成 SM2 密钥对");
        byte[] result = NativeCryptoManager.encryptAsymGmSSL("SM2", plaintext, sm2PublicKey);
        if (result == null) throw new Exception("GmSSL SM2 加密失败");
        return result;
    }

    // SM2 解密（GmSSL native）
    public static byte[] decryptSM2GmSSL(byte[] ciphertext) throws Exception {
        if (sm2PrivateKey == null) throw new Exception("请先生成 SM2 密钥对");
        byte[] result = NativeCryptoManager.decryptAsymGmSSL("SM2", ciphertext, sm2PublicKey, sm2PrivateKey);
        if (result == null) throw new Exception("GmSSL SM2 解密失败");
        return result;
    }

    // SM2 签名（GmSSL native）
    public static byte[] signSM2GmSSL(byte[] data) throws Exception {
        if (sm2PrivateKey == null) throw new Exception("请先生成 SM2 密钥对");
        byte[] result = NativeCryptoManager.signGmSSL("SM2", data, sm2PrivateKey);
        if (result == null) throw new Exception("GmSSL SM2 签名失败");
        return result;
    }

    // SM2 验签（GmSSL native）
    public static boolean verifySM2GmSSL(byte[] data, byte[] signature) throws Exception {
        if (sm2PublicKey == null) throw new Exception("请先生成 SM2 密钥对");
        return NativeCryptoManager.verifyGmSSL("SM2", data, signature, sm2PublicKey);
    }

    // ===== SM9 =====

    // SM9 加密（GmSSL native - 每次自动生成临时密钥对）
    public static byte[] encryptSM9GmSSL(byte[] plaintext) throws Exception {
        byte[] result = NativeCryptoManager.encryptAsymGmSSL("SM9", plaintext, null);
        if (result == null) throw new Exception("GmSSL SM9 加密失败");
        return result;
    }
}
