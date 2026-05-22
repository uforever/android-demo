package com.example.demo.crypto.hash;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.crypto.native_.NativeCryptoManager;
import com.example.demo.databinding.FragmentHashBinding;

import java.nio.charset.StandardCharsets;

public class HashFragment extends Fragment {

    private FragmentHashBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] algorithms = {"SHA-256", "SHA-512", "SM3", "BLAKE2b", "MD5", "HMAC-SHA256", "HMAC-SM3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, algorithms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAlgorithm.setAdapter(adapter);

        binding.btnCompute.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }

            try {
                appendLog("=== " + algorithm + " ===");
                appendLog("输入: " + input);

                boolean isHmac = algorithm.startsWith("HMAC-");
                String key = "secret_key_123";

                if (isHmac) {
                    computeHMAC(algorithm, input, key);
                } else {
                    computeHash(algorithm, input);
                }
                appendLog("");
            } catch (Exception e) {
                appendLog("计算失败: " + e.getMessage());
            }
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
    }

    private void computeHash(String algorithm, String input) throws Exception {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

        switch (algorithm) {
            case "SHA-256":
            case "SHA-512":
                // JCA
                appendLog("[JCA] " + HashManager.hashJCA(algorithm, input));
                // Conscrypt
                try { appendLog("[Conscrypt] " + HashManager.hashConscrypt(algorithm, input)); }
                catch (Exception e) { appendLog("[Conscrypt] 不支持: " + e.getMessage()); }
                // OpenSSL
                try { appendLog("[OpenSSL] " + NativeCryptoManager.hashOpenSSL(algorithm, inputBytes)); }
                catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                // libsodium
                try { appendLog("[libsodium] " + NativeCryptoManager.hashLibsodium(algorithm, inputBytes)); }
                catch (Exception e) { appendLog("[libsodium] 不支持: " + e.getMessage()); }
                break;

            case "SM3":
                // BC
                appendLog("[BC] " + HashManager.hashBC("SM3", input));
                // GmSSL
                try { appendLog("[GmSSL] " + NativeCryptoManager.hashGmSSL("SM3", inputBytes)); }
                catch (Exception e) { appendLog("[GmSSL] 不支持: " + e.getMessage()); }
                break;

            case "BLAKE2b":
                // BC
                appendLog("[BC] " + HashManager.hashBC("BLAKE2b-256", input));
                // libsodium
                try { appendLog("[libsodium] " + NativeCryptoManager.hashLibsodium("BLAKE2b", inputBytes)); }
                catch (Exception e) { appendLog("[libsodium] 不支持: " + e.getMessage()); }
                break;

            case "MD5":
                // JCA
                appendLog("[JCA] " + HashManager.hashJCA("MD5", input));
                // OpenSSL
                try { appendLog("[OpenSSL] " + NativeCryptoManager.hashOpenSSL("MD5", inputBytes)); }
                catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                // libtomcrypt
                try { appendLog("[libtomcrypt] " + NativeCryptoManager.hashLibtomcrypt("MD5", inputBytes)); }
                catch (Exception e) { appendLog("[libtomcrypt] 不支持: " + e.getMessage()); }
                break;
        }
    }

    private void computeHMAC(String algorithm, String input, String key) throws Exception {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        switch (algorithm) {
            case "HMAC-SHA256":
                // JCA
                appendLog("[JCA] " + HashManager.hmacJCA("HmacSHA256", input, key));
                // Conscrypt
                try { appendLog("[Conscrypt] " + HashManager.hmacConscrypt("HmacSHA256", input, key)); }
                catch (Exception e) { appendLog("[Conscrypt] 不支持: " + e.getMessage()); }
                // OpenSSL
                try { appendLog("[OpenSSL] " + NativeCryptoManager.hmacOpenSSL("SHA256", inputBytes, keyBytes)); }
                catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                break;

            case "HMAC-SM3":
                // BC
                appendLog("[BC] " + HashManager.hmacBC("HmacSM3", input, key));
                // GmSSL
                try { appendLog("[GmSSL] " + NativeCryptoManager.hmacGmSSL("SM3", inputBytes, keyBytes)); }
                catch (Exception e) { appendLog("[GmSSL] 不支持: " + e.getMessage()); }
                break;
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
