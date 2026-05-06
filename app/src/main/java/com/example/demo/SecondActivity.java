package com.example.demo;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * SecondActivity 用于演示显式 Intent 的使用
 *
 * 显式 Intent 说明：
 * - 明确指定目标组件（通过类名）
 * - 用于应用内部组件之间的跳转
 * - 示例：new Intent(getActivity(), SecondActivity.class)
 *
 * 此 Activity 在 ActivityFragment 中通过显式 Intent 启动
 * 展示了 Activity 之间的基本跳转流程
 */
public class SecondActivity extends AppCompatActivity {

    /**
     * Activity 创建时调用
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 启用边缘到边缘显示，让内容延伸到系统栏和刘海区域
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_second);

        // 处理刘海屏/打孔屏适配
        setupWindowInsets();

        // 获取关闭按钮并设置点击事件
        // 点击后调用 finish() 销毁当前 Activity，返回上一个 Activity
        Button btnFinish = findViewById(R.id.btn_finish);
        btnFinish.setOnClickListener(v -> finish());
    }

    /**
     * 设置窗口Insets处理，解决刘海屏/打孔屏遮挡内容的问题
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, windowInsets) -> {
            // 获取状态栏和挖孔区域的inset
            int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()).top;
            int leftInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()).left;
            int rightInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()).right;
            // 设置内容padding，避免被状态栏和挖孔区域遮挡
            view.setPadding(leftInset, topInset, rightInset, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Activity 生命周期说明（完整流程）：
     *
     * 启动时：
     * onCreate() → onStart() → onResume()
     *
     * 关闭时（调用 finish() 或用户按返回键）：
     * onPause() → onStop() → onDestroy()
     *
     * 屏幕旋转时（配置变更）：
     * onPause() → onStop() → onDestroy() → onCreate() → onStart() → onResume()
     *
     * 注意：如果在 AndroidManifest.xml 中配置了 android:configChanges="orientation"
     * 则屏幕旋转时不会重建 Activity，只会调用 onConfigurationChanged()
     */
}
