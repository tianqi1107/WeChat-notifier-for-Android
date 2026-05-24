package com.tq.wechatnotifier;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class WeChatNotifierApp extends Application {
    public static final String CHANNEL_SERVICE = "wechat_notifier_service";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_SERVICE,
                "后台服务通知",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("保持微信通知监听服务运行");
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
    }
}
