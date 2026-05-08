package com.example.demo.network.websocket;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class OkHttpWebSocket {

    private static final String WS_URL = "wss://echo.websocket.org";
    
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final Handler mainHandler;
    private OnWebSocketListener listener;

    public interface OnWebSocketListener {
        void onConnected();
        void onMessage(String message);
        void onDisconnected(int code, String reason);
        void onError(String error);
    }

    public OkHttpWebSocket() {
        client = new OkHttpClient.Builder()
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(OnWebSocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(WS_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onConnected();
                    }
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onMessage(text);
                    }
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onMessage(bytes.hex());
                    }
                });
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onDisconnected(code, reason);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(t.getMessage());
                    }
                });
            }
        });
    }

    public void send(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
    }

    public boolean isConnected() {
        return webSocket != null;
    }
}
