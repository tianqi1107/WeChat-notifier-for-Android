package com.tq.wechatnotifier.service;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.tq.wechatnotifier.alert.AlertManager;
import com.tq.wechatnotifier.time.TimePeriodHelper;
import com.tq.wechatnotifier.util.PrefsManager;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeChatNotificationListener extends NotificationListenerService {
    private static final String TAG = "WeChatNL";
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final Pattern SENDER_PATTERN = Pattern.compile("^(.+?):\\s");

    private PrefsManager prefsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "NotificationListener onCreate");
        prefsManager = new PrefsManager(this);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "NotificationListener connected");
        prefsManager = new PrefsManager(this);

        // Start keep-alive service
        try {
            Intent intent = new Intent(this, KeepAliveService.class);
            startForegroundService(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start KeepAliveService", e);
        }
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.w(TAG, "NotificationListener disconnected, requesting rebind");
        try {
            requestRebind(new ComponentName(this, WeChatNotificationListener.class));
        } catch (Exception e) {
            Log.e(TAG, "Failed to request rebind", e);
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "NotificationListener destroyed, requesting rebind");
        try {
            requestRebind(new ComponentName(this, WeChatNotificationListener.class));
        } catch (Exception e) {
            Log.e(TAG, "Failed to request rebind on destroy", e);
        }
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;

        // Only process WeChat notifications
        if (!WECHAT_PACKAGE.equals(sbn.getPackageName())) return;

        // Check if service is enabled
        if (!prefsManager.isServiceEnabled()) return;

        // Check time period
        if (prefsManager.isTimePeriodEnabled()) {
            if (!TimePeriodHelper.isWithinActivePeriod(
                    prefsManager.getStartHour(), prefsManager.getStartMinute(),
                    prefsManager.getEndHour(), prefsManager.getEndMinute())) {
                return;
            }
        }

        // Extract sender name
        String sender = extractSenderName(sbn.getNotification());
        if (sender == null || sender.isEmpty()) {
            Log.d(TAG, "Could not extract sender name from notification");
            return;
        }

        Log.d(TAG, "WeChat notification from: " + sender);

        // Record seen contact
        prefsManager.addSeenContact(sender);

        // Check whitelist
        Set<String> whitelist = prefsManager.getWhitelist();
        if (whitelist.contains(sender)) {
            Log.i(TAG, "Whitelisted contact detected: " + sender + " - triggering alert");
            AlertManager.playAlert(this);
        }
    }

    private String extractSenderName(Notification notification) {
        Bundle extras = notification.extras;
        if (extras == null) return null;

        String title = extras.getString(Notification.EXTRA_TITLE);
        String text = extras.getString(Notification.EXTRA_TEXT);
        String bigText = extras.getString(Notification.EXTRA_BIG_TEXT);
        String conversationTitle = extras.getString(Notification.EXTRA_CONVERSATION_TITLE);

        Log.d(TAG, "Title: " + title + ", Text: " + text + ", BigText: " + bigText);

        // Case 1: Direct message - title is the sender name
        if (title != null && !isWeChatAppName(title)) {
            return title.trim();
        }

        // Case 2: Conversation title available
        if (conversationTitle != null && !isWeChatAppName(conversationTitle)) {
            return conversationTitle.trim();
        }

        // Case 3: Parse "Sender: message" format from text
        if (text != null) {
            String sender = parseSenderFromText(text);
            if (sender != null) return sender;
        }

        // Case 4: Parse from big text
        if (bigText != null) {
            String sender = parseSenderFromText(bigText);
            if (sender != null) return sender;
        }

        return null;
    }

    private String parseSenderFromText(String text) {
        Matcher matcher = SENDER_PATTERN.matcher(text);
        if (matcher.find()) {
            String sender = matcher.group(1);
            if (sender != null && !isWeChatAppName(sender)) {
                return sender.trim();
            }
        }
        return null;
    }

    private boolean isWeChatAppName(String name) {
        return "微信".equals(name) || "WeChat".equals(name) || "com.tencent.mm".equals(name);
    }
}
