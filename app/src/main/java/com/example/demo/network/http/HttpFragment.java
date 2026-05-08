package com.example.demo.network.http;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentHttpBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HttpFragment extends Fragment {

    private FragmentHttpBinding binding;
    private OkHttpManager okHttpManager;
    private RetrofitManager retrofitManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHttpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        okHttpManager = OkHttpManager.getInstance();
        retrofitManager = RetrofitManager.getInstance();

        okHttpManager.setListener(new OkHttpManager.OnHttpResponseListener() {
            @Override
            public void onSuccess(String response, int statusCode, String headers) {
                appendLog(response);
            }

            @Override
            public void onFailure(String error) {
                appendLog("错误: " + error);
            }
        });

        retrofitManager.setListener(new RetrofitManager.OnHttpResponseListener() {
            @Override
            public void onSuccess(String response, int statusCode, String headers) {
                appendLog(response);
            }

            @Override
            public void onFailure(String error) {
                appendLog("错误: " + error);
            }
        });

        binding.btnOkhttpGet.setOnClickListener(v -> {
            appendLog("=== OkHttp GET 请求 ===");
            new Thread(() -> okHttpManager.performGetRequest("https://echo.hoppscotch.io?foo=bar")).start();
        });

        binding.btnOkhttpPost.setOnClickListener(v -> {
            appendLog("=== OkHttp POST 请求 ===");
            new Thread(() -> {
                try {
                    JSONObject json = new JSONObject();
                    json.put("name", "Test");
                    json.put("value", "Hello from OkHttp");
                    okHttpManager.performPostRequest("https://echo.hoppscotch.io", json);
                } catch (Exception e) {
                    appendLog("JSON 构建失败: " + e.getMessage());
                }
            }).start();
        });

        binding.btnRetrofitGet.setOnClickListener(v -> {
            appendLog("=== Retrofit GET 请求 ===");
            new Thread(() -> retrofitManager.performGetRequest("test_value")).start();
        });

        binding.btnRetrofitPost.setOnClickListener(v -> {
            appendLog("=== Retrofit POST 请求 ===");
            new Thread(() -> {
                Map<String, Object> body = new HashMap<>();
                body.put("name", "Test");
                body.put("value", "Hello from Retrofit");
                retrofitManager.performPostRequest(body);
            }).start();
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
        super.onDestroyView();
        binding = null;
    }
}
