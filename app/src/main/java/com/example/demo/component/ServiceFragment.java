package com.example.demo.component;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentServiceBinding;

public class ServiceFragment extends Fragment {

    private static final String TAG = "ServiceFragment";
    private FragmentServiceBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentServiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnStartBackground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.demo.BackgroundService.class);
            requireContext().startService(intent);
            Log.d(TAG, "后台服务已启动");
        });

        binding.btnStopBackground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.demo.BackgroundService.class);
            requireContext().stopService(intent);
            Log.d(TAG, "后台服务已停止");
        });

        binding.btnStartForeground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.demo.ForegroundService.class);
            requireContext().startService(intent);
            Log.d(TAG, "前台服务已启动");
        });

        binding.btnStopForeground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.demo.ForegroundService.class);
            requireContext().stopService(intent);
            Log.d(TAG, "前台服务已停止");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
