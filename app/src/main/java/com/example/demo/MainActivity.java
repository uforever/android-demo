package com.example.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.demo.databinding.ActivityMainBinding;

/**
 * MainActivity 是应用程序的主入口 Activity，作为四大组件演示的容器
 *
 * 功能说明：
 * 1. 作为应用的启动页面，承载底部导航栏（BottomNavigationView）
 * 2. 使用 Navigation Component 管理四个 Fragment 的切换
 * 3. 处理 Android 13+ 的 POST_NOTIFICATIONS 动态权限请求
 *
 * 生命周期说明：
 * - onCreate(): 初始化 ViewBinding、设置导航、请求权限
 * - onStart(): Activity 可见但不可交互
 * - onResume(): Activity 可见且可交互（用户可操作底部导航）
 * - onPause(): Activity 失去焦点（如按下Home键）
 * - onStop(): Activity 完全不可见
 * - onDestroy(): Activity 被销毁（如系统回收内存）
 */
public class MainActivity extends AppCompatActivity {

    /**
     * ViewBinding 对象，用于类型安全地访问布局中的视图
     * 替代传统的 findViewById()，避免空指针异常和类型转换错误
     */
    private ActivityMainBinding binding;

    /**
     * NavController 导航控制器，用于管理 Fragment 之间的切换
     * 通过 NavHostFragment 获取，与 BottomNavigationView 关联
     */
    private NavController navController;

    /**
     * 通知权限请求启动器（Android 13+ 新增）
     * 使用 Activity Result API 处理权限请求结果
     * registerForActivityResult() 在 onCreate() 之前调用（作为成员变量初始化）
     */
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "通知权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Activity 创建时调用的生命周期方法
     * 执行顺序：onCreate() -> onStart() -> onResume()
     *
     * @param savedInstanceState 保存的实例状态（如屏幕旋转时保存的数据）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 启用边缘到边缘显示，让内容延伸到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // 使用 ViewBinding 绑定布局，替代 setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 处理刘海屏/打孔屏适配
        setupWindowInsets();

        // 初始化导航组件
        setupNavigation();

        // 请求 Android 13+ 的通知权限（前台服务需要）
        requestNotificationPermissionIfNeeded();
    }

    /**
     * 设置窗口Insets处理，解决刘海屏/打孔屏遮挡内容的问题
     *
     * 问题说明：
     * 在带有摄像头挖孔的设备上，如果不处理WindowInsets，
     * 内容可能会被摄像头挖孔区域遮挡，导致标签文字显示异常
     *
     * 解决方案：
     * 1. 使用WindowInsetsCompat获取系统栏和挖孔区域的inset
     * 2. 将内容padding设置为inset值，使内容不会被遮挡
     */
    private void setupWindowInsets() {
        // 为底部导航栏设置insets，避免被系统栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation, (view, windowInsets) -> {
            // 获取系统栏（导航栏）的inset
            view.setPadding(0, 0, 0, windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // 为NavHostFragment设置insets，避免被状态栏和挖孔区域遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment, (view, windowInsets) -> {
            // 获取状态栏和挖孔区域的inset
            int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()).top;
            int leftInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()).left;
            int rightInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()).right;
            view.setPadding(leftInset, topInset, rightInset, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * 设置导航组件
     * 1. 获取 NavHostFragment（导航容器）
     * 2. 获取 NavController（导航控制器）
     * 3. 将 BottomNavigationView 与 NavController 关联
     *
     * Navigation Component 工作原理：
     * - NavHostFragment 作为 Fragment 的容器
     * - NavController 管理导航栈和 Fragment 切换
     * - NavigationUI.setupWithNavController() 自动处理底部导航点击事件
     */
    private void setupNavigation() {
        // 通过 ID 获取 NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            // 获取导航控制器
            navController = navHostFragment.getNavController();

            // 将底部导航与导航控制器关联
            // 点击底部导航项时自动切换对应的 Fragment
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
    }

    /**
     * 请求通知权限（Android 13+ 动态权限）
     *
     * Android 13 (API 33) 引入了 POST_NOTIFICATIONS 权限
     * 前台服务需要此权限才能显示通知
     *
     * 权限请求流程：
     * 1. 检查 Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU (Android 13)
     * 2. 检查当前是否已授予权限
     * 3. 如果未授予，使用 ActivityResultLauncher 请求权限
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 启动权限请求
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
