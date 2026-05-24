package com.example.demo.network.http;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class NativeHttpManager {

    private static NativeHttpManager instance;
    private OnHttpResponseListener listener;

    static {
        try {
            System.loadLibrary("demo");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    private NativeHttpManager() {}

    public static synchronized NativeHttpManager getInstance() {
        if (instance == null) {
            instance = new NativeHttpManager();
        }
        return instance;
    }

    public void setListener(OnHttpResponseListener listener) {
        this.listener = listener;
    }

    public void performGetRequest(String url) {
        new Thread(() -> {
            try {
                String response = nativeGetRequest(url);
                if (listener != null) {
                    listener.onSuccess(response, 200, "X-Native: libcurl");
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onFailure(e.getMessage());
                }
            }
        }).start();
    }

    public void performPostRequest(String url, String body) {
        new Thread(() -> {
            try {
                String response = nativePostRequest(url, body);
                if (listener != null) {
                    listener.onSuccess(response, 201, "X-Native: libcurl");
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onFailure(e.getMessage());
                }
            }
        }).start();
    }

    private native String nativeGetRequest(String url);
    private native String nativePostRequest(String url, String body);

    public interface OnHttpResponseListener {
        void onSuccess(String response, int statusCode, String headers);
        void onFailure(String error);
    }
}