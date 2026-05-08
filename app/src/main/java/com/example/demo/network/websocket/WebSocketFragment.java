package com.example.demo.network.websocket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentWebsocketBinding;

public class WebSocketFragment extends Fragment {

    private FragmentWebsocketBinding binding;
    private OkHttpWebSocket okHttpWebSocket;
    private JavaWebSocket javaWebSocket;
    private boolean useOkHttp = true;
    private int messageCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWebsocketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        okHttpWebSocket = new OkHttpWebSocket();
        javaWebSocket = new JavaWebSocket();

        okHttpWebSocket.setListener(new OkHttpWebSocket.OnWebSocketListener() {
            @Override
            public void onConnected() {
                appendLog("[OkHttp WS] 连接成功");
            }

            @Override
            public void onMessage(String message) {
                appendLog("[OkHttp WS] 收到: " + message);
            }

            @Override
            public void onDisconnected(int code, String reason) {
                appendLog("[OkHttp WS] 断开连接: code=" + code + ", reason=" + reason);
            }

            @Override
            public void onError(String error) {
                appendLog("[OkHttp WS] 错误: " + error);
            }
        });

        javaWebSocket.setListener(new JavaWebSocket.OnWebSocketListener() {
            @Override
            public void onConnected() {
                appendLog("[Java WS] 连接成功");
            }

            @Override
            public void onMessage(String message) {
                appendLog("[Java WS] 收到: " + message);
            }

            @Override
            public void onDisconnected(int code, String reason) {
                appendLog("[Java WS] 断开连接: code=" + code + ", reason=" + reason);
            }

            @Override
            public void onError(String error) {
                appendLog("[Java WS] 错误: " + error);
            }
        });

        binding.rgWsType.setOnCheckedChangeListener((group, checkedId) -> {
            useOkHttp = checkedId == binding.rbOkhttp.getId();
            appendLog("切换到 " + (useOkHttp ? "OkHttp WebSocket" : "Java-WebSocket"));
        });

        binding.btnConnect.setOnClickListener(v -> {
            appendLog("=== 连接 WebSocket ===");
            if (useOkHttp) {
                okHttpWebSocket.connect();
            } else {
                javaWebSocket.connect();
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String message = "Hello WebSocket " + (++messageCount);
            if (useOkHttp) {
                if (okHttpWebSocket.isConnected()) {
                    appendLog("[OkHttp WS] 发送: " + message);
                    okHttpWebSocket.send(message);
                } else {
                    appendLog("[OkHttp WS] 未连接");
                }
            } else {
                if (javaWebSocket.isConnected()) {
                    appendLog("[Java WS] 发送: " + message);
                    javaWebSocket.send(message);
                } else {
                    appendLog("[Java WS] 未连接");
                }
            }
        });

        binding.btnDisconnect.setOnClickListener(v -> {
            appendLog("=== 断开 WebSocket ===");
            if (useOkHttp) {
                okHttpWebSocket.disconnect();
            } else {
                javaWebSocket.disconnect();
            }
        });

        binding.btnClearLog.setOnClickListener(v -> {
            binding.tvLog.setText("");
        });
    }

    private void appendLog(String log) {
        if (binding != null) {
            getActivity().runOnUiThread(() -> {
                binding.tvLog.append(log + "\n");
                binding.scrollView.post(() -> {
                    binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                });
            });
        }
    }

    @Override
    public void onDestroyView() {
        okHttpWebSocket.disconnect();
        javaWebSocket.disconnect();
        binding = null;
        super.onDestroyView();
    }
}
