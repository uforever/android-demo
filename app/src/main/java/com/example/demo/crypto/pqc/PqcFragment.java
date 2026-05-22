package com.example.demo.crypto.pqc;

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
import com.example.demo.databinding.FragmentPqcBinding;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPublicKeyParameters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PqcFragment extends Fragment {

    private FragmentPqcBinding binding;
    private AsymmetricCipherKeyPair currentKeyPair;
    private String currentAlgorithm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPqcBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] algorithms = {"ML-KEM-512", "ML-KEM-768", "ML-KEM-1024", "ML-DSA-44", "ML-DSA-65", "ML-DSA-87"};
        binding.spinnerAlgorithm.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, algorithms));

        binding.btnGenerateKeypair.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            try {
                appendLog("=== 生成 " + algorithm + " 密钥对 ===");
                if (algorithm.startsWith("ML-KEM")) {
                    currentKeyPair = PqcManager.generateMLKEMKeyPairFull(algorithm);
                    MLKEMPublicKeyParameters pubKey = (MLKEMPublicKeyParameters) currentKeyPair.getPublic();
                    MLKEMPrivateKeyParameters privKey = (MLKEMPrivateKeyParameters) currentKeyPair.getPrivate();
                    appendLog("[BC] ML-KEM 密钥对已生成");
                    appendLog("公钥长度: " + pubKey.getEncoded().length + " bytes");
                    appendLog("私钥长度: " + privKey.getEncoded().length + " bytes");
                } else {
                    currentKeyPair = PqcManager.generateMLDSAKeyPairFull(algorithm);
                    MLDSAPublicKeyParameters pubKey = (MLDSAPublicKeyParameters) currentKeyPair.getPublic();
                    MLDSAPrivateKeyParameters privKey = (MLDSAPrivateKeyParameters) currentKeyPair.getPrivate();
                    appendLog("[BC] ML-DSA 密钥对已生成");
                    appendLog("公钥长度: " + pubKey.getEncoded().length + " bytes");
                    appendLog("私钥长度: " + privKey.getEncoded().length + " bytes");
                }
                currentAlgorithm = algorithm;
                String pubHex = HashManager.bytesToHex(
                    currentKeyPair.getPublic() instanceof MLKEMPublicKeyParameters
                        ? ((MLKEMPublicKeyParameters) currentKeyPair.getPublic()).getEncoded()
                        : ((MLDSAPublicKeyParameters) currentKeyPair.getPublic()).getEncoded());
                appendLog("公钥: " + pubHex.substring(0, Math.min(64, pubHex.length())) + "...");
                appendLog("");
            } catch (Exception e) {
                appendLog("生成密钥对失败: " + e.getMessage());
            }
        });

        binding.btnEncapsulate.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            if (!algorithm.startsWith("ML-KEM")) {
                appendLog("ML-DSA 不支持封装操作，请选择 ML-KEM 算法");
                return;
            }

            try {
                // 确保有密钥对
                ensureKeyPair(algorithm);

                MLKEMPublicKeyParameters pubKey = (MLKEMPublicKeyParameters) currentKeyPair.getPublic();
                MLKEMPrivateKeyParameters privKey = (MLKEMPrivateKeyParameters) currentKeyPair.getPrivate();

                appendLog("=== " + algorithm + " 封装/解封装 ===");

                // 封装
                byte[][] encResult = PqcManager.encapsulate(pubKey);
                byte[] ciphertext = encResult[0];
                byte[] sharedSecretEnc = encResult[1];
                appendLog("封装成功");
                appendLog("密文长度: " + ciphertext.length + " bytes");
                appendLog("密文: " + HashManager.bytesToHex(ciphertext).substring(0, Math.min(64, ciphertext.length * 2)) + "...");
                appendLog("共享密钥(封装): " + HashManager.bytesToHex(sharedSecretEnc));

                // 解封装
                byte[] sharedSecretDec = PqcManager.decapsulate(privKey, ciphertext);
                appendLog("解封装成功");
                appendLog("共享密钥(解封装): " + HashManager.bytesToHex(sharedSecretDec));

                // 验证一致性
                if (Arrays.equals(sharedSecretEnc, sharedSecretDec)) {
                    appendLog("验证: 封装/解封装共享密钥一致 ✓");
                } else {
                    appendLog("验证: 封装/解封装共享密钥不一致 ✗");
                }
                appendLog("");
            } catch (Exception e) {
                appendLog("封装/解封装失败: " + e.getMessage());
            }
        });

        binding.btnSign.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            if (!algorithm.startsWith("ML-DSA")) {
                appendLog("ML-KEM 不支持签名操作，请选择 ML-DSA 算法");
                return;
            }

            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) {
                appendLog("请输入原文");
                return;
            }

            try {
                // 确保有密钥对
                ensureKeyPair(algorithm);

                MLDSAPrivateKeyParameters privKey = (MLDSAPrivateKeyParameters) currentKeyPair.getPrivate();
                MLDSAPublicKeyParameters pubKey = (MLDSAPublicKeyParameters) currentKeyPair.getPublic();

                byte[] message = input.getBytes(StandardCharsets.UTF_8);

                appendLog("=== " + algorithm + " 签名/验签 ===");
                appendLog("原文: " + input);

                // 签名
                byte[] signature = PqcManager.sign(privKey, message);
                appendLog("签名成功");
                appendLog("签名长度: " + signature.length + " bytes");
                appendLog("签名: " + HashManager.bytesToHex(signature).substring(0, Math.min(64, signature.length * 2)) + "...");

                // 验签
                boolean valid = PqcManager.verify(pubKey, message, signature);
                appendLog("验签结果: " + (valid ? "通过 ✓" : "失败 ✗"));

                // 用错误消息验签（验证否定情况）
                byte[] wrongMessage = "Wrong message".getBytes(StandardCharsets.UTF_8);
                boolean wrongValid = PqcManager.verify(pubKey, wrongMessage, signature);
                appendLog("错误消息验签: " + (wrongValid ? "通过（异常）" : "失败（预期） ✓"));
                appendLog("");
            } catch (Exception e) {
                appendLog("签名/验签失败: " + e.getMessage());
            }
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
    }

    private void ensureKeyPair(String algorithm) throws Exception {
        if (currentKeyPair == null || !algorithm.equals(currentAlgorithm)) {
            if (algorithm.startsWith("ML-KEM")) {
                currentKeyPair = PqcManager.generateMLKEMKeyPairFull(algorithm);
            } else {
                currentKeyPair = PqcManager.generateMLDSAKeyPairFull(algorithm);
            }
            currentAlgorithm = algorithm;
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
