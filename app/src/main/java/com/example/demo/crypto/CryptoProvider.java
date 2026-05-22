package com.example.demo.crypto;

import android.annotation.SuppressLint;
import java.security.Security;

@SuppressLint("StaticFieldLeak")
public class CryptoProvider {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;

        // 注册 Bouncy Castle Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // 注册 Conscrypt Provider（优先级高，用于 AES-GCM 等加速）
        Security.insertProviderAt(org.conscrypt.Conscrypt.newProvider(), 1);

        // 注册 Google Tink
        try {
            com.google.crypto.tink.config.TinkConfig.register();
        } catch (Exception e) {
            // Tink 初始化失败不影响其他库
        }

        initialized = true;
    }
}
