package com.example.demo.crypto.pqc;

import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMExtractor;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSASigner;

import java.security.SecureRandom;
import java.security.Security;

public class PqcManager {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private static MLKEMParameters getMLKEMParams(String name) {
        switch (name) {
            case "ML-KEM-512": return MLKEMParameters.ml_kem_512;
            case "ML-KEM-768": return MLKEMParameters.ml_kem_768;
            case "ML-KEM-1024": return MLKEMParameters.ml_kem_1024;
            default: return MLKEMParameters.ml_kem_768;
        }
    }

    private static MLDSAParameters getMLDSAParams(String name) {
        switch (name) {
            case "ML-DSA-44": return MLDSAParameters.ml_dsa_44;
            case "ML-DSA-65": return MLDSAParameters.ml_dsa_65;
            case "ML-DSA-87": return MLDSAParameters.ml_dsa_87;
            default: return MLDSAParameters.ml_dsa_65;
        }
    }

    // ===== ML-KEM =====

    public static AsymmetricCipherKeyPair generateMLKEMKeyPairFull(String parameterSet) throws Exception {
        MLKEMKeyPairGenerator kpg = new MLKEMKeyPairGenerator();
        kpg.init(new MLKEMKeyGenerationParameters(new SecureRandom(), getMLKEMParams(parameterSet)));
        return kpg.generateKeyPair();
    }

    public static byte[][] generateMLKEMKeyPair(String parameterSet) throws Exception {
        AsymmetricCipherKeyPair keyPair = generateMLKEMKeyPairFull(parameterSet);
        MLKEMPublicKeyParameters pubKey = (MLKEMPublicKeyParameters) keyPair.getPublic();
        MLKEMPrivateKeyParameters privKey = (MLKEMPrivateKeyParameters) keyPair.getPrivate();
        return new byte[][]{
            pubKey.getEncoded(),
            privKey.getEncoded()
        };
    }

    // ML-KEM 封装（Encapsulate）- 返回密文和共享密钥
    public static byte[][] encapsulate(MLKEMPublicKeyParameters publicKey) throws Exception {
        MLKEMGenerator generator = new MLKEMGenerator(new SecureRandom());
        SecretWithEncapsulation result = generator.generateEncapsulated(publicKey);
        return new byte[][]{
            result.getEncapsulation(),  // 密文
            result.getSecret()          // 共享密钥
        };
    }

    // ML-KEM 解封装（Decapsulate）- 返回共享密钥
    public static byte[] decapsulate(MLKEMPrivateKeyParameters privateKey, byte[] ciphertext) throws Exception {
        MLKEMExtractor extractor = new MLKEMExtractor(privateKey);
        return extractor.extractSecret(ciphertext);
    }

    // ===== ML-DSA =====

    public static AsymmetricCipherKeyPair generateMLDSAKeyPairFull(String parameterSet) throws Exception {
        MLDSAKeyPairGenerator kpg = new MLDSAKeyPairGenerator();
        kpg.init(new MLDSAKeyGenerationParameters(new SecureRandom(), getMLDSAParams(parameterSet)));
        return kpg.generateKeyPair();
    }

    public static byte[][] generateMLDSAKeyPair(String parameterSet) throws Exception {
        AsymmetricCipherKeyPair keyPair = generateMLDSAKeyPairFull(parameterSet);
        MLDSAPublicKeyParameters pubKey = (MLDSAPublicKeyParameters) keyPair.getPublic();
        MLDSAPrivateKeyParameters privKey = (MLDSAPrivateKeyParameters) keyPair.getPrivate();
        return new byte[][]{
            pubKey.getEncoded(),
            privKey.getEncoded()
        };
    }

    // ML-DSA 签名
    public static byte[] sign(MLDSAPrivateKeyParameters privateKey, byte[] message) throws Exception {
        Signer signer = new MLDSASigner();
        signer.init(true, privateKey);
        signer.update(message, 0, message.length);
        return signer.generateSignature();
    }

    // ML-DSA 验签
    public static boolean verify(MLDSAPublicKeyParameters publicKey, byte[] message, byte[] signature) throws Exception {
        Signer verifier = new MLDSASigner();
        verifier.init(false, publicKey);
        verifier.update(message, 0, message.length);
        return verifier.verifySignature(signature);
    }
}
