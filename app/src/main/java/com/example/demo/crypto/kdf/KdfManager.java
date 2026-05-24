package com.example.demo.crypto.kdf;

import com.example.demo.crypto.hash.HashManager;
import com.example.demo.crypto.native_.NativeCryptoManager;

import org.lsposed.lsparanoid.Obfuscate;

import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@Obfuscate
public class KdfManager {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // ===== PBKDF2 =====

    // PBKDF2 (JCA)
    public static String pbkdf2JCA(String password, String salt, int iterations, int outputLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), iterations, outputLength * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] result = skf.generateSecret(spec).getEncoded();
        return HashManager.bytesToHex(result);
    }

    // PBKDF2 (OpenSSL)
    public static String pbkdf2OpenSSL(String password, String salt, int iterations, int outputLength) throws Exception {
        byte[] result = NativeCryptoManager.pbkdf2OpenSSL(
                password.getBytes(StandardCharsets.UTF_8),
                salt.getBytes(StandardCharsets.UTF_8),
                iterations, outputLength);
        return HashManager.bytesToHex(result);
    }

    // ===== HKDF =====

    // HKDF (BC) - standard HKDF-Extract-then-Expand
    public static String hkdfBC(String ikmHex, String saltHex, String info, int outputLength) throws Exception {
        byte[] ikm = HashManager.hexToBytes(ikmHex);
        byte[] salt = saltHex != null && !saltHex.isEmpty() ? HashManager.hexToBytes(saltHex) : null;
        byte[] infoBytes = info != null ? info.getBytes(StandardCharsets.UTF_8) : null;

        HKDFParameters params = new HKDFParameters(ikm, salt, infoBytes);
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new org.bouncycastle.crypto.digests.SHA256Digest());
        hkdf.init(params);
        byte[] output = new byte[outputLength];
        hkdf.generateBytes(output, 0, outputLength);
        return HashManager.bytesToHex(output);
    }
}
