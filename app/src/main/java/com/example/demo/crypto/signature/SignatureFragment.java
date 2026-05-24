package com.example.demo.crypto.signature;

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
import com.example.demo.databinding.FragmentSignatureBinding;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

@Obfuscate
public class SignatureFragment extends Fragment {

    private FragmentSignatureBinding binding;
    private KeyPair currentKeyPair;
    private String currentSignatureHex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignatureBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] algorithms = {"RSA-PSS", "ECDSA"};
        binding.spinnerAlgorithm.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, algorithms));

        binding.btnGenerateKeypair.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            try {
                appendLog("=== 生成 " + algorithm + " 密钥对 ===");
                switch (algorithm) {
                    case "RSA-PSS":
                        currentKeyPair = SignatureManager.generateRSAKeyPair(2048);
                        appendLog("[JCA] RSA-2048 密钥对已生成");
                        break;
                    case "ECDSA":
                        currentKeyPair = SignatureManager.generateECKeyPair(256);
                        appendLog("[JCA] EC-256 密钥对已生成");
                        break;
                }
                currentSignatureHex = null;
                appendLog("");
            } catch (Exception e) {
                appendLog("生成密钥对失败: " + e.getMessage());
            }
        });

        binding.btnSign.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入原文"); return; }
            if (currentKeyPair == null) { appendLog("请先生成密钥对"); return; }

            try {
                String dataHex = HashManager.bytesToHex(input.getBytes(StandardCharsets.UTF_8));
                byte[] dataBytes = input.getBytes(StandardCharsets.UTF_8);
                appendLog("=== " + algorithm + " 签名 ===");
                appendLog("原文: " + input);

                switch (algorithm) {
                    case "RSA-PSS":
                        // JCA
                        try {
                            currentSignatureHex = SignatureManager.signJCA("SHA256withRSA/PSS", dataHex, currentKeyPair.getPrivate());
                            appendLog("[JCA] " + currentSignatureHex);
                        } catch (Exception e) { appendLog("[JCA] 不支持: " + e.getMessage()); }
                        // OpenSSL
                        try {
                            byte[] privKeyDer = currentKeyPair.getPrivate().getEncoded();
                            byte[] sig = NativeCryptoManager.signOpenSSL("RSA-PSS", dataBytes, privKeyDer);
                            if (sig != null) {
                                appendLog("[OpenSSL] " + HashManager.bytesToHex(sig));
                            } else {
                                appendLog("[OpenSSL] 待实现");
                            }
                        } catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                        break;

                    case "ECDSA":
                        // JCA
                        try {
                            currentSignatureHex = SignatureManager.signJCA("SHA256withECDSA", dataHex, currentKeyPair.getPrivate());
                            appendLog("[JCA] " + currentSignatureHex);
                        } catch (Exception e) { appendLog("[JCA] 不支持: " + e.getMessage()); }
                        // Conscrypt
                        try {
                            String sig = SignatureManager.signConscrypt("SHA256withECDSA", dataHex, currentKeyPair.getPrivate());
                            appendLog("[Conscrypt] " + sig);
                        } catch (Exception e) { appendLog("[Conscrypt] 不支持: " + e.getMessage()); }
                        // OpenSSL
                        try {
                            byte[] privKeyDer = currentKeyPair.getPrivate().getEncoded();
                            byte[] sig = NativeCryptoManager.signOpenSSL("ECDSA", dataBytes, privKeyDer);
                            if (sig != null) {
                                appendLog("[OpenSSL] " + HashManager.bytesToHex(sig));
                            } else {
                                appendLog("[OpenSSL] 待实现");
                            }
                        } catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                        break;
                }
                appendLog("");
            } catch (Exception e) {
                appendLog("签名失败: " + e.getMessage());
            }
        });

        binding.btnVerify.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty() || currentSignatureHex == null || currentKeyPair == null) {
                appendLog("请先签名"); return;
            }

            try {
                String dataHex = HashManager.bytesToHex(input.getBytes(StandardCharsets.UTF_8));
                appendLog("=== " + algorithm + " 验签 ===");

                boolean result;
                switch (algorithm) {
                    case "RSA-PSS":
                        result = SignatureManager.verifyJCA("SHA256withRSA/PSS", dataHex, currentSignatureHex, currentKeyPair.getPublic());
                        break;
                    case "ECDSA":
                        result = SignatureManager.verifyJCA("SHA256withECDSA", dataHex, currentSignatureHex, currentKeyPair.getPublic());
                        break;
                    default:
                        result = false;
                }
                appendLog("验签结果: " + (result ? "通过" : "失败"));
                appendLog("");
            } catch (Exception e) {
                appendLog("验签失败: " + e.getMessage());
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
