package com.example.demo.component;

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
import androidx.core.content.ContextCompat;

import com.example.demo.databinding.FragmentBroadcastBinding;

public class BroadcastFragment extends Fragment {

    private static final String TAG = "BroadcastFragment";
    public static final String ACTION_MY_NOTIFICATION = "com.example.MY_NOTIFICATION";

    private FragmentBroadcastBinding binding;
    private BroadcastReceiver dynamicReceiver;
    private IntentFilter intentFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBroadcastBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        dynamicReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "收到广播: " + action);

                String status = "";
                
                if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    status = "【系统广播】电源已连接！";
                } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        status = "【系统广播】Wi-Fi 已开启！";
                    } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                        status = "【系统广播】Wi-Fi 已关闭！";
                    }
                } else if (ACTION_MY_NOTIFICATION.equals(action)) {
                    status = "【自定义广播】收到: com.example.MY_NOTIFICATION";
                }

                if (!status.isEmpty()) {
                    binding.tvBroadcastStatus.setText(status);
                }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ACTION_MY_NOTIFICATION);

        // 使用 ContextCompat.registerReceiver 兼容不同 API 版本
        ContextCompat.registerReceiver(requireContext(), dynamicReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
