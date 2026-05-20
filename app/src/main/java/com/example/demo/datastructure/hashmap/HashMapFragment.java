package com.example.demo.datastructure.hashmap;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentHashMapBinding;

public class HashMapFragment extends Fragment {

    private FragmentHashMapBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHashMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnHashSolve.setOnClickListener(v -> {
            String arrStr = binding.etArray.getText().toString().trim();
            String targetStr = binding.etTarget.getText().toString().trim();
            if (arrStr.isEmpty() || targetStr.isEmpty()) {
                appendLog("请输入数组和目标值");
                return;
            }
            try {
                int[] nums = TwoSumSolver.parseNumbers(arrStr);
                int target = Integer.parseInt(targetStr);
                appendLog("=== 哈希表求解 ===");
                appendLog(TwoSumSolver.solveByHashMap(nums, target));
                appendLog("");
            } catch (NumberFormatException e) {
                appendLog("输入格式错误");
            }
        });

        binding.btnBruteSolve.setOnClickListener(v -> {
            String arrStr = binding.etArray.getText().toString().trim();
            String targetStr = binding.etTarget.getText().toString().trim();
            if (arrStr.isEmpty() || targetStr.isEmpty()) {
                appendLog("请输入数组和目标值");
                return;
            }
            try {
                int[] nums = TwoSumSolver.parseNumbers(arrStr);
                int target = Integer.parseInt(targetStr);
                appendLog("=== 暴力求解 ===");
                appendLog(TwoSumSolver.solveByBruteForce(nums, target));
                appendLog("");
            } catch (NumberFormatException e) {
                appendLog("输入格式错误");
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
