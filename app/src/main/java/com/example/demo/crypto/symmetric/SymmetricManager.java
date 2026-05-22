package com.example.demo.crypto.symmetric;

import com.example.demo.crypto.hash.HashManager;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.Aead;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricManager {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try {
            AeadConfig.register();
        } catch (Exception ignored) {}
    }

    // 生成随机密钥
    public static String generateKey(String algorithm, int keySize) throws Exception {
        if (algorithm.equals("SM4")) {
            // SM4: generate random 16 bytes manually (BC 1.80 no longer provides KeyGenerator for SM4)
            byte[] keyBytes = new byte[keySize / 8];
            new SecureRandom().nextBytes(keyBytes);
            return HashManager.bytesToHex(keyBytes);
        } else if (algorithm.equals("ChaCha20")) {
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            kg.init(keySize, new SecureRandom());
            SecretKey key = kg.generateKey();
            return HashManager.bytesToHex(key.getEncoded());
        } else {
            // AES, DES use JCA default provider
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            kg.init(keySize, new SecureRandom());
            SecretKey key = kg.generateKey();
            return HashManager.bytesToHex(key.getEncoded());
        }
    }

    // JCA 加密
    public static String encryptJCA(String transformation, String plaintextHex, String keyHex, String ivHex) throws Exception {
        byte[] keyBytes = HashManager.hexToBytes(keyHex);
        byte[] ivBytes = ivHex != null && !ivHex.isEmpty() ? HashManager.hexToBytes(ivHex) : null;
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, getAlgorithm(transformation));

        Cipher cipher = Cipher.getInstance(transformation);
        if (transformation.contains("GCM")) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, ivBytes != null ? ivBytes : new byte[12]));
        } else if (ivBytes != null && !transformation.contains("ECB")) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }
        byte[] encrypted = cipher.doFinal(HashManager.hexToBytes(plaintextHex));
        return HashManager.bytesToHex(encrypted);
    }

    // BC 加密
    public static String encryptBC(String transformation, String plaintextHex, String keyHex, String ivHex) throws Exception {
        byte[] keyBytes = HashManager.hexToBytes(keyHex);
        byte[] ivBytes = ivHex != null && !ivHex.isEmpty() ? HashManager.hexToBytes(ivHex) : null;
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, getAlgorithm(transformation));

        try {
            // BC provider is intentionally used for testing SM4 and other algorithms
            @SuppressWarnings("DeprecatedProvider")
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            if (transformation.contains("GCM")) {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, ivBytes != null ? ivBytes : new byte[12]));
            } else if (ivBytes != null && !transformation.contains("ECB")) {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            }
            byte[] encrypted = cipher.doFinal(HashManager.hexToBytes(plaintextHex));
            return HashManager.bytesToHex(encrypted);
        } catch (Exception e) {
            throw new Exception("[BC] " + transformation + " 不支持: " + e.getMessage(), e);
        }
    }

    // Conscrypt 加密
    public static String encryptConscrypt(String transformation, String plaintextHex, String keyHex, String ivHex) throws Exception {
        byte[] keyBytes = HashManager.hexToBytes(keyHex);
        byte[] ivBytes = ivHex != null && !ivHex.isEmpty() ? HashManager.hexToBytes(ivHex) : null;
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, getAlgorithm(transformation));

        Cipher cipher = Cipher.getInstance(transformation, "Conscrypt");
        if (transformation.contains("GCM")) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(128, ivBytes != null ? ivBytes : new byte[12]));
        } else if (ivBytes != null && !transformation.contains("ECB")) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }
        byte[] encrypted = cipher.doFinal(HashManager.hexToBytes(plaintextHex));
        return HashManager.bytesToHex(encrypted);
    }

    // Tink ChaCha20-Poly1305 加密
    public static String encryptTink(String plaintextHex, String keyHex, String ivHex) throws Exception {
        byte[] plaintext = HashManager.hexToBytes(plaintextHex);

        // 使用 Tink 的 ChaCha20-Poly1305
        KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.CHACHA20_POLY1305);
        Aead aead = keysetHandle.getPrimitive(Aead.class);

        // Tink 内部管理 nonce，这里传入关联数据
        byte[] ciphertext = aead.encrypt(plaintext, null);
        return HashManager.bytesToHex(ciphertext);
    }

    // 生成随机 IV
    public static String generateIV(int size) {
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return HashManager.bytesToHex(iv);
    }

    private static String getAlgorithm(String transformation) {
        return transformation.split("/")[0].toUpperCase(Locale.ROOT);
    }
}
