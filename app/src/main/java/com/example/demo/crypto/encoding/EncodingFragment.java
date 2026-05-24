package com.example.demo.crypto.encoding;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.lsposed.lsparanoid.Obfuscate;

import com.example.demo.databinding.FragmentEncodingBinding;

@Obfuscate
public class EncodingFragment extends Fragment {

    private FragmentEncodingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEncodingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBase64Encode.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }
            try {
                appendLog("=== Base64 编码 ===");
                appendLog("输入: " + input);
                appendLog("结果: " + EncodingManager.base64Encode(input));
                appendLog("");
            } catch (Exception e) {
                appendLog("编码失败: " + e.getMessage());
            }
        });

        binding.btnBase64Decode.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }
            try {
                appendLog("=== Base64 解码 ===");
                appendLog("输入: " + input);
                appendLog("结果: " + EncodingManager.base64Decode(input));
                appendLog("");
            } catch (Exception e) {
                appendLog("解码失败: " + e.getMessage());
            }
        });

        binding.btnHexEncode.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }
            try {
                appendLog("=== Hex 编码 ===");
                appendLog("输入: " + input);
                appendLog("结果: " + EncodingManager.hexEncode(input));
                appendLog("");
            } catch (Exception e) {
                appendLog("编码失败: " + e.getMessage());
            }
        });

        binding.btnHexDecode.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }
            try {
                appendLog("=== Hex 解码 ===");
                appendLog("输入: " + input);
                appendLog("结果: " + EncodingManager.hexDecode(input));
                appendLog("");
            } catch (Exception e) {
                appendLog("解码失败: " + e.getMessage());
            }
        });

        binding.btnUrlEncode.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }
            try {
                appendLog("=== URL 编码 ===");
                appendLog("输入: " + input);
                appendLog("结果: " + EncodingManager.urlEncode(input));
                appendLog("");
            } catch (Exception e) {
                appendLog("编码失败: " + e.getMessage());
            }
        });

        binding.btnUrlDecode.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) { appendLog("请输入文本"); return; }
            try {
                appendLog("=== URL 解码 ===");
                appendLog("输入: " + input);
                appendLog("结果: " + EncodingManager.urlDecode(input));
                appendLog("");
            } catch (Exception e) {
                appendLog("解码失败: " + e.getMessage());
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
