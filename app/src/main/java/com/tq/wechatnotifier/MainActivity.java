package com.tq.wechatnotifier;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import android.service.notification.NotificationListenerService;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.tq.wechatnotifier.service.KeepAliveService;
import com.tq.wechatnotifier.util.PrefsManager;

public class MainActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private MaterialSwitch serviceSwitch;
    private MaterialSwitch timeSwitch;
    private TextView permissionStatus;
    private TextView timeRangeText;
    private MaterialButton btnPickStart;
    private MaterialButton btnPickEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = new PrefsManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("微信通知提醒");

        // Permission status
        permissionStatus = findViewById(R.id.text_permission_status);

        // Auto-restart service if enabled
        if (prefsManager.isServiceEnabled() && isNotificationListenerEnabled()) {
            startKeepAliveService();
            try {
                NotificationListenerService.requestRebind(
                        new ComponentName(this, com.tq.wechatnotifier.service.WeChatNotificationListener.class));
            } catch (Exception e) {
                // ignore
            }
        }

        // Service switch
        serviceSwitch = findViewById(R.id.switch_service);
        serviceSwitch.setChecked(prefsManager.isServiceEnabled());
        serviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !isNotificationListenerEnabled()) {
                buttonView.setChecked(false);
                showPermissionDialog();
                return;
            }
            prefsManager.setServiceEnabled(isChecked);
            if (isChecked) {
                startKeepAliveService();
                // Force rebind NotificationListenerService
                try {
                    NotificationListenerService.requestRebind(
                            new ComponentName(MainActivity.this, com.tq.wechatnotifier.service.WeChatNotificationListener.class));
                } catch (Exception e) {
                    // ignore
                }
            } else {
                stopService(new Intent(this, KeepAliveService.class));
            }
        });

        // Time period switch
        timeSwitch = findViewById(R.id.switch_time_period);
        timeSwitch.setChecked(prefsManager.isTimePeriodEnabled());
        timeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.setTimePeriodEnabled(isChecked);
            updateTimePickersEnabled(isChecked);
        });

        // Time pickers
        timeRangeText = findViewById(R.id.text_time_range);
        btnPickStart = findViewById(R.id.btn_pick_start);
        btnPickEnd = findViewById(R.id.btn_pick_end);

        btnPickStart.setOnClickListener(v -> showTimePicker(true));
        btnPickEnd.setOnClickListener(v -> showTimePicker(false));

        updateTimeDisplay();
        updateTimePickersEnabled(prefsManager.isTimePeriodEnabled());

        // Whitelist button
        MaterialButton btnWhitelist = findViewById(R.id.btn_manage_whitelist);
        btnWhitelist.setOnClickListener(v -> {
            startActivity(new Intent(this, WhitelistActivity.class));
        });

        // Battery optimization hint
        MaterialButton btnBattery = findViewById(R.id.btn_battery_settings);
        btnBattery.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception e) {
                // fallback
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    private void updatePermissionStatus() {
        boolean enabled = isNotificationListenerEnabled();
        if (enabled) {
            permissionStatus.setText("通知监听权限：已授予");
            permissionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_ok));
        } else {
            permissionStatus.setText("通知监听权限：未授予（点击下方按钮开启）");
            permissionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_error));
        }
    }

    private boolean isNotificationListenerEnabled() {
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要通知监听权限")
                .setMessage("请在设置中找到\"微信通知提醒\"并开启通知访问权限。\n\n开启后返回此页面。")
                .setPositiveButton("前往设置", (d, w) -> {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void startKeepAliveService() {
        Intent intent = new Intent(this, KeepAliveService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    private void showTimePicker(boolean isStart) {
        int hour = isStart ? prefsManager.getStartHour() : prefsManager.getEndHour();
        int minute = isStart ? prefsManager.getStartMinute() : prefsManager.getEndMinute();

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(isStart ? "设置开始时间" : "设置结束时间")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            if (isStart) {
                prefsManager.setTimePeriod(picker.getHour(), picker.getMinute(),
                        prefsManager.getEndHour(), prefsManager.getEndMinute());
            } else {
                prefsManager.setTimePeriod(prefsManager.getStartHour(), prefsManager.getStartMinute(),
                        picker.getHour(), picker.getMinute());
            }
            updateTimeDisplay();
        });

        picker.show(getSupportFragmentManager(), isStart ? "start_time" : "end_time");
    }

    private void updateTimeDisplay() {
        String start = String.format("%02d:%02d", prefsManager.getStartHour(), prefsManager.getStartMinute());
        String end = String.format("%02d:%02d", prefsManager.getEndHour(), prefsManager.getEndMinute());
        timeRangeText.setText("生效时间段: " + start + " - " + end);
        btnPickStart.setText("开始: " + start);
        btnPickEnd.setText("结束: " + end);
    }

    private void updateTimePickersEnabled(boolean enabled) {
        btnPickStart.setEnabled(enabled);
        btnPickEnd.setEnabled(enabled);
        timeRangeText.setAlpha(enabled ? 1.0f : 0.5f);
    }
}
