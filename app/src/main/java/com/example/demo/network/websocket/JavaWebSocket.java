package com.example.demo.network.websocket;

import android.os.Handler;
import android.os.Looper;

import org.lsposed.lsparanoid.Obfuscate;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

@Obfuscate
public class JavaWebSocket {

    private static final String WS_URL = "wss://echo.websocket.org";
    
    private WebSocketClient webSocketClient;
    private final Handler mainHandler;
    private OnWebSocketListener listener;

    public interface OnWebSocketListener {
        void onConnected();
        void onMessage(String message);
        void onDisconnected(int code, String reason);
        void onError(String error);
    }

    public JavaWebSocket() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(OnWebSocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        try {
            URI uri = new URI(WS_URL);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onConnected();
                        }
                    });
                }

                @Override
                public void onMessage(String message) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onMessage(message);
                        }
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onDisconnected(code, reason);
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onError(ex.getMessage());
                        }
                    });
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            if (listener != null) {
                listener.onError("URI 解析错误: " + e.getMessage());
            }
        }
    }

    public void send(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}
