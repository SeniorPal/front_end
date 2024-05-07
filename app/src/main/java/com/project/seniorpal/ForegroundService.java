package com.project.seniorpal;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @SuppressLint("ForegroundServiceType")
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = getNotification(); // 确保这个方法返回一个有效的通知对象
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(100, notification);
        } else {
            startForeground(100, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        }
        return START_NOT_STICKY;
    }


    public Notification getNotification() {
        NotificationChannel channel = new NotificationChannel("channel_01", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(this, "channel_01")
                .setContentTitle("Service Running")
                .setContentText("This is a running foreground service")
                .setSmallIcon(R.drawable.ic_banner_foreground);
        return builder.build();
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Foreground Service";
            String description = "用于保持应用后台运行";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 用于绑定，不需要时返回null
    }
}
