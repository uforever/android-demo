package com.example.demo.network.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.lsposed.lsparanoid.Obfuscate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.Map;

@Obfuscate
public class RetrofitManager {

    private static RetrofitManager instance;
    private final EchoApiService apiService;
    private final Handler mainHandler;
    private OnHttpResponseListener listener;

    public interface OnHttpResponseListener {
        void onSuccess(String response, int statusCode, String headers);
        void onFailure(String error);
    }

    public interface EchoApiService {
        @GET("/")
        Call<Map<String, Object>> getEcho(@Query("foo") String foo);

        @POST("/")
        Call<Map<String, Object>> postEcho(@Body Map<String, Object> body);

        @POST("/")
        @FormUrlEncoded
        Call<Map<String, Object>> postFormEcho(@Field("key") String key, @Field("value") String value);
    }

    private RetrofitManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://echo.hoppscotch.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(EchoApiService.class);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized RetrofitManager getInstance() {
        if (instance == null) {
            instance = new RetrofitManager();
        }
        return instance;
    }

    public void setListener(OnHttpResponseListener listener) {
        this.listener = listener;
    }

    public void performGetRequest(String param) {
        apiService.getEcho(param).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                String headers = response.headers().toString();
                int statusCode = response.code();
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        if (response.isSuccessful() && response.body() != null) {
                            listener.onSuccess("Retrofit GET 响应 [" + statusCode + "]\nHeaders:\n" + headers + "\nBody:\n" + response.body().toString(), statusCode, headers);
                        } else {
                            listener.onFailure("Retrofit GET 请求失败 [" + statusCode + "]");
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onFailure("Retrofit GET 请求失败: " + t.getMessage());
                    }
                });
            }
        });
    }

    public void performPostRequest(Map<String, Object> body) {
        apiService.postEcho(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                String headers = response.headers().toString();
                int statusCode = response.code();
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        if (response.isSuccessful() && response.body() != null) {
                            listener.onSuccess("Retrofit POST 响应 [" + statusCode + "]\nHeaders:\n" + headers + "\nBody:\n" + response.body().toString(), statusCode, headers);
                        } else {
                            listener.onFailure("Retrofit POST 请求失败 [" + statusCode + "]");
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onFailure("Retrofit POST 请求失败: " + t.getMessage());
                    }
                });
            }
        });
    }
}
