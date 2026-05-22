package com.example.demo.crypto.hash;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashManager {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // JCA 哈希
    public static String hashJCA(String algorithm, String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    // Conscrypt 哈希（通过 JCA 接口，Conscrypt 作为 Provider）
    public static String hashConscrypt(String algorithm, String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm, "Conscrypt");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    // Bouncy Castle 哈希（使用轻量级 API 支持 SM3, BLAKE2b）
    public static String hashBC(String algorithm, String input) throws Exception {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        org.bouncycastle.crypto.Digest digest;

        switch (algorithm) {
            case "SM3":
                digest = new org.bouncycastle.crypto.digests.SM3Digest();
                break;
            case "BLAKE2b-256":
                digest = new org.bouncycastle.crypto.digests.Blake2bDigest(256);
                break;
            case "BLAKE2b-512":
                digest = new org.bouncycastle.crypto.digests.Blake2bDigest(512);
                break;
            default:
                // Fallback to JCA
                MessageDigest md = MessageDigest.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
                return bytesToHex(md.digest(inputBytes));
        }

        byte[] result = new byte[digest.getDigestSize()];
        digest.update(inputBytes, 0, inputBytes.length);
        digest.doFinal(result, 0);
        return bytesToHex(result);
    }

    // HMAC (JCA)
    public static String hmacJCA(String algorithm, String input, String key) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        mac.init(keySpec);
        byte[] result = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(result);
    }

    // HMAC (Conscrypt)
    public static String hmacConscrypt(String algorithm, String input, String key) throws Exception {
        Mac mac = Mac.getInstance(algorithm, "Conscrypt");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        mac.init(keySpec);
        byte[] result = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(result);
    }

    // HMAC (BC lightweight API)
    public static String hmacBC(String algorithm, String input, String key) throws Exception {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        org.bouncycastle.crypto.Digest digest;

        switch (algorithm) {
            case "HmacSM3":
                digest = new org.bouncycastle.crypto.digests.SM3Digest();
                break;
            case "HmacSHA256":
                digest = new org.bouncycastle.crypto.digests.SHA256Digest();
                break;
            case "HmacSHA512":
                digest = new org.bouncycastle.crypto.digests.SHA512Digest();
                break;
            default:
                // Fallback to JCA
                Mac mac = Mac.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
                SecretKeySpec keySpec = new SecretKeySpec(keyBytes, algorithm);
                mac.init(keySpec);
                return bytesToHex(mac.doFinal(inputBytes));
        }

        org.bouncycastle.crypto.macs.HMac hmac = new org.bouncycastle.crypto.macs.HMac(digest);
        hmac.init(new org.bouncycastle.crypto.params.KeyParameter(keyBytes));
        hmac.update(inputBytes, 0, inputBytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        hmac.doFinal(result, 0);
        return bytesToHex(result);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
