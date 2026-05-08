package com.example.demo.network.grpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentGrpcBinding;

public class GrpcFragment extends Fragment {

    private FragmentGrpcBinding binding;
    private GrpcManager grpcManager;
    private int messageCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGrpcBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        grpcManager = new GrpcManager();

        grpcManager.setListener(new GrpcManager.OnGrpcResponseListener() {
            @Override
            public void onSuccess(String response) {
                appendLog(response);
            }

            @Override
            public void onFailure(String error) {
                appendLog("错误: " + error);
            }
        });

        binding.btnConnect.setOnClickListener(v -> {
            appendLog("=== 连接 gRPC ===");
            grpcManager.connect();
        });

        binding.btnSend.setOnClickListener(v -> {
            String greeting = "Hello gRPC " + (++messageCount);
            appendLog("发送请求: " + greeting);
            grpcManager.sayHello(greeting);
        });

        binding.btnDisconnect.setOnClickListener(v -> {
            appendLog("=== 断开 gRPC ===");
            grpcManager.disconnect();
        });

        binding.btnClearLog.setOnClickListener(v -> {
            binding.tvLog.setText("");
        });
    }

    private void appendLog(String log) {
        if (binding != null) {
            getActivity().runOnUiThread(() -> {
                binding.tvLog.append(log + "\n\n");
                binding.scrollView.post(() -> {
                    binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                });
            });
        }
    }

    @Override
    public void onDestroyView() {
        grpcManager.disconnect();
        binding = null;
        super.onDestroyView();
    }
}
