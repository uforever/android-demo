package com.example.demo.datastructure.heap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentHeapBinding;

public class HeapFragment extends Fragment {

    private FragmentHeapBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHeapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnFindTopK.setOnClickListener(v -> {
            String numStr = binding.etNumbers.getText().toString().trim();
            String kStr = binding.etK.getText().toString().trim();
            if (numStr.isEmpty() || kStr.isEmpty()) {
                appendLog("请输入数字序列和K值");
                return;
            }
            try {
                int[] nums = TopKFinder.parseNumbers(numStr);
                int k = Integer.parseInt(kStr);
                appendLog("=== Top K 查找 ===");
                appendLog(TopKFinder.findTopK(nums, k));
                appendLog("");
            } catch (NumberFormatException e) {
                appendLog("输入格式错误，请用逗号分隔数字");
            }
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
