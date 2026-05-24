package com.example.demo.crypto.kdf;

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

import com.example.demo.databinding.FragmentKdfBinding;

@Obfuscate
public class KdfFragment extends Fragment {

    private FragmentKdfBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentKdfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] algorithms = {"PBKDF2", "HKDF"};
        binding.spinnerAlgorithm.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, algorithms));

        binding.btnDerive.setOnClickListener(v -> {
            String algorithm = (String) binding.spinnerAlgorithm.getSelectedItem();
            String password = binding.etPassword.getText().toString().trim();
            String salt = binding.etSalt.getText().toString().trim();
            String iterationsStr = binding.etIterations.getText().toString().trim();
            String outputLenStr = binding.etOutputLength.getText().toString().trim();

            if (password.isEmpty()) { appendLog("请输入密码"); return; }
            if (salt.isEmpty()) salt = "default_salt";

            try {
                int iterations = iterationsStr.isEmpty() ? 10000 : Integer.parseInt(iterationsStr);
                int outputLen = outputLenStr.isEmpty() ? 32 : Integer.parseInt(outputLenStr);

                appendLog("=== " + algorithm + " ===");
                appendLog("密码: " + password);
                appendLog("盐值: " + salt);

                switch (algorithm) {
                    case "PBKDF2":
                        // JCA
                        try {
                            appendLog("[JCA] " + KdfManager.pbkdf2JCA(password, salt, iterations, outputLen));
                        } catch (Exception e) { appendLog("[JCA] 不支持: " + e.getMessage()); }
                        // OpenSSL
                        try {
                            String result = KdfManager.pbkdf2OpenSSL(password, salt, iterations, outputLen);
                            appendLog("[OpenSSL] " + result);
                        } catch (Exception e) { appendLog("[OpenSSL] 不支持: " + e.getMessage()); }
                        break;

                    case "HKDF":
                        String ikmHex = com.example.demo.crypto.hash.HashManager.bytesToHex(
                                password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        String saltHex = com.example.demo.crypto.hash.HashManager.bytesToHex(
                                salt.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        // BC
                        try {
                            appendLog("[BC] " + KdfManager.hkdfBC(ikmHex, saltHex, "info", outputLen));
                        } catch (Exception e) { appendLog("[BC] 不支持: " + e.getMessage()); }
                        break;
                }
                appendLog("");
            } catch (Exception e) {
                appendLog("派生失败: " + e.getMessage());
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
