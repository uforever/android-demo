package com.example.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentBroadcastBinding;

/**
 * BroadcastFragment 用于演示 BroadcastReceiver（广播接收器）组件
 * 
 * BroadcastReceiver 是 Android 四大组件之一，用于接收和处理广播消息：
 * 
 * 【广播类型】
 * - 系统广播：Android 系统发送的广播（如电源连接、网络状态变化）
 * - 自定义广播：应用自己定义和发送的广播
 * 
 * 【注册方式】
 * - 静态注册：在 AndroidManifest.xml 中注册（适合需要在应用未启动时接收广播）
 * - 动态注册：在代码中注册（适合只在特定生命周期内接收广播）
 * 
 * 本 Fragment 使用动态注册方式，在 onResume() 注册，onPause() 注销，避免内存泄漏
 */
public class BroadcastFragment extends Fragment {

    /** 日志标签 */
    private static final String TAG = "BroadcastFragment";
    
    /** 自定义广播 Action */
    public static final String ACTION_MY_NOTIFICATION = "com.example.MY_NOTIFICATION";

    /** ViewBinding 对象 */
    private FragmentBroadcastBinding binding;
    
    /** 动态注册的广播接收器 */
    private BroadcastReceiver dynamicReceiver;
    
    /** 意图过滤器，用于指定要接收的广播类型 */
    private IntentFilter intentFilter;

    /**
     * 创建 Fragment 视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBroadcastBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Fragment 可见时注册广播接收器
     */
    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    /**
     * Fragment 不可见时注销广播接收器
     * 必须在此处注销，避免内存泄漏
     */
    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
    }

    /**
     * 注册广播接收器
     * 
     * 本方法演示了如何动态注册广播接收器：
     * 1. 创建 BroadcastReceiver 实例，实现 onReceive() 方法处理广播
     * 2. 创建 IntentFilter，添加要监听的广播 Action
     * 3. 调用 registerReceiver() 注册接收器
     */
    private void registerBroadcastReceiver() {
        // 创建广播接收器
        dynamicReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "收到广播: " + action);

                String status = "";
                
                // 处理系统广播：电源连接
                if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    status = "【系统广播】电源已连接！";
                } 
                // 处理系统广播：Wi-Fi 状态变化
                else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        status = "【系统广播】Wi-Fi 已开启！";
                    } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                        status = "【系统广播】Wi-Fi 已关闭！";
                    }
                } 
                // 处理自定义广播
                else if (ACTION_MY_NOTIFICATION.equals(action)) {
                    status = "【自定义广播】收到: com.example.MY_NOTIFICATION";
                }

                // 更新 UI 显示广播状态
                if (!status.isEmpty()) {
                    binding.tvBroadcastStatus.setText(status);
                }
            }
        };

        // 创建意图过滤器，指定要接收的广播类型
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);      // 电源连接
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); // Wi-Fi 状态变化
        intentFilter.addAction(ACTION_MY_NOTIFICATION);              // 自定义广播

        // 注册广播接收器
        // Context.RECEIVER_NOT_EXPORTED 表示接收器不对外暴露（安全考虑）
        requireContext().registerReceiver(dynamicReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    /**
     * 注销广播接收器
     * 
     * 注意：
     * 1. 必须在 Fragment 销毁前注销，否则会导致内存泄漏
     * 2. 使用 try-catch 包裹，避免重复注销抛出异常
     */
    private void unregisterBroadcastReceiver() {
        if (dynamicReceiver != null) {
            try {
                requireContext().unregisterReceiver(dynamicReceiver);
            } catch (Exception e) {
                Log.e(TAG, "注销广播失败: " + e.getMessage());
            }
            dynamicReceiver = null;
        }
    }

    /**
     * Fragment 视图销毁时释放资源
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
