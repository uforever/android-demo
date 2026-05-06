package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentServiceBinding;

/**
 * ServiceFragment 用于演示 Android Service 组件的两种类型：后台服务和前台服务
 * 
 * Service 是 Android 四大组件之一，用于在后台执行长时间运行的操作：
 * 
 * 【后台服务 (Background Service)】
 * - 在后台运行，没有用户界面
 * - 优先级较低，当系统资源紧张时可能被系统杀死
 * - 适合不需要持续运行的任务（如下载文件）
 * - 注意：Android 8.0+ 对后台服务有严格限制
 * 
 * 【前台服务 (Foreground Service)】
 * - 显示常驻通知，用户可见
 * - 优先级高，不易被系统杀死
 * - 适合需要持续运行的任务（如音乐播放、定位追踪）
 * - Android 13+ 需要 POST_NOTIFICATIONS 权限
 */
public class ServiceFragment extends Fragment {

    /** 日志标签，用于 Logcat 输出 */
    private static final String TAG = "ServiceFragment";
    
    /** ViewBinding 对象 */
    private FragmentServiceBinding binding;

    /**
     * 创建 Fragment 视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentServiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后设置监听器
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
    }

    /**
     * 设置四个按钮的点击事件监听器
     */
    private void setupListeners() {
        // 启动后台服务按钮
        binding.btnStartBackground.setOnClickListener(v -> {
            // 创建 Intent 并指定目标服务
            Intent intent = new Intent(requireContext(), BackgroundService.class);
            // 启动服务（Android 8.0+ 后台服务受限制）
            requireContext().startService(intent);
            Log.d(TAG, "后台服务已启动");
        });

        // 停止后台服务按钮
        binding.btnStopBackground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BackgroundService.class);
            // 停止服务
            requireContext().stopService(intent);
            Log.d(TAG, "后台服务已停止");
        });

        // 启动前台服务按钮
        binding.btnStartForeground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ForegroundService.class);
            // 启动前台服务（需要通知权限）
            requireContext().startService(intent);
            Log.d(TAG, "前台服务已启动");
        });

        // 停止前台服务按钮
        binding.btnStopForeground.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ForegroundService.class);
            requireContext().stopService(intent);
            Log.d(TAG, "前台服务已停止");
        });
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
