package com.example.demo.network.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpManager {

    private static OkHttpManager instance;
    private final OkHttpClient client;
    private final Handler mainHandler;
    private OnHttpResponseListener listener;

    public interface OnHttpResponseListener {
        void onSuccess(String response, int statusCode, String headers);
        void onFailure(String error);
    }

    private OkHttpManager() {
        mainHandler = new Handler(Looper.getMainLooper());
        
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
            if (listener != null) {
                mainHandler.post(() -> listener.onSuccess("[OkHttp Log] " + message, -1, ""));
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    public static synchronized OkHttpManager getInstance() {
        if (instance == null) {
            instance = new OkHttpManager();
        }
        return instance;
    }

    public void setListener(OnHttpResponseListener listener) {
        this.listener = listener;
    }

    public void performGetRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onFailure("GET 请求失败: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String headers = response.headers().toString();
                int statusCode = response.code();
                String body = response.body() != null ? response.body().string() : "";
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        if (response.isSuccessful()) {
                            listener.onSuccess("GET 响应 [" + statusCode + "]\nHeaders:\n" + headers + "\nBody:\n" + body, statusCode, headers);
                        } else {
                            listener.onFailure("GET 请求失败 [" + statusCode + "]: " + body);
                        }
                    }
                });
            }
        });
    }

    public void performPostRequest(String url, JSONObject jsonBody) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onFailure("POST 请求失败: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String headers = response.headers().toString();
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";

                mainHandler.post(() -> {
                    if (listener != null) {
                        if (response.isSuccessful()) {
                            listener.onSuccess("POST 响应 [" + statusCode + "]\nHeaders:\n" + headers + "\nBody:\n" + responseBody, statusCode, headers);
                        } else {
                            listener.onFailure("POST 请求失败 [" + statusCode + "]: " + responseBody);
                        }
                    }
                });
            }
        });
    }
}
