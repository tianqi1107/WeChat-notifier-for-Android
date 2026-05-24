package com.tq.wechatnotifier.alert;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class AlertManager {
    private static final String TAG = "AlertManager";
    private static MediaPlayer mediaPlayer;
    private static Vibrator vibrator;

    public static synchronized void playAlert(Context context) {
        playRingtone(context);
        vibrate(context);
    }

    private static void playRingtone(Context context) {
        stopRingtone();

        mediaPlayer = new MediaPlayer();

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mediaPlayer.setAudioAttributes(attrs);

        try {
            // Try custom sound first, fall back to system alarm
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/alert_sound");
            mediaPlayer.setDataSource(context, soundUri);
        } catch (Exception e) {
            Log.w(TAG, "Custom sound not found, using system alarm ringtone");
            Uri defaultAlarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (defaultAlarm == null) {
                defaultAlarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            try {
                mediaPlayer.setDataSource(context, defaultAlarm);
            } catch (Exception e2) {
                Log.e(TAG, "Failed to set any sound source", e2);
                mediaPlayer.release();
                mediaPlayer = null;
                return;
            }
        }

        try {
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                if (mediaPlayer == mp) {
                    mediaPlayer = null;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to play alert sound", e);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private static void stopRingtone() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping media player", e);
            }
            mediaPlayer = null;
        }
    }

    private static void vibrate(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }

    public static synchronized void stopAlert() {
        stopRingtone();
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }
}
