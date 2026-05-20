package com.example.demo.datastructure.stack;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentStackBinding;

public class StackFragment extends Fragment {

    private FragmentStackBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBracketMatch.setOnClickListener(v -> {
            String input = binding.etBracket.getText().toString().trim();
            if (input.isEmpty()) {
                appendLog("请输入含括号的字符串");
                return;
            }
            appendLog("=== 括号匹配 ===");
            appendLog(StackAlgorithm.bracketMatch(input));
            appendLog("");
        });

        binding.btnEvaluate.setOnClickListener(v -> {
            String input = binding.etExpression.getText().toString().trim();
            if (input.isEmpty()) {
                appendLog("请输入表达式");
                return;
            }
            appendLog("=== 表达式求值 ===");
            appendLog(StackAlgorithm.evaluateExpression(input));
            appendLog("");
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
    }

    @SuppressLint("SetTextI18n")
    private void appendLog(String message) {
        String current = binding.tvLog.getText().toString();
        if (!current.isEmpty()) {
            current += "\n";
        }
        binding.tvLog.setText(current + message);
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
