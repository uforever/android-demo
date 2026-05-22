package com.example.demo.crypto.symmetric;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.crypto.hash.HashManager;
import com.example.demo.crypto.native_.NativeCryptoManager;
import com.example.demo.databinding.FragmentSymmetricBinding;

import java.nio.charset.StandardCharsets;

public class SymmetricFragment extends Fragment {

    private FragmentSymmetricBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSymmetricBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] algorithms = {"AES", "SM4", "DES", "ChaCha20-Poly1305"};
        String[] modes = {"ECB", "CBC", "CTR", "GCM", "CFB", "OFB"};
        String[] paddings = {"PKCS7Padding", "NoPadding"};

        binding.spinnerAlgorithm.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, algorithms));
        binding.spinnerMode.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, modes));
        binding.spinnerPadding.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, paddings));

        binding.btnGenerateKey.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            try {
                int keySize = getKeySize(algorithm);
                String keyHex = SymmetricManager.generateKey(algorithm.equals("ChaCha20-Poly1305") ? "ChaCha20" : algorithm, keySize);
                binding.etKey.setText(keyHex);
                appendLog("生成 " + algorithm + " 密钥 (" + keySize + "位): " + keyHex);
            } catch (Exception e) {
                appendLog("生成密钥失败: " + e.getMessage());
            }
        });

        binding.btnEncrypt.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            String mode = (String) binding.spinnerMode.getSelectedItem();
            String padding = (String) binding.spinnerPadding.getSelectedItem();
            String plaintext = binding.etPlaintext.getText().toString().trim();
            String keyHex = binding.etKey.getText().toString().trim();

            if (plaintext.isEmpty() || keyHex.isEmpty()) {
                appendLog("请输入明文和密钥"); return;
            }

            try {
                String transformation = algorithm.equals("ChaCha20-Poly1305")
                        ? "ChaCha20-Poly1305"
                        : algorithm + "/" + mode + "/" + padding;

                // BC 使用 PKCS5Padding（与 PKCS7Padding 等价）
                String bcTransformation = algorithm.equals("ChaCha20-Poly1305")
                        ? "ChaCha20-Poly1305"
                        : algorithm + "/" + mode + "/" + (padding.equals("PKCS7Padding") ? "PKCS5Padding" : padding);

                // OpenSSL transformation 格式
                String opensslTransformation = getOpenSSLTransformation(algorithm, mode, keyHex.length() * 4);

                String plaintextHex = HashManager.bytesToHex(plaintext.getBytes(StandardCharsets.UTF_8));
                String ivHex = null;

                if (!mode.equals("ECB") && !algorithm.equals("ChaCha20-Poly1305")) {
                    int ivSize = mode.equals("GCM") ? 12 : 16;
                    ivHex = SymmetricManager.generateIV(ivSize);
                } else if (algorithm.equals("ChaCha20-Poly1305")) {
                    ivHex = SymmetricManager.generateIV(12);
                }

                appendLog("=== " + transformation + " ===");
                appendLog("明文: " + plaintext);
                appendLog("密钥: " + keyHex);
                if (ivHex != null) appendLog("IV: " + ivHex);

                byte[] ptBytes = HashManager.hexToBytes(plaintextHex);
                byte[] keyBytes = HashManager.hexToBytes(keyHex);
                byte[] ivBytes = ivHex != null ? HashManager.hexToBytes(ivHex) : null;

                // ===== Java 层 =====
                // AES: JCA + Conscrypt
                if (algorithm.equals("AES")) {
                    try {
                        String result = SymmetricManager.encryptJCA(transformation, plaintextHex, keyHex, ivHex);
                        appendLog("[JCA] " + result);
                    } catch (Exception e) { appendLog("[JCA] 不支持: " + e.getMessage()); }

                    if (mode.equals("GCM")) {
                        try {
                            String result = SymmetricManager.encryptConscrypt(transformation, plaintextHex, keyHex, ivHex);
                            appendLog("[Conscrypt] " + result);
                        } catch (Exception e) { appendLog("[Conscrypt] 不支持: " + e.getMessage()); }
                    }

                    try {
                        String result = SymmetricManager.encryptBC(bcTransformation, plaintextHex, keyHex, ivHex);
                        appendLog("[BC] " + result);
                    } catch (Exception e) { appendLog("[BC] 不支持: " + e.getMessage()); }
                }

                // SM4: BC 不支持 SM4 Cipher，仅使用 Native 层
                if (algorithm.equals("SM4")) {
                    appendLog("[BC] BC 不提供 SM4 Cipher 实现");
                }

                // DES: JCA
                if (algorithm.equals("DES")) {
                    try {
                        String result = SymmetricManager.encryptJCA(transformation, plaintextHex, keyHex, ivHex);
                        appendLog("[JCA] " + result);
                    } catch (Exception e) { appendLog("[JCA] 不支持: " + e.getMessage()); }
                }

                // ChaCha20-Poly1305: Tink
                if (algorithm.equals("ChaCha20-Poly1305")) {
                    appendLog("[Tink] Tink 使用随机 nonce，结果与 libsodium 不同是正常的");
                    try {
                        String result = SymmetricManager.encryptTink(plaintextHex, keyHex, ivHex);
                        appendLog("[Tink] " + result);
                    } catch (Exception e) { appendLog("[Tink] 不支持: " + e.getMessage()); }
                }

                // ===== Native 层 =====
                // AES: OpenSSL + libtomcrypt
                if (algorithm.equals("AES") && opensslTransformation != null) {
                    try {
                        byte[] result = NativeCryptoManager.encryptOpenSSL(opensslTransformation, ptBytes, keyBytes, ivBytes != null ? ivBytes : new byte[0]);
                        appendLog("[OpenSSL] " + HashManager.bytesToHex(result));
                    } catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }

                    try {
                        byte[] result = NativeCryptoManager.encryptLibtomcrypt(transformation, ptBytes, keyBytes, ivBytes != null ? ivBytes : new byte[0]);
                        appendLog("[libtomcrypt] " + HashManager.bytesToHex(result));
                    } catch (Exception e) { appendLog("[libtomcrypt] 不支持: " + e.getMessage()); }
                }

                // SM4: OpenSSL + GmSSL
                if (algorithm.equals("SM4") && opensslTransformation != null) {
                    try {
                        byte[] result = NativeCryptoManager.encryptOpenSSL(opensslTransformation, ptBytes, keyBytes, ivBytes != null ? ivBytes : new byte[0]);
                        appendLog("[OpenSSL] " + HashManager.bytesToHex(result));
                    } catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }

                    try {
                        byte[] result = NativeCryptoManager.encryptGmSSL(transformation, ptBytes, keyBytes, ivBytes != null ? ivBytes : new byte[0]);
                        appendLog("[GmSSL] " + HashManager.bytesToHex(result));
                    } catch (Exception e) { appendLog("[GmSSL] 不支持: " + e.getMessage()); }
                }

                // DES: libtomcrypt
                if (algorithm.equals("DES")) {
                    try {
                        byte[] result = NativeCryptoManager.encryptLibtomcrypt(transformation, ptBytes, keyBytes, ivBytes != null ? ivBytes : new byte[0]);
                        appendLog("[libtomcrypt] " + HashManager.bytesToHex(result));
                    } catch (Exception e) { appendLog("[libtomcrypt] 不支持: " + e.getMessage()); }
                }

                // ChaCha20-Poly1305: libsodium
                if (algorithm.equals("ChaCha20-Poly1305")) {
                    try {
                        byte[] result = NativeCryptoManager.encryptLibsodium("ChaCha20-Poly1305", ptBytes, keyBytes, ivBytes != null ? ivBytes : new byte[0]);
                        appendLog("[libsodium] " + HashManager.bytesToHex(result));
                    } catch (Exception e) { appendLog("[libsodium] 不支持: " + e.getMessage()); }
                }

                appendLog("");
            } catch (Exception e) {
                appendLog("加密失败: " + e.getMessage());
            }
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
    }

    private String getOpenSSLTransformation(String algorithm, String mode, int keyBits) {
        if (algorithm.equals("AES")) {
            return "AES-" + (keyBits) + "-" + mode;
        } else if (algorithm.equals("SM4")) {
            return "SM4-" + mode;
        }
        return null;
    }

    private int getKeySize(String algorithm) {
        switch (algorithm) {
            case "AES": return 256;
            case "SM4": return 128;
            case "DES": return 56;
            case "ChaCha20-Poly1305": return 256;
            default: return 128;
        }
    }

    @SuppressLint("SetTextI18n")
    private void appendLog(String message) {
        String current = binding.tvLog.getText().toString();
        if (!current.isEmpty()) current += "\n";
        binding.tvLog.setText(current + message);
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
