package com.example.demo.crypto.signature;

import com.example.demo.crypto.hash.HashManager;

import org.lsposed.lsparanoid.Obfuscate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

@Obfuscate
public class SignatureManager {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // ===== JCA =====

    public static String signJCA(String algorithm, String dataHex, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance(algorithm);
        sig.initSign(privateKey);
        sig.update(HashManager.hexToBytes(dataHex));
        return HashManager.bytesToHex(sig.sign());
    }

    public static boolean verifyJCA(String algorithm, String dataHex, String signatureHex, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance(algorithm);
        sig.initVerify(publicKey);
        sig.update(HashManager.hexToBytes(dataHex));
        return sig.verify(HashManager.hexToBytes(signatureHex));
    }

    // ===== Conscrypt =====

    public static String signConscrypt(String algorithm, String dataHex, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance(algorithm, "Conscrypt");
        sig.initSign(privateKey);
        sig.update(HashManager.hexToBytes(dataHex));
        return HashManager.bytesToHex(sig.sign());
    }

    public static boolean verifyConscrypt(String algorithm, String dataHex, String signatureHex, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance(algorithm, "Conscrypt");
        sig.initVerify(publicKey);
        sig.update(HashManager.hexToBytes(dataHex));
        return sig.verify(HashManager.hexToBytes(signatureHex));
    }

    // ===== BC =====

    public static String signBC(String algorithm, String dataHex, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        sig.initSign(privateKey);
        sig.update(HashManager.hexToBytes(dataHex));
        return HashManager.bytesToHex(sig.sign());
    }

    public static boolean verifyBC(String algorithm, String dataHex, String signatureHex, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        sig.initVerify(publicKey);
        sig.update(HashManager.hexToBytes(dataHex));
        return sig.verify(HashManager.hexToBytes(signatureHex));
    }

    // ===== 密钥对生成 =====

    public static KeyPair generateRSAKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    public static KeyPair generateECKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }
}
