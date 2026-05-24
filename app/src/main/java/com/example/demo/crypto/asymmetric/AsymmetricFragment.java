package com.example.demo.crypto.asymmetric;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.lsposed.lsparanoid.Obfuscate;

import com.example.demo.crypto.hash.HashManager;
import com.example.demo.crypto.native_.NativeCryptoManager;
import com.example.demo.databinding.FragmentAsymmetricBinding;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

@Obfuscate
public class AsymmetricFragment extends Fragment {

    private FragmentAsymmetricBinding binding;
    private KeyPair currentKeyPair; // RSA

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAsymmetricBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] algorithms = {"RSA", "SM2", "SM9"};
        String[] keySizes = {"2048", "4096"};
        binding.spinnerAlgorithm.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, algorithms));
        binding.spinnerKeySize.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, keySizes));

        binding.btnGenerateKeypair.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            try {
                appendLog("=== 生成 " + algorithm + " 密钥对 ===");
                switch (algorithm) {
                    case "RSA":
                        int keySize = Integer.parseInt((String) binding.spinnerKeySize.getSelectedItem());
                        currentKeyPair = AsymmetricManager.generateRSAKeyPairJCA(keySize);
                        appendLog("[JCA] RSA-" + keySize + " 密钥对已生成");
                        appendLog("公钥: " + HashManager.bytesToHex(currentKeyPair.getPublic().getEncoded()).substring(0, 64) + "...");
                        break;
                    case "SM2":
                        AsymmetricManager.generateSM2KeyPairGmSSL();
                        appendLog("[GmSSL] SM2 密钥对已生成");
                        appendLog("公钥: " + HashManager.bytesToHex(AsymmetricManager.sm2PublicKey).substring(0, 64) + "...");
                        appendLog("私钥: " + HashManager.bytesToHex(AsymmetricManager.sm2PrivateKey).substring(0, 32) + "...");
                        break;
                    case "SM9":
                        appendLog("[GmSSL] SM9 使用临时密钥对（每次加密自动生成）");
                        break;
                }
                appendLog("");
            } catch (Exception e) {
                appendLog("生成密钥对失败: " + e.getMessage());
            }
        });

        binding.btnEncrypt.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            String plaintext = binding.etPlaintext.getText().toString().trim();
            if (plaintext.isEmpty()) { appendLog("请输入明文"); return; }

            try {
                byte[] ptBytes = plaintext.getBytes(StandardCharsets.UTF_8);
                appendLog("=== " + algorithm + " 加密 ===");
                appendLog("明文: " + plaintext);

                switch (algorithm) {
                    case "RSA":
                        if (currentKeyPair == null) {
                            appendLog("请先生成密钥对"); return;
                        }
                        // JCA
                        appendLog("RSA-OAEP 使用随机填充，每次加密结果不同是正常的");
                        String transformation = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
                        try {
                            String plaintextHex = HashManager.bytesToHex(ptBytes);
                            String result = AsymmetricManager.encryptRSAJCA(transformation, plaintextHex, currentKeyPair.getPublic());
                            appendLog("[JCA] " + result);
                        } catch (Exception e) { appendLog("[JCA] 不支持: " + e.getMessage()); }

                        // OpenSSL
                        try {
                            byte[] pubKeyDer = currentKeyPair.getPublic().getEncoded();
                            byte[] result = NativeCryptoManager.encryptAsymOpenSSL("RSA", ptBytes, pubKeyDer);
                            if (result != null) {
                                appendLog("[OpenSSL] " + HashManager.bytesToHex(result));
                            } else {
                                appendLog("[OpenSSL] 待实现");
                            }
                        } catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                        break;

                    case "SM2":
                        if (AsymmetricManager.sm2PublicKey == null) {
                            appendLog("请先生成 SM2 密钥对"); return;
                        }
                        // GmSSL 加密 + 解密验证
                        try {
                            byte[] encrypted = AsymmetricManager.encryptSM2GmSSL(ptBytes);
                            appendLog("[GmSSL] 加密: " + HashManager.bytesToHex(encrypted).substring(0, Math.min(64, encrypted.length * 2)) + "...");
                            // 解密验证
                            byte[] decrypted = AsymmetricManager.decryptSM2GmSSL(encrypted);
                            String decStr = new String(decrypted, StandardCharsets.UTF_8);
                            appendLog("[GmSSL] 解密: " + decStr + (decStr.equals(plaintext) ? " ✓" : " ✗"));
                        } catch (Exception e) { appendLog("[GmSSL] 失败: " + e.getMessage()); }

                        // GmSSL 签名 + 验签
                        try {
                            byte[] signature = AsymmetricManager.signSM2GmSSL(ptBytes);
                            appendLog("[GmSSL] 签名: " + HashManager.bytesToHex(signature).substring(0, Math.min(64, signature.length * 2)) + "...");
                            boolean valid = AsymmetricManager.verifySM2GmSSL(ptBytes, signature);
                            appendLog("[GmSSL] 验签: " + (valid ? "通过 ✓" : "失败 ✗"));
                        } catch (Exception e) { appendLog("[GmSSL] 签名/验签失败: " + e.getMessage()); }
                        break;

                    case "SM9":
                        // GmSSL SM9 加密（自动生成临时密钥对）
                        try {
                            byte[] encrypted = AsymmetricManager.encryptSM9GmSSL(ptBytes);
                            appendLog("[GmSSL] SM9 加密成功");
                            appendLog("[GmSSL] 密文: " + HashManager.bytesToHex(encrypted).substring(0, Math.min(64, encrypted.length * 2)) + "...");
                            appendLog("[GmSSL] 密文长度: " + encrypted.length + " bytes");
                            appendLog("[GmSSL] SM9 解密需使用同一临时密钥对，此处仅演示加密");
                        } catch (Exception e) { appendLog("[GmSSL] SM9 加密失败: " + e.getMessage()); }
                        break;
                }
                appendLog("");
            } catch (Exception e) {
                appendLog("加密失败: " + e.getMessage());
            }
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
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
