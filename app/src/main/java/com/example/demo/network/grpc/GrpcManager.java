package com.example.demo.network.grpc;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class GrpcManager {

    private static final String GRPC_HOST = "grpcb.in";
    private static final int GRPC_PORT = 9001;
    
    private ManagedChannel channel;
    private HelloServiceGrpc.HelloServiceBlockingStub blockingStub;
    private final Handler mainHandler;
    private OnGrpcResponseListener listener;

    public interface OnGrpcResponseListener {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public GrpcManager() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(OnGrpcResponseListener listener) {
        this.listener = listener;
    }

    public void connect() {
        channel = ManagedChannelBuilder.forAddress(GRPC_HOST, GRPC_PORT)
                .useTransportSecurity()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .build();
        
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
        
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess("gRPC 通道已建立 (TLS)");
            }
        });
    }

    public void sayHello(String greeting) {
        new Thread(() -> {
            try {
                HelloRequest request = HelloRequest.newBuilder()
                        .setGreeting(greeting)
                        .build();
                
                HelloResponse response = blockingStub.sayHello(request);
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess("gRPC 响应: " + response.getReply());
                    }
                });
            } catch (StatusRuntimeException e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onFailure("gRPC 调用失败: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    public void disconnect() {
        if (channel != null) {
            channel.shutdown();
            channel = null;
            blockingStub = null;
            
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onSuccess("gRPC 通道已关闭");
                }
            });
        }
    }

    public boolean isConnected() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }
}
