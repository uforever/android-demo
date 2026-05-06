package com.example.demo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * ForegroundService 是一个前台服务示例
 * 
 * 前台服务特点：
 * - 显示常驻通知，用户可见
 * - 优先级高，不易被系统杀死
 * - 适合需要持续运行的任务（如音乐播放、定位追踪）
 * - Android 13+ 需要 POST_NOTIFICATIONS 权限
 * 
 * 前台服务与后台服务的区别：
 * - 前台服务必须显示通知，后台服务不需要
 * - 前台服务优先级更高，系统不会轻易杀死
 * - 前台服务消耗更多系统资源（因为通知需要持续显示）
 */
public class ForegroundService extends Service {

    /** 日志标签 */
    private static final String TAG = "ForegroundService";
    
    /** 通知渠道 ID */
    private static final String CHANNEL_ID = "foreground_service_channel";
    
    /** 通知 ID */
    private static final int NOTIFICATION_ID = 1;

    /** Handler 用于定时执行任务 */
    private Handler handler;
    
    /** Runnable 任务 */
    private Runnable runnable;
    
    /** 服务运行状态标志 */
    private boolean isRunning = false;

    /**
     * 服务创建时调用
     * 初始化 Handler、Runnable 和通知渠道
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 创建 Handler
        handler = new Handler(Looper.getMainLooper());
        
        // 创建定时任务
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    Log.d(TAG, "前台服务运行中... 正在执行后台任务");
                    handler.postDelayed(this, 5000);
                }
            }
        };
        
        // 创建通知渠道（Android 8.0+ 必需）
        createNotificationChannel();
        
        Log.d(TAG, "ForegroundService 已创建");
    }

    /**
     * 启动服务时调用
     * 调用 startForeground() 将服务提升为前台服务
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            
            // 启动前台服务，显示通知
            startForeground(NOTIFICATION_ID, createNotification());
            
            // 启动定时任务
            handler.post(runnable);
            
            Log.d(TAG, "前台服务已启动，通知已显示");
        }
        return START_STICKY;
    }

    /**
     * 服务销毁时调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        isRunning = false;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        
        Log.d(TAG, "前台服务已停止");
    }

    /**
     * 绑定服务时调用
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 创建通知渠道（Android 8.0+ 必需）
     * 
     * 通知渠道允许用户管理应用的通知行为
     * 必须在显示通知前创建渠道
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_description));
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 创建前台服务的通知
     * 
     * @return Notification 对象
     */
    private Notification createNotification() {
        // 创建点击通知时打开的 Intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                notificationIntent,
                // FLAG_UPDATE_CURRENT: 更新已存在的 PendingIntent
                // FLAG_IMMUTABLE: 不可变标志（Android 12+ 推荐）
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 使用 NotificationCompat.Builder 创建通知
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)  // 设置为持续通知（不可被用户清除）
                .build();
    }
}
