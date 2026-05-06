package com.example.demo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/**
 * BackgroundService 是一个后台服务示例
 * 
 * 后台服务特点：
 * - 在后台运行，没有用户界面
 * - 优先级较低，系统资源紧张时可能被杀死
 * - Android 8.0+ 对后台服务有严格限制，建议使用 WorkManager 替代
 * 
 * 服务生命周期：
 * - onCreate(): 服务创建时调用（只调用一次）
 * - onStartCommand(): 每次调用 startService() 时调用
 * - onDestroy(): 服务销毁时调用
 * 
 * 本服务演示：每5秒打印一次日志
 */
public class BackgroundService extends Service {

    /** 日志标签 */
    private static final String TAG = "BackgroundService";
    
    /** 日志打印间隔（毫秒） */
    private static final long INTERVAL_MS = 5000;

    /** Handler 用于定时执行任务 */
    private Handler handler;
    
    /** Runnable 任务，每5秒执行一次 */
    private Runnable runnable;
    
    /** 服务运行状态标志 */
    private boolean isRunning = false;

    /**
     * 服务创建时调用
     * 初始化 Handler 和 Runnable
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 创建 Handler，使用主线程的 Looper
        handler = new Handler(Looper.getMainLooper());
        
        // 创建 Runnable，每5秒打印一次日志
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    Log.d(TAG, "后台服务运行中... 每5秒打印一次");
                    // 延迟5秒后再次执行
                    handler.postDelayed(this, INTERVAL_MS);
                }
            }
        };
        
        Log.d(TAG, "BackgroundService 已创建");
    }

    /**
     * 启动服务时调用
     * 
     * @param intent 启动服务时传入的 Intent
     * @param flags 启动标志
     * @param startId 启动 ID
     * @return 返回值决定服务被杀死后的行为：
     *         - START_STICKY: 服务被杀死后会自动重启
     *         - START_NOT_STICKY: 服务被杀死后不会重启
     *         - START_REDELIVER_INTENT: 服务被杀死后会重启并重新传递 Intent
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            // 立即执行第一次任务
            handler.post(runnable);
            Log.d(TAG, "后台服务已启动");
        }
        return START_STICKY;
    }

    /**
     * 服务销毁时调用
     * 停止定时任务，释放资源
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 停止定时任务
        isRunning = false;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        
        Log.d(TAG, "后台服务已停止");
    }

    /**
     * 绑定服务时调用
     * 本服务不支持绑定，返回 null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
