package com.example.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentActivityBinding;

import java.util.List;

/**
 * ActivityFragment 用于演示 Intent 的两种类型：显式 Intent 和隐式 Intent
 * 
 * Intent 是 Android 中组件间通信的核心机制：
 * - 显式 Intent：明确指定目标组件（通过类名），用于应用内部跳转
 * - 隐式 Intent：不指定具体组件，通过 action、category、data 匹配，用于调用系统功能或跨应用通信
 * 
 * Fragment 生命周期说明：
 * - onCreateView(): 创建视图，返回 Fragment 的布局
 * - onViewCreated(): 视图创建完成，可以开始操作视图
 * - onDestroyView(): 视图被销毁，释放 ViewBinding
 */
public class ActivityFragment extends Fragment {

    /** ViewBinding 对象，用于安全访问布局视图 */
    private FragmentActivityBinding binding;

    /**
     * 创建 Fragment 视图
     * 
     * @param inflater 布局解析器，用于将 XML 布局转换为 View 对象
     * @param container 父容器，Fragment 将被添加到其中
     * @param savedInstanceState 保存的实例状态
     * @return Fragment 的根视图
     */
    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                         @Nullable android.view.ViewGroup container,
                                         @Nullable Bundle savedInstanceState) {
        // 使用 ViewBinding 绑定布局
        binding = FragmentActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后调用，此时可以安全地操作视图组件
     */
    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 设置按钮点击监听器
        setupListeners();
    }

    /**
     * 设置按钮点击事件监听器
     */
    private void setupListeners() {
        // 显式 Intent 演示：跳转到 SecondActivity
        binding.btnStartSecond.setOnClickListener(v -> {
            // 创建显式 Intent，明确指定目标 Activity 类
            Intent intent = new Intent(getActivity(), SecondActivity.class);
            // 启动 Activity
            startActivity(intent);
        });

        // 隐式 Intent 演示：调用系统拨号功能
        binding.btnDial.setOnClickListener(v -> {
            String phoneNumber = binding.etPhone.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                Toast.makeText(getContext(), "请输入电话号码", Toast.LENGTH_SHORT).show();
                return;
            }
            // 调用拨号方法
            dialPhone(phoneNumber);
        });
    }

    /**
     * 使用隐式 Intent 调用系统拨号功能
     * 
     * 隐式 Intent 工作原理：
     * 1. 创建 Intent 时指定 action（ACTION_DIAL）和 data（tel:xxx）
     * 2. 系统通过 PackageManager 查找匹配的 Activity
     * 3. 如果找到多个匹配，会显示选择器让用户选择
     * 
     * Android 11+ 包可见性：
     * - 需要在 AndroidManifest.xml 中添加 <queries> 声明
     * - 使用 queryIntentActivities() 替代 resolveActivity() 判断是否有匹配应用
     * 
     * @param phoneNumber 要拨打的电话号码
     */
    private void dialPhone(String phoneNumber) {
        // 构建电话 URI：tel:电话号码
        Uri uri = Uri.parse("tel:" + phoneNumber);
        // 创建隐式 Intent，指定 action 为 ACTION_DIAL
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        
        // Android 11+ 包可见性检查：使用 queryIntentActivities 判断是否有拨号应用
        // queryIntentActivities 返回所有能处理此 Intent 的 Activity 列表
        PackageManager packageManager = requireActivity().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        
        // 检查是否有应用可以处理此 Intent
        if (!activities.isEmpty()) {
            startActivity(intent);
        } else {
            // 如果没有找到拨号应用，尝试使用 ACTION_VIEW 作为备用方案
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
            List<ResolveInfo> viewActivities = packageManager.queryIntentActivities(viewIntent, 0);
            
            if (!viewActivities.isEmpty()) {
                startActivity(viewIntent);
            } else {
                Toast.makeText(getContext(), "没有找到拨号应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fragment 视图被销毁时调用
     * 必须在此处将 ViewBinding 置为 null，避免内存泄漏
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
